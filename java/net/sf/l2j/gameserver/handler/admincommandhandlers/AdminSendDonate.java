package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ConnectionPool;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance.ItemLocation;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.clientpackets.Say2;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class AdminSendDonate implements IAdminCommandHandler
{
	protected static final Logger _log = Logger.getLogger(AdminSendDonate.class.getName());
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_donate",
		"admin_givedonate"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{	

		if (command.equals("admin_donate"))
		{
			AdminHelpPage.showHelpPage(activeChar, "senddonate.htm");
		}
		else if (command.startsWith("admin_givedonate"))
		{
			try
			{
				
				StringTokenizer st = new StringTokenizer(command, " ");
				st.nextToken();
				
				String playername = "";
				Player player = null;
				
				if (st.countTokens() == 4)
				{
					playername = st.nextToken();
					player = L2World.getInstance().getPlayer(playername);
					String id = st.nextToken();
					int idval = Integer.parseInt(id);
					String num = st.nextToken();
					int numval = Integer.parseInt(num);
					String location = st.nextToken();
					
					// Can't use on yourself
					if (player != null && player.equals(activeChar))
					{
						activeChar.sendPacket(SystemMessageId.CANNOT_USE_ON_YOURSELF);
						return false;
					}
					
					if (player != null)
						createItem(activeChar, player, idval, numval, getItemLocation(location));
					else
						giveItemToOfflinePlayer(activeChar, playername, idval, numval, getItemLocation(location));
					
				}
				else
				{
					activeChar.sendChatMessage(0, Say2.ALL, "SYS", "Please fill in all the blanks before requesting a item creation.");
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //donate [player] <itemId> [amount] [location]");
			}
			
			AdminHelpPage.showHelpPage(activeChar, "senddonate.htm");
		}
		
		return true;
	}
	
	/**
	 * Create item on player inventory. If player is offline, store item on database by giveItemToOfflinePlayer method.
	 * @param activeChar
	 * @param player
	 * @param id
	 * @param count
	 * @param location
	 */
	private static void createItem(Player activeChar, Player player, int id, int count, ItemLocation location)
	{
		Item item = ItemTable.getInstance().getTemplate(id);
		if (item == null)
		{
			activeChar.sendChatMessage(0, Say2.ALL, "SYS", "Unknown Item ID.");
			return;
		}
		
		if (count > 10 && !item.isStackable())
		{
			activeChar.sendChatMessage(0, Say2.ALL, "SYS", "You can't to create more than 10 non stackable items!");
			return;
		}
		
		if (location == ItemLocation.INVENTORY)
			player.getInventory().addItem("Admin", id, count, player, activeChar);
		else if (location == ItemLocation.WAREHOUSE)
			player.getWarehouse().addItem("Admin", id, count, player, activeChar);
		
		if (activeChar != player)
		{
			if (count > 1)
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S2_S1).addItemName(id).addNumber(count));
			else
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1).addItemName(id));
		}
		
		activeChar.sendChatMessage(0, Say2.ALL, "SYS", "Spawned " + count + " " + item.getName() + " in " + player.getName() + " " + (location == ItemLocation.INVENTORY ? "inventory" : "warehouse") + ".");
	}
	
	/**
	 * If player is offline, store item by SQL Query
	 * @param activeChar
	 * @param playername
	 * @param id
	 * @param count
	 * @param location
	 */
	private static void giveItemToOfflinePlayer(Player activeChar, String playername, int id, int count, ItemLocation location)
	{
		Item item = ItemTable.getInstance().getTemplate(id);
		int objectId = IdFactory.getInstance().getNextId();
		
		try (Connection con = ConnectionPool.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("SELECT obj_Id FROM characters WHERE char_name=?");
			statement.setString(1, playername);
			ResultSet result = statement.executeQuery();
			int objId = 0;
			
			if (result.next())
			{
				objId = result.getInt(1);
			}
			
			result.close();
			statement.close();
			
			if (objId == 0)
			{
				activeChar.sendChatMessage(0, Say2.ALL, "SYS", "Char \"" + playername + "\" does not exists!");
				con.close();
				return;
			}
			
			if (item == null)
			{
				activeChar.sendChatMessage(0, Say2.ALL, "SYS", "Unknown Item ID.");
				return;
			}
			
			if (count > 1 && !item.isStackable())
			{
				activeChar.sendChatMessage(0, Say2.ALL, "SYS", "You can't to create more than 1 non stackable items!");
				return;
			}
			
			statement = con.prepareStatement("INSERT INTO items (owner_id,item_id,count,loc,loc_data,enchant_level,object_id,custom_type1,custom_type2,mana_left,time) VALUES (?,?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, objId);
			statement.setInt(2, item.getItemId());
			statement.setInt(3, count);
			statement.setString(4, location.name());
			statement.setInt(5, 0);
			statement.setInt(6, 0);
			statement.setInt(7, objectId);
			statement.setInt(8, 0);
			statement.setInt(9, 0);
			statement.setInt(10, -1);
			statement.setLong(11, 0);
			
			statement.executeUpdate();
			statement.close();
			
			activeChar.sendChatMessage(0, Say2.ALL, "SYS", "Created " + count + " " + item.getName() + " in " + playername + " " + (location == ItemLocation.INVENTORY ? "inventory" : "warehouse") + ".");
			_log.info("Insert item: (" + objId + ", " + item.getName() + ", " + count + ", " + objectId + ")");
		}
		catch (SQLException e)
		{
			_log.log(Level.SEVERE, "Could not insert item " + item.getName() + " into DB: Reason: " + e.getMessage(), e);
		}
	}
	
	private static ItemLocation getItemLocation(String name)
	{
		ItemLocation location = null;
		if (name.equalsIgnoreCase("inventory"))
			location = ItemLocation.INVENTORY;
		else if (name.equalsIgnoreCase("warehouse"))
			location = ItemLocation.WAREHOUSE;
		return location;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
