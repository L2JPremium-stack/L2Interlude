package net.sf.l2j.gameserver.model.actor.instance;
 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ConnectionPool;
import net.sf.l2j.gameserver.datatables.CharNameTable;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.xml.IconTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.instancemanager.custom.AuctionItem;
import net.sf.l2j.gameserver.instancemanager.custom.AuctionTable;
import net.sf.l2j.gameserver.instancemanager.custom.Pagination;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.item.type.EtcItemType;
import net.sf.l2j.gameserver.model.item.type.WeaponType;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
 
public class L2PawnShopInstance extends L2NpcInstance
{
    public L2PawnShopInstance(int objectId, NpcTemplate template)
    {
		super(objectId, template);
	}
   
    @Override
	public void showChatWindow(Player player)
	{
    	onBypassFeedback(player, "auction 1 - *null* - All");
	}
    
    @Override
    public void onBypassFeedback(Player player, String command)
    {
        if (command.startsWith("auction"))
        {
            try
            {
                String[] data = command.substring(8).split(" - ");
                int page = Integer.parseInt(data[0]);
                String search = data[1];
                String Type = data[2];
                showAuction(player, page, search, Type);
            }
            catch (Exception e)
            {
                showChatWindow(player);
                player.sendMessage("Invalid input. Please try again.");
                return;
            }
        }
        else if (command.startsWith("buy"))
        {
            int auctionId = Integer.parseInt(command.substring(4));
            AuctionItem item = AuctionTable.getInstance().getItem(auctionId);
           
            if (item == null)
            {
                showChatWindow(player);
                player.sendMessage("Invalid choice. Please try again.");
                return;
            }
           
            if (player.getInventory().getItemByItemId(item.getCostId()) == null || player.getInventory().getItemByItemId(item.getCostId()).getCount() < item.getCostCount())
            {
                showChatWindow(player);
                player.sendMessage("Incorrect item count.");
                return;
            }
           
            player.destroyItemByItemId("auction", item.getCostId(), item.getCostCount(), this, true);
           
            Player owner = L2World.getInstance().getPlayer(item.getOwnerId());
            if (owner != null && owner.isOnline())
            {
                owner.addItem("auction", item.getCostId(), item.getCostCount(), null, true);
                owner.sendMessage("You have sold an item in the Auction Shop.");
            }
            else
            {
                addItemToOffline(item.getOwnerId(), item.getCostId(), item.getCostCount());
            }
            
         // ItemInstance i = player.getInventory().addItem("auction", item.getItemId(),  item.getCount(), player, this);
            ItemInstance i = player.addItem("auction", item.getItemId(), item.getCount(), this, true);
            i.setEnchantLevel(item.getEnchant());
            player.sendPacket(new InventoryUpdate());
            player.sendMessage("You have purchased an item from the Auction Shop.");
           
            AuctionTable.getInstance().deleteItem(item);
           
            showChatWindow(player);
        }
        else if (command.startsWith("addpanel"))
        {
            int page = Integer.parseInt(command.substring(9));
           
            showAddPanel(player, page);
        }
        else if (command.startsWith("additem"))
        {
            int itemId = Integer.parseInt(command.substring(8));
           
            if (player.getInventory().getItemByObjectId(itemId) == null)
            {
                showChatWindow(player);
                player.sendMessage("Invalid item. Please try again.");
                return;
            }
           
            showAddPanel2(player, itemId);
        }
        else if (command.startsWith("addit2"))
        {
            try
            {
                String[] data = command.substring(7).split(" ");
                int itemId = Integer.parseInt(data[0]);
                String costitemtype = data[1];
                int costCount = Integer.parseInt(data[2]);
                int itemAmount = Integer.parseInt(data[3]);
               
                if (player.getInventory().getItemByObjectId(itemId) == null)
                {
                    showChatWindow(player);
                    player.sendMessage("Invalid item. Please try again.");
                    return;
                }
                if (player.getInventory().getItemByObjectId(itemId).getCount() < itemAmount)
                {
                    showChatWindow(player);
                    player.sendMessage("Invalid item. Please try again.");
                    return;
                }
                if (!player.getInventory().getItemByObjectId(itemId).isTradable())
                {
                    showChatWindow(player);
                    player.sendMessage("Invalid item. Please try again.");
                    return;
                }
               
                int costId = 0;
                if (costitemtype.equals("Ticket"))
                {
                    costId = Config.ITEM_TICKET_SPAWNSHOP;
                }
                if (player.getInventory().getInventoryItemCount(57, -1) < Config.AMOUNT_ADENA_ADD_PAWNSHOP)
    			{
                	showChatWindow(player);
                	player.sendMessage("You do not have enough Adena.");
    				return;
    			}
               
                AuctionTable.getInstance().addItem(new AuctionItem(AuctionTable.getInstance().getNextAuctionId(), player.getObjectId(), player.getInventory().getItemByObjectId(itemId).getItemId(), itemAmount, player.getInventory().getItemByObjectId(itemId).getEnchantLevel(), costId, costCount));
                player.destroyItemByItemId("Consume", 57, Config.AMOUNT_ADENA_ADD_PAWNSHOP, player, true);
                player.destroyItem("auction", itemId, itemAmount, this, true);
                player.sendPacket(new InventoryUpdate());
                player.sendMessage("You have added an item for sale in the Auction Shop.");
                showChatWindow(player);
            }
            catch (Exception e)
            {
                showChatWindow(player);
                player.sendMessage("Invalid input. Please try again.");
                return;
            }
        }
        else if (command.startsWith("myitems"))
        {
            int page = Integer.parseInt(command.substring(8));
            showMyItems(player, page);
        }
        else if (command.startsWith("remove"))
        {
            int auctionId = Integer.parseInt(command.substring(7));
            AuctionItem item = AuctionTable.getInstance().getItem(auctionId);
           
            if (item == null)
            {
                showChatWindow(player);
                player.sendMessage("Invalid choice. Please try again.");
                return;
            }
            if (player.getInventory().getInventoryItemCount(57, -1) < Config.AMOUNT_ADENA_REMOVE_PAWNSHOP)
			{
            	showChatWindow(player);
            	player.sendMessage("You do not have enough Adena.");
				return;
			}
            AuctionTable.getInstance().deleteItem(item);
			
            ItemInstance i = player.getInventory().addItem("auction", item.getItemId(),  item.getCount(), player, this);
            i.setEnchantLevel(item.getEnchant());
            player.sendPacket(new InventoryUpdate());
            player.sendMessage("You have removed an item from the Auction Shop.");
            player.destroyItemByItemId("Consume", 57, Config.AMOUNT_ADENA_REMOVE_PAWNSHOP, player, true);
            showChatWindow(player);
        }
        else
        {
            super.onBypassFeedback(player, command);
        }
    }
   
    private void showMyItems(Player player, int page)
	{
		Map<Integer, List<AuctionItem>> items = new HashMap<>();
		int curr = 1;
		int counter = 0;
		
		List<AuctionItem> temp = new ArrayList<>();
		for (AuctionItem item : AuctionTable.getInstance().getItems())
		{
			if (item.getOwnerId() == player.getObjectId())
			{
				temp.add(item);
				
				counter++;
				
				if (counter == 8)
				{
					items.put(curr, temp);
					temp = new ArrayList<>();
					curr++;
					counter = 0;
				}
			}
		}
        items.put(curr, temp);
       
        if (!items.containsKey(page))
        {
            showChatWindow(player);
            player.sendMessage("Invalid page. Please try again.");
            return;
        }
       
        String html = "";
        html += "<html><title>Auction Shop</title><body><center><br1><br>";
        html += "<font color=00f43f>" +Config.STRING_REMOVE_ITEM_COIN;
        html +="</font><br>";
        html += "<table width=310 >";
        for (AuctionItem item : items.get(page))
        {
        	html += "<tr>";
        	html += "<td><img src=\""+IconTable.getIcon(item.getItemId())+"\" width=32 height=32 align=center></td>";
        	html += "<td><font color=LEVEL>"+(item.getEnchant() > 0 ? "+"+item.getEnchant()+" "+ItemTable.getInstance().getTemplate(item.getItemId()).getName()+" - "+item.getCount() : ItemTable.getInstance().getTemplate(item.getItemId()).getName()+" - "+item.getCount())+"</font>";
        	html += "<br1><font color=00BB77>Cost: "+item.getCostCount()+" "+ItemTable.getInstance().getTemplate(item.getCostId()).getName()+"</font>";
        	html += "</td>";
        	html += "<td fixwidth=49><button value=\"Remove\" action=\"bypass -h npc_"+getObjectId()+"_remove "+item.getAuctionId()+"\" width=49 height=17 back=\"l2ui_ch3.smallbutton1_down_click\" fore=\"l2ui_ch3.smallbutton1\">";
        	html += "</td></tr>";
        }
        html += "</table>";
       
       
        html += "<table><tr>";
        if (items.keySet().size() > 1)
        {

        	if (page > 1)
                html += "<td width=30><a action=\"bypass -h npc_"+getObjectId()+"_myitems "+(page-1)+"\">Prev</a></td>";
        	html += "<td width=10>"+page+"</td>";
            if (items.keySet().size() > page)
                html += "<td width=30><a action=\"bypass -h npc_"+getObjectId()+"_myitems "+(page+1)+"\">Next</a></td>";
        }

        html += "</tr></table>";
        
        html += "</center></body></html>";
       
        NpcHtmlMessage htm = new NpcHtmlMessage(getObjectId());
        htm.setHtml(html);
        player.sendPacket(htm);
    }
   
    private void showAddPanel2(Player player, int itemId)
    {
        ItemInstance item = player.getInventory().getItemByObjectId(itemId);
       
        String html = "";
        html += "<html><title>Auction Shop</title><body><center><br1>";
        html += "Item: "+(item.getEnchantLevel() > 0 ? "+"+item.getEnchantLevel()+" "+item.getName() : item.getName());
       
        if (item.isStackable() && checkItem(item))
        {
            html += "<br>Set amount of items to sell:";
            html += "<edit var=amm type=number width=120 height=17>";
        }
       
        html += "<br>Select price:";
        html += "<br><combobox width=80 height=17 var=ebox list=Ticket;>";
        html += "<br><edit var=count type=number width=120 height=17>";
        html += "<br><button value=\"Add item\" action=\"bypass -h npc_"+getObjectId()+"_addit2 "+itemId+" $ebox $count "+(item.isStackable() ? "$amm" : "1")+"\" width=49 height=16 back=\"l2ui_ch3.smallbutton1_down\"_click\" fore=\"l2ui_ch3.smallbutton1\">";
        html += "</center></body></html>";
       
        NpcHtmlMessage htm = new NpcHtmlMessage(getObjectId());
        htm.setHtml(html);
        player.sendPacket(htm);
    }
   
    private void showAddPanel(Player player, int page)
    {
        HashMap<Integer, ArrayList<ItemInstance>> items = new HashMap<>();
        int curr = 1;
        int counter = 0;
       
        ArrayList<ItemInstance> temp = new ArrayList<>();
        for (ItemInstance item : player.getInventory().getItems())
        {
        	if (item.getItemId() != Config.ITEM_TICKET_SPAWNSHOP && item.isTradable() && checkItem(item) && !checkItemMIST(item))
            {
                temp.add(item);
               
                counter++;
               
                if (counter == 8)
                {
                    items.put(curr, temp);
                    temp = new ArrayList<>();
                    curr++;
                    counter = 0;
                }
            }
        }
        items.put(curr, temp);
       
        if (!items.containsKey(page))
        {
            showChatWindow(player);
            player.sendMessage("Invalid page. Please try again.");
            return;
        }
       
        String html = "";
        html += "<html><title>Auction Shop</title><body><center><br1>";
        html += "Select item:<br1>";
        html += "<font color=00f43f>" +Config.STRING_ADD_ITEM_COIN;
        html +="</font>";

        html += "<br><table width=310 bgcolor=000000>";
       
        for (ItemInstance item : items.get(page))
        {
        	html += "<tr>";
        	html += "<td><img src=\""+IconTable.getIcon(item.getItemId())+"\" width=32 height=32 align=center></td>";
        	html += "<td>"+(item.getEnchantLevel() > 0 ? "+"+item.getEnchantLevel()+" "+ItemTable.getInstance().getTemplate(item.getItemId()).getName()+" - "+item.getCount() : ItemTable.getInstance().getTemplate(item.getItemId()).getName()+" - "+item.getCount());
        	html += "<br></td>";
        	html += "<td fixwidth=71><button value=\"Select\" action=\"bypass -h npc_"+getObjectId()+"_additem "+item.getObjectId()+"\" width=70 height=21 back=\"L2UI.DefaultButton_click\" fore=\"L2UI.DefaultButton\">";
        	html += "</td></tr>";
        }
        html += "</table>";
        html += "<table><tr>";

        if (items.keySet().size() > 1)
        {
            if (page > 1)
                html += "<td width=30><a action=\"bypass -h npc_"+getObjectId()+"_addpanel "+(page-1)+"\">Prev</a></td>";
            html += "<td width=10>"+page+"</td>";
            if (items.keySet().size() > page)
                html += "<td width=30><a action=\"bypass -h npc_"+getObjectId()+"_addpanel "+(page+1)+"\">Next</a></td>";
        }
        html += "</tr></table>";
        html += "</center></body></html>";
       
        NpcHtmlMessage htm = new NpcHtmlMessage(getObjectId());
        htm.setHtml(html);
        player.sendPacket(htm);
    }
   
    	private static void addItemToOffline(int owner_id, int item_id, int count)
    	{
    		Item item = ItemTable.getInstance().getTemplate(item_id);
    		int objectId = IdFactory.getInstance().getNextId();
    		
    		try (Connection con = ConnectionPool.getConnection();
    			PreparedStatement statement = con.prepareStatement("INSERT INTO items (owner_id,item_id,count,loc,loc_data,enchant_level,object_id,custom_type1,custom_type2,mana_left,time) VALUES (?,?,?,?,?,?,?,?,?,?,?)"))
    		{
    			if (count > 1 && !item.isStackable())
    				return;
    			
    			statement.setInt(1, owner_id);
    			statement.setInt(2, item.getItemId());
    			statement.setInt(3, count);
    			statement.setString(4, "INVENTORY");
    			statement.setInt(5, 0);
    			statement.setInt(6, 0);
    			statement.setInt(7, objectId);
    			statement.setInt(8, 0);
    			statement.setInt(9, 0);
    			statement.setInt(10, -1);
    			statement.setLong(11, 0);
    			statement.executeUpdate();
    		}
    		catch (SQLException e)
    		{
    			_log.info("Could not update item char: " + e);
    		}
    	}
    private void showAuction(Player player, int page, String search, String Type)
    {
    	final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
    	//html.setFile(player.isLang() + "mods/pawnshop/buy_list.htm");
    	html.setFile("data/html/mods/pawnshop/buy_list.htm");
        boolean src = !search.equals("*null*");
        
       
        HashMap<Integer, ArrayList<AuctionItem>> items = new HashMap<>();
        
        int curr = 1;
        int counter = 0;
       
        ArrayList<AuctionItem> temp = new ArrayList<>();
        
        final Pagination<AuctionItem> list = new Pagination<>(temp.stream(), page, 5);
        
        for (AuctionItem item : AuctionTable.getInstance().getItems())
        {
        				if (item.getOwnerId() != player.getObjectId() && (!src || (src && ItemTable.getInstance().getTemplate(item.getItemId()).getName().contains(search))))
        				{
        					temp.add(item);
        					
        					counter++;
        					
        					if (counter == 7)
        					{
        						items.put(curr, temp);
        						temp = new ArrayList<>();
        						curr++;
        						counter = 0;
        					}
        				}
        			}
        items.put(curr, temp);
       
        if (!items.containsKey(page))
        {
            showChatWindow(player);
            player.sendMessage("Invalid page. Please try again.");
            return;
        }
        
        list.append("<html><title>Auction Shop</title><body><center><br1>");
//        list.append("<multiedit var=srch width=150 height=20><br1>");
//        list.append("<button value=\"Search\" action=\"bypass -h npc_"+getObjectId()+"_auction 1 - $srch\" width=70 height=21 back=\"L2UI.DefaultButton_click\" fore=\"L2UI.DefaultButton\">");
        list.append("<table width=310>");
        //list.append("<tr><td>Item</td><td>Cost</td><td></td></tr>");
        for (AuctionItem item : items.get(page))
        {
        	boolean isJewelery = false;
        	boolean isArmor = false;
        	switch (ItemTable.getInstance().getTemplate(item.getItemId()).getBodyPart())
        	{
        		case Item.SLOT_NECK:
        		case Item.SLOT_LR_EAR:
        		case Item.SLOT_LR_FINGER:
        			isJewelery = true;
        			break;
        	}
        	switch (ItemTable.getInstance().getTemplate(item.getItemId()).getBodyPart())
        	{
        		case Item.SLOT_CHEST:
        		case Item.SLOT_FEET:
        		case Item.SLOT_FULL_ARMOR:
        		case Item.SLOT_GLOVES:
        		case Item.SLOT_HEAD:
        		case Item.SLOT_LEGS:
        		case Item.TYPE1_SHIELD_ARMOR:
        		case Item.SLOT_L_HAND:
        			isArmor = true;
        			break;
        	}
        	if(Type.equals("Weapon") && !(ItemTable.getInstance().getTemplate(item.getItemId()).getItemType() instanceof WeaponType))
        		continue;
        	if(Type.equals("Armor") && !isArmor)
        		continue;
        	if(	Type.equals("Jewelery") && !isJewelery)
        		continue;
        	//if(Type.equals("Augment") && !(ItemTable.getInstance().getTemplate(item.getItemId()).getItemType() instanceof WeaponType && item.getAugument() != 0))
        	//	continue;
        	
        	list.append("<tr>");
        	list.append("<td width=32 align=right><img src=\""+IconTable.getIcon(item.getItemId())+"\" width=32 height=32 align=center></td>");
        	//list.append("<td>"+(item.getEnchant() > 0 ? "+"+item.getEnchant()+" "+ItemTable.getInstance().getTemplate(item.getItemId()).getName()+" - "+item.getCount() : ItemTable.getInstance().getTemplate(item.getItemId()).getName()+" - "+item.getCount()));
        	//list.append("<br1>Cost: "+item.getCostCount()+" "+ItemTable.getInstance().getTemplate(item.getCostId()).getName());
        	//list.append("</td>");
        	list.append("<td width=5 align=left><a action=\"bypass -h npc_"+getObjectId()+"_buy "+item.getAuctionId()+"\"> "+ItemTable.getInstance().getTemplate(item.getItemId()).getName()+"</a>"+(item.getEnchant() > 0 ? "<font color=LEVEL>+"+item.getEnchant()+"</font>" : " ") +"<br1><font color=fdfffe>Player: "+CharNameTable.getInstance().getNameById(item.getOwnerId())+"</font> <font color=00f43f>Price: "+item.getCostCount()+" "+ItemTable.getInstance().getTemplate(item.getCostId()).getName()+"</font><br1></td><br>><br>");
        	//list.append("</br></br></br></br>");
        	list.append("</tr>");
        }
        list.append("</table>");
        list.append("<img src=\"L2UI.SquareWhite\" width=270 height=1>");
        
        list.append("<table><tr><td></td>");
        
       
        if (items.keySet().size() > 1)
        {
            if (page > 1)
            	list.append("<td><button value=\"Prev\" action=\"bypass -h npc_"+getObjectId()+"_auction "+(page-1)+" - "+search+" - "+Type+"\" width=70 height=21 back=\"sek.cbui94\" fore=\"sek.cbui92\"</td>");
                //list.append("<a action=\"bypass -h npc_"+getObjectId()+"_auction "+(page-1)+" - "+search+" - "+Type+"\"><- Prev</a>");
            list.append("<td>"+page+"</td>");
            if (items.keySet().size() > page)
                list.append("<td><button value=\"Next\" action=\"bypass -h npc_"+getObjectId()+"_auction "+(page+1)+" - "+search+" - "+Type+"\" width=70 height=21 back=\"sek.cbui94\" fore=\"sek.cbui92\"</td>");
            	//list.append("<a action=\"bypass -h npc_"+getObjectId()+"_auction "+(page+1)+" - "+search+" - "+Type+"\">Next -></a>");
        }
        list.append("</tr></table>");
        list.append("</center></body></html>");
       
        //list.generateSpace(20);
		
		html.replace("%list%", list.getContent());
		html.replace("%objectId%", getObjectId());
		player.sendPacket(html);
    }
   
    public boolean checkItem(ItemInstance item)
    {
    	if(Config.ENABLE_GRADE_ITEMS_SPAWNSHOP)
    	{
    		if (!Config.GRADE_ITEMS_PAWNSHOP.contains(item.getItem().getCrystalType()) && !Config.ITEMS_NO_RULES_PAWNSHOP.contains(item.getItemId()))
    		{
    			return false;
    		}
    	}
		return true;
    	
    }
    //FIX ITEMS MIST EM MARKET SHOP!
	  public static boolean checkItemMIST(ItemInstance item)
	  {
		  if(item.getItemType() != EtcItemType.ARROW && (item.getItemType() != EtcItemType.SHOT) && (item.getItemType() != EtcItemType.ELIXIR) && (item.getItemType() != EtcItemType.NONE) && (item.getItemType() != EtcItemType.SCROLL))
		  {
			  return false;
		  }
		  return true;
		  
	  }
}