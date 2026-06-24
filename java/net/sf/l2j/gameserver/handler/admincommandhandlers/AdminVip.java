package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ConnectionPool;
import net.sf.l2j.gameserver.datatables.GmListTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.clientpackets.Say2;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
/**
 * 
 * @author Sarada
 *
 */
public class AdminVip implements IAdminCommandHandler
{
	private static String[] _adminCommands = { "admin_setvip", "admin_removevip" };
	private final static Logger _log = Logger.getLogger(AdminVip.class.getName());
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.startsWith("admin_setvip"))
		{
			StringTokenizer str = new StringTokenizer(command);
			
			L2Object target = activeChar.getTarget();
			Player player = null;
			
			if (target != null && target instanceof Player)
				player = (Player)target;
			else
				player = activeChar;
			
			try
			{
				str.nextToken();
				String time = str.nextToken();
				if (str.hasMoreTokens())
				{
					String playername = time;
					time = str.nextToken();
					player = L2World.getInstance().getPlayer(playername);
					doVip(activeChar, player, playername, time);
				}
				else
				{
					String playername = player.getName();
					doVip(activeChar, player, playername, time);
				}
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Usage: //setvip <char_name> [time](in days)");
			}
			
			player.broadcastUserInfo();
			
			if(player.isVip())
				return false;
		}
		else if(command.startsWith("admin_removevip"))
		{
			StringTokenizer str = new StringTokenizer(command);
			
			L2Object target = activeChar.getTarget();
			Player player = null;
			
			if (target instanceof Player)
				player = (Player)target;
			else
				player = activeChar;
			
			try
			{
				str.nextToken();
				
				if (str.hasMoreTokens())
				{
					String playername = str.nextToken();
					player = L2World.getInstance().getPlayer(playername);
					removeVip(activeChar, player, playername);
				}
				else
				{
					String playername = player.getName();
					removeVip(activeChar, player, playername);
				}
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Usage: //removevip <char_name>");
			}
			
			player.broadcastUserInfo();
			
			if(player.isVip())
				return false;
		}
		return false;
	}
	
	public void doVip(Player activeChar, Player _player, String _playername, String _time)
	{
		int days = Integer.parseInt(_time);
		
		if (_player == null)
		{
			activeChar.sendMessage("Character not found.");
			return;
		}
		if (_player.isVip())
		{
			activeChar.sendMessage("Player " + _playername + " is already an VIP.");
			return;
		}
		
		if(days > 0)
		{
			_player.setVip(true);
			_player.setEndTime("vip", days);
			_player.sendPacket(new CreatureSay(0,Say2.PARTYROOM_COMMANDER,"System","Dear player, you are now an VIP, congratulations."));
			
			try (Connection con = ConnectionPool.getConnection())
			{
				PreparedStatement statement = con.prepareStatement("UPDATE characters SET vip=1, vip_end=? WHERE obj_id=?");
				statement.setLong(1, _player.getVipEndTime());
				statement.setInt(2, _player.getObjectId());
				statement.execute();
				statement.close();
				
				if(Config.ALLOW_VIP_NCOLOR)
					_player.getAppearance().setNameColor(Config.VIP_NCOLOR);
				
				if(Config.ALLOW_VIP_TCOLOR)
					_player.getAppearance().setTitleColor(Config.VIP_TCOLOR);
				
				_player.broadcastUserInfo();
				_player.sendSkillList();
				
				GmListTable.broadcastMessageToGMs("GM "+ activeChar.getName()+ " set an VIP status for player "+ _playername + " for " + _time + " day(s)");
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING,"Something went wrong, check log folder for details", e);
			}
		}
	}
	
	public void removeVip(Player activeChar, Player _player, String _playername)
	{
		if (!_player.isVip())
		{
			activeChar.sendMessage("Player " + _playername + " is not an VIP.");
			return;
		}
		
		_player.setVip(false);
		_player.setVipEndTime(0);
		
		try (Connection con = ConnectionPool.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET Vip=0, Vip_end=0 WHERE obj_id=?");
			statement.setInt(1, _player.getObjectId());
			statement.execute();
			statement.close();
			
			_player.getAppearance().setNameColor(0xFFFF77);
			_player.getAppearance().setTitleColor(0xFFFF77);
			_player.broadcastUserInfo();
			_player.sendSkillList();
			
			GmListTable.broadcastMessageToGMs("GM "+activeChar.getName()+" removed Vip status of player "+ _playername);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING,"Something went wrong, check log folder for details", e);
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return _adminCommands;
	}
}