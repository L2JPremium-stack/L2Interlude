package net.sf.l2j.community.marketplace;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2j.Config;
import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.community.marketplace.HtmlBuilder.HtmlType;
import net.sf.l2j.dailyreward.IBypassHandler;
import net.sf.l2j.gameserver.ConnectionPool;
import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.communitybbs.Manager.BaseBBSManager;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.xml.IconTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.item.type.EtcItemType;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.util.Broadcast;
import net.sf.l2j.gameserver.util.Util;

public class MarketplaceCBBypasses implements IBypassHandler
{
	@Override
	public boolean handleBypass(String bypass, Player activeChar)
	{
		StringTokenizer st = new StringTokenizer(bypass, " ");
		st.nextToken();
	/*	if (bypass.startsWith("bp_showMarketPlace"))
		{
			String[] data = bypass.substring(19).split(" - ");
			int page = Integer.parseInt(data[0]);
			String search = data[1];

			showMarketBoard(activeChar, page, search);
		}	*/
		if (bypass.startsWith("bp_showMarketPlace"))
		{
		    String[] data = bypass.substring(19).split(" - ");
		    
		    // Check if data contains at least two parts (page and search term)
		    if (data.length < 2 || data[0].trim().isEmpty())
		    {
		        // Handle the case where the page number or search term is missing
		        activeChar.sendMessage("Please enter at least one letter in the search.");
		        return false; // Exit early
		    }

		    // Try to parse the page number
		    int page = 0;
		    try 
		    {
		        page = Integer.parseInt(data[0].trim());  // Try parsing the page number
		    }
		    catch (NumberFormatException e)
		    {
		        return false;  // Exit early if the page number is invalid
		    }

		    // Validate the search term (must contain at least one letter)
		    String search = data[1].trim();
		    if (search.isEmpty() || !search.matches(".*[a-zA-Z].*")) 
		    {
		        // If no letters are found in the search term, send a message to the player
		        activeChar.sendMessage("Please enter at least one letter in the search.");
		        return false;
		    }

		    // If everything is valid, proceed to show the market board
		    showMarketBoard(activeChar, page, search);
		}
		else if (bypass.startsWith("bp_showByGrade"))
		{
			String[] grade = bypass.substring(15).split(" - ");
			int pageGrade = Integer.parseInt(grade[0]);
			String searchGrade = grade[1];

			showMarketBoardByGrade(activeChar, pageGrade, searchGrade);
		}
		else if (bypass.startsWith("bp_showByCategory"))
		{
			String[] category = bypass.substring(18).split(" - ");
			int pageCategory = Integer.parseInt(category[0]);
			String searchCategory = category[1];

			showMarketBoardByCategory(activeChar, pageCategory, searchCategory);
		}
		else if (bypass.startsWith("bp_buy"))
		{
			int auctionId = Integer.parseInt(bypass.substring(7));
			AuctionHolder item = AuctionTableCommunity.getInstance().getItem(auctionId);
 
			if (item == null)
			{
				activeChar.sendMessage("Invalid choice. Please try again.");
				return false;
			}
 
			showBuyBoard(activeChar, item);
		}
		
		else if (bypass.startsWith("bp_confirmBuy"))
		{
		    int auctionId = Integer.parseInt(bypass.substring(14));
		    AuctionHolder item = AuctionTableCommunity.getInstance().getItem(auctionId);

		//    System.out.println("bp_confirmBuy: jogador " + activeChar.getName() + " tentou comprar auctionId: " + auctionId);

		    ThreadPool.schedule(() ->
		    {
		        if (item == null)
		        {
		        //    System.out.println("bp_confirmBuy: item null para auctionId: " + auctionId);
		            activeChar.sendMessage("Invalid choice. Please try again.");
		            return;
		        }

		        ItemInstance costItem = activeChar.getInventory().getItemByItemId(item.getCostId());
		        if (costItem == null || costItem.getCount() < item.getCostCount())
		        {
		        //    System.out.println("bp_confirmBuy: jogador " + activeChar.getName() + " não tem itens para pagar. Necessário: " + item.getCostCount() + ", Possui: " + (costItem == null ? 0 : costItem.getCount()));
		            activeChar.sendMessage("Incorrect item count.");
		            showBuyBoard(activeChar, item);
		            return;
		        }

		        // Remove item do comprador (pagamento total)
		        activeChar.destroyItemByItemId("auction", item.getCostId(), item.getCostCount(), activeChar, true);
		   //     System.out.println("bp_confirmBuy: item removido do comprador: " + item.getCostCount() + " x " + item.getCostId());

		        long cost = item.getCostCount();
		        long tax = 0;
		        if (cost >= Config.AUCTION_TAX_MINIMUM_ITEM_COUNT)
		            tax = (long) Math.ceil(cost * Config.AUCTION_TAX_RATE);

		        long finalAmount = Math.max(0, cost - tax);

		   //     System.out.println("bp_confirmBuy: custo total: " + cost + ", taxa: " + tax + ", valor líquido para vendedor: " + finalAmount);

		        Player owner = L2World.getInstance().getPlayer(item.getOwnerId());
		        if (owner != null && owner.isOnline())
		        {
		            owner.addItem("Auction", item.getCostId(), (int) finalAmount, null, true);
		            owner.sendMessage("You have sold an item in the marketplace.");
		            if (tax > 0)
		                owner.sendMessage("Marketplace fee: " + tax + " was deducted from your sale.");
		         //   System.out.println("bp_confirmBuy: pagamento enviado para vendedor online: " + owner.getName());
		        }
		        else
		        {
		            addItemToOffline(item.getOwnerId(), item.getCostId(), (int) finalAmount);
		       //     System.out.println("bp_confirmBuy: pagamento enviado para vendedor offline id: " + item.getOwnerId());
		        }

		        // Dá o item comprado ao comprador
		        ItemInstance i = activeChar.addItem("auction", item.getItemId(), item.getCount(), activeChar, true);
		        i.setEnchantLevel(item.getEnchant());

		        activeChar.sendPacket(new InventoryUpdate());
		        activeChar.sendMessage("You have purchased an item from the marketplace.");
		   //     System.out.println("bp_confirmBuy: item entregue para comprador: " + activeChar.getName());

		        // Logs, broadcast, etc
		        MarketBuy.Log("Market", activeChar.getName(), ItemTable.getInstance().getTemplate(item.getItemId()).getName(), item.getEnchant(), item.getCount());

		        if (item.getEnchant() >= 1)
		            Broadcast.gameAnnounceAuctionShop(activeChar.getName() + " purchased " + ItemTable.getInstance().getTemplate(item.getItemId()).getName() + " +" + item.getEnchant() + " on Auction.");
		        else
		            Broadcast.gameAnnounceAuctionShop(activeChar.getName() + " purchased " + ItemTable.getInstance().getTemplate(item.getItemId()).getName() + " on Auction.");

		        AuctionTableCommunity.getInstance().deleteItem(item);
		        showMarketBoard(activeChar, 1, "*null*");

		   //     System.out.println("bp_confirmBuy: compra finalizada para jogador " + activeChar.getName());
		    }, Rnd.get(100, 500));
		}
		else if (bypass.startsWith("bp_addpanel"))
		{
			int page = Integer.parseInt(bypass.substring(12));
 
			showAddBoard(activeChar, page);
		}
		else if (bypass.startsWith("bp_additem"))
		{
			int itemId = Integer.parseInt(bypass.substring(11));
 
			if (activeChar.getInventory().getItemByObjectId(itemId) == null)
			{
				activeChar.sendMessage("Invalid item. Please try again.");
				return false;
			}
 
			showAddBoard2(activeChar, itemId);
		}
		else if (bypass.startsWith("bp_addit2"))
		{
			try
			{
				String[] data = bypass.substring(10).split(" ");
				int itemId = Integer.parseInt(data[0]);
				String costitemtype = data[1];
				int costCount = Integer.parseInt(data[2]);
				int itemAmount = Integer.parseInt(data[3]);
				int feeId = Config.MARKETPLACE_FEE[0];
				int feeAmount = Config.MARKETPLACE_FEE[1];
				/*
	        	if ((activeChar.getMarketPlaceTimer() - 1500) > System.currentTimeMillis() && Config.MIN_PLAY_TIME_TO_USE_MARKETPLACE)
				{
					if (((activeChar.getMarketPlaceTimer() - System.currentTimeMillis()) / 1000) >= 60)
					{
						activeChar.sendMessage("You need " + Config.PLAY_TIME_TO_USE_MARKETPLACE + " minutes online to use marketplace. Finish in " + (activeChar.getMarketPlaceTimer() - System.currentTimeMillis()) / (1000 * 60) + " minute(s).");
						activeChar.sendPacket(new ExShowScreenMessage("You can use marketplace in " + (activeChar.getMarketPlaceTimer() - System.currentTimeMillis()) / (1000 * 60) + " minute(s)", 5 * 1000));
					}
					else
					{
						activeChar.sendMessage("You need " + Config.PLAY_TIME_TO_USE_MARKETPLACE + " minutes online to use marketplace. Finish in " + (activeChar.getMarketPlaceTimer() - System.currentTimeMillis()) / 1000 + " second(s).");
						activeChar.sendPacket(new ExShowScreenMessage("You can use marketplace in " + (activeChar.getMarketPlaceTimer() - System.currentTimeMillis()) / 1000 + " second(s)", 5 * 1000));
					}
					return false;
				}
				if (activeChar.getLevel() <= Config.MIN_LEVEL_TO_SELL_ON_MARKETPLACE)
				{
					activeChar.sendMessage("You need to be at least " + Config.MIN_LEVEL_TO_SELL_ON_MARKETPLACE + " level in order to use marketplace.");
					return false;
				}
				*/
				if (costCount < 1 || itemAmount < 1)
				{
					activeChar.sendMessage("[Protect] Minimum quantity: 1");
					return false;
				}
				
				if (activeChar.getInventory().getInventoryItemCount(feeId, -1) < feeAmount)
				{
					activeChar.sendMessage("You don't have " + StringUtil.concat(String.valueOf(feeAmount)) + " - " + ItemTable.getInstance().getTemplate(feeId).getName() +" to pay the fee.");
					return false;
				}
				if (activeChar.getInventory().getItemByObjectId(itemId) == null)
				{
					activeChar.sendMessage("Invalid item. Please try again.");
					return false;
				}
				if (activeChar.getInventory().getItemByObjectId(itemId).getCount() < itemAmount)
				{
					activeChar.sendMessage("Invalid item. Please try again.");
					return false;
				}
				if (!activeChar.getInventory().getItemByObjectId(itemId).isTradable())
				{
					activeChar.sendMessage("Invalid item. Please try again.");
					return false;
				}
 
				int costId = 0;
				if (costitemtype.equals("Donate"))
				{
					costId = Config.TICKET_MARKE_PLACE;
				}
			
				// Warning
				//MarketSell.Log(activeChar.getName(), activeChar.getInventory().getItemByObjectId(itemId).getItemName(), activeChar.getInventory().getItemByObjectId(itemId).getEnchantLevel(), activeChar.getInventory().getItemByObjectId(itemId).getCount(), activeChar.getInventory().getItemByObjectId(itemId).getObjectId());
				//AdminData.getInstance().broadcastMessageToGMs("[WARN]" + activeChar.getName() + " ADD [" + activeChar.getInventory().getItemByObjectId(itemId).getItemName() + "] COUNT [" + activeChar.getInventory().getItemByObjectId(itemId).getCount() + "]");
			//	sendPacket(new ExShowScreenMessage(message, duration * 1000))
				if (activeChar.getInventory().getItemByObjectId(itemId).getEnchantLevel() >= 1)
				{
				//	 for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
				//	 {
				//		 player.sendPacket(new ExShowScreenMessage("" + activeChar.getName() + " added " + " " + activeChar.getInventory().getItemByObjectId(itemId).getItemName() + " +" + activeChar.getInventory().getItemByObjectId(itemId).getEnchantLevel() + " on Auction.", 5000));
				//	 }
					Broadcast.gameAnnounceAuctionShop("" + activeChar.getName() + " added " + " " + activeChar.getInventory().getItemByObjectId(itemId).getItemName() + " +" + activeChar.getInventory().getItemByObjectId(itemId).getEnchantLevel() + " on Auction.");
					
				}
				else
				{
				//	 for (L2PcInstance player : L2World.getInstance().getAllPlayers().values())
				//	 {
				//		 player.sendPacket(new ExShowScreenMessage("" + activeChar.getName() + " added " + " " + activeChar.getInventory().getItemByObjectId(itemId).getItemName() + " on Auction.", 5000));	 
				//	 }
					Broadcast.gameAnnounceAuctionShop("" + activeChar.getName() + " added " + " " + activeChar.getInventory().getItemByObjectId(itemId).getItemName() + " on Auction.");
				
				}

				// Auction
				AuctionTableCommunity.getInstance().addItem(new AuctionHolder(AuctionTableCommunity.getInstance().getNextAuctionId(), activeChar.getObjectId(), activeChar.getInventory().getItemByObjectId(itemId).getItemId(), itemAmount, activeChar.getInventory().getItemByObjectId(itemId).getEnchantLevel(), costId, costCount));
 
				activeChar.destroyItemByItemId("Auction Fee", feeId, feeAmount, activeChar, true);
				activeChar.destroyItem("Auction Item", itemId, itemAmount, activeChar, true);
				activeChar.sendPacket(new InventoryUpdate());
				activeChar.sendMessage("You have added an item for sale in the Marketplace.");
				showAddBoard(activeChar, 1);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Invalid input. Please try again.");
				return false;
			}
		}
		else if (bypass.startsWith("bp_myitems"))
		{
			int page = Integer.parseInt(bypass.substring(11));
			showMyItemsBoard(activeChar, page);
		}
		else if (bypass.startsWith("bp_removeMarket"))
		{
			int auctionId = Integer.parseInt(bypass.substring(16));
			AuctionHolder item = AuctionTableCommunity.getInstance().getItem(auctionId);
 
			if (item == null)
			{
				activeChar.sendMessage("Invalid choice. Please try again.");
				return false;
			}
			
		    if (item.getOwnerId() != activeChar.getObjectId())
		    {
		        activeChar.sendMessage("Illegal action detected, you will be sent to jail.");
				Util.handleIllegalPlayerAction(activeChar, "Auction: player [" + activeChar.getName() + "] trying to remove market exploit.", 4);
		        return false;
		    }
		    
			AuctionTableCommunity.getInstance().deleteItem(item);
 
			ItemInstance i = activeChar.addItem("auction", item.getItemId(), item.getCount(), activeChar, true);
			i.setEnchantLevel(item.getEnchant());
			activeChar.sendPacket(new InventoryUpdate());
			activeChar.sendMessage("You have removed an item from the Auction.");
			showMyItemsBoard(activeChar, 1);
		}
		return false;
	}
	  public static boolean checkItem(ItemInstance item)
	    {
	    	if(Config.ENABLE_GRADE_ITEMS_MARKETPLACE)
	    	{
	    	//	if (!Config.GRADE_ITEMS_PAWNSHOP.contains(item.getItem().getCrystalType()) && !Config.ITEMS_NO_RULES_PAWNSHOP.contains(item.getItemId()) || !(item.getItemType() != EtcItemType.ARROW)|| !(item.getItemType() != EtcItemType.SHOT) || !(item.getItemType() != EtcItemType.NONE))
	    		if (!Config.GRADE_ITEMS_MARKETPLACE.contains(item.getItem().getCrystalType()) && !Config.ITEMS_NO_RULES_MARKETPLACE.contains(item.getItemId()))
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
	  
	public static void showMarketBoard(Player player, int page, String search)
	{
		boolean src = !search.equals("*null*");
		 
		HashMap<Integer, ArrayList<AuctionHolder>> items = new HashMap<>();
		int curr = 1;
		int counter = 0;
 
		ArrayList<AuctionHolder> temp = new ArrayList<>();
		for (Map.Entry<Integer, AuctionHolder> entry : AuctionTableCommunity.getInstance().getItems().entrySet())
		{
			if (entry.getValue().getOwnerId() != player.getObjectId() && (!src || (src && ItemTable.getInstance().getTemplate(entry.getValue().getItemId()).getName().contains(search))))
			{
				temp.add(entry.getValue());
 
				counter++;
 
				if (counter == 9)
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
			player.sendMessage("Invalid page. Please try again.");
			return;
		}
 
		HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY);
		hb.append("<html><body>");
		hb.append("<br><br>");
		hb.append("<center><table><tr>");
		hb.append("<td><button value=\"Auction List\" action=\"bypass bp_showMarketPlace 1 - *null*\" width=114 height=31 back=\"L2UI_CH3.bigbutton2_over\" fore=\"L2UI_CH3.bigbutton2\"></td>");
		hb.append("<td><button value=\"Create Auction\" action=\"bypass bp_addpanel 1\" width=114 height=31 back=\"L2UI_CH3.bigbutton2_over\" fore=\"L2UI_CH3.bigbutton2\"></td>");
		hb.append("<td><button value=\"My Auction\" action=\"bypass bp_myitems 1\" width=114 height=31 back=\"L2UI_CH3.bigbutton2_over\" fore=\"L2UI_CH3.bigbutton2\"></td>");
		hb.append("</tr></table>");
		hb.append("<br>");
		hb.append("<table width=570><tr><td>");
		hb.append("<img src=L2UI.SquareGray width=620 height=1>");
		hb.append("<table bgcolor=000000><tr><td>");
		
		hb.append("<table width=130 height=230><tr><td>");
		hb.append("<table width=130><tr><td><center><font color=F28034>Item Name:</font></center></td></tr></table></td></tr>");
		hb.append("<tr><td><table width=130><tr><td><center><edit var=srch width=110 height=13></center></td></tr></table></td></tr>");
		hb.append("<tr><td><table width=130><tr><td align=center><button value=\"Search\" action=\"bypass bp_showMarketPlace 1 - $srch\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td></tr></table></td></tr>");
		
		hb.append("<tr><td height=5></td></tr>");
		hb.append("<tr><td><table width=130><tr><td><center><font color=F28034>Grade:</font></center></td></tr></table></td></tr>");
		hb.append("<tr><td><table width=130><tr><td align=center><combobox width=110 height=15 var=gde list=NONE;D;C;B;A;S;></td></tr></table></td></tr>");
		hb.append("<tr><td><table width=130><tr><td align=center><button value=\"APPLY\" action=\"bypass bp_showByGrade 1 - $gde\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td></tr></table></td></tr>");
		
		hb.append("<tr><td height=5></td></tr>");
		hb.append("<tr><td><table width=130><tr><td><center><font color=F28034>Category:</font></center></td></tr></table></td></tr>");
		hb.append("<tr><td><table width=130><tr><td align=center><combobox width=110 height=15 var=ctg list=ALL;WEAPON;ARMOR;JEWELS;ACCESSORY;MISC;SCROLL;></td></tr></table></td></tr>");
		hb.append("<tr><td><table width=130><tr><td align=center><button value=\"APPLY\" action=\"bypass bp_showByCategory 1 - $ctg\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td></tr></table></td></tr>");
		hb.append("</table><br></td>");
		
		hb.append("<td>");
		hb.append("<img src=L2UI.SquareGray width=500 height=1>");
		hb.append("<table width=480 height=340 bgcolor=000000>");
		hb.append("<tr><td><center>");
		hb.append("<table width=380><tr><td>");
		hb.append("<table width=500>");
		hb.append("<tr>");
		hb.append("<td width=40><font color=F28034>Item</font></td>");
		hb.append("<td width=230></td>");
		hb.append("<td width=40 align=center><font color=F28034>Grade</font></td>");
		hb.append("<td width=80 align=center><font color=F28034>Count</font></td>");
		hb.append("<td width=80 align=right><font color=F28034>Sale Price</font></td>");
		hb.append("<td width=40></td>");
		hb.append("</tr>");
		
		for (AuctionHolder item : items.get(page))
		{
			String gradeIcon = "N/A";
			
			switch (ItemTable.getInstance().getTemplate(item.getItemId()).getCrystalType())
			{
				default:
				case NONE:
					gradeIcon = "N/A";
					break; 
				case D:
					gradeIcon = "<img src=symbol.grade_d width=16 height=16>";
					break;
				case C:
					gradeIcon = "<img src=symbol.grade_c width=16 height=16>";
					break;
				case B:
					gradeIcon = "<img src=symbol.grade_b width=16 height=16>";
					break;
				case A:
					gradeIcon = "<img src=symbol.grade_a width=16 height=16>";
					break;
				case S:
					gradeIcon = "<img src=symbol.grade_s width=16 height=16>";
					break;
			}
			
			String priceIcon = "N/A";
			
			switch (ItemTable.getInstance().getTemplate(item.getCostId()).getItemId())
			{
			    default:
			    case 0:
				    priceIcon = "N/A";
				    break; 
			    case 57:
			    	priceIcon = "<img src=alexarnhold.adenaauction width=16 height=16>";
				    break;
			    case 9511:
			    	priceIcon = "<img src=alexarnhold.donatecoinauction width=16 height=16>";
				    break;
			}
			
			hb.append("<tr>");
			hb.append("<td width=32 height=37><img src=\""+IconTable.getIcon(item.getItemId())+"\" width=32 height=32></td>");
			
			if (item.getEnchant() > 0)
				hb.append("<td width=230 height=25><a action=\"bypass bp_buy "+item.getAuctionId()+"\">"+ItemTable.getInstance().getTemplate(item.getItemId()).getName()+" +"+item.getEnchant()+"</a></td>");
			else
				hb.append("<td width=230 height=25><a action=\"bypass bp_buy "+item.getAuctionId()+"\">"+ItemTable.getInstance().getTemplate(item.getItemId()).getName()+"</a></td>");
			
			hb.append("<td width=40 height=25 align=center>"+gradeIcon+"</td>");
			hb.append("<td width=80 height=20 align=center>"+item.getCount()+"</td>");
			hb.append("<td width=80 align=right><font color=LEVEL>"+StringUtil.formatNumber(item.getCostCount())+"</font></td><br1>");
			hb.append("<td width=40 height=25 align=left><font color=LEVEL>"+priceIcon+"</font></td><br1>");
			hb.append("</tr>");
		}
		
		hb.append("</table>");
		hb.append("</td></tr></table>");
		hb.append("</center></td></tr>");
		hb.append("</table>");
		hb.append("<img src=L2UI.SquareGray width=500 height=1>");
		hb.append("<table width=300>");
		hb.append("<tr>");
		if (items.keySet().size() > 1)
		{
			hb.append("<td width=100></td>");
			if (page > 1)
				hb.append("<td width=30 align=center valign=center><button value=\"PREV\" action=\"bypass bp_showMarketPlace " + (page-1) + " - " + search + "\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td>");

			hb.append("<td width=30 align=center valign=center>Page 1</td>");
			
			if (items.keySet().size() > page)
				hb.append("<td width=30 align=center valign=center><button value=\"NEXT\" action=\"bypass bp_showMarketPlace " + (page+1) + " - " + search + "\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td>");
		}
		hb.append("</tr>");
		hb.append("</table>");
		hb.append("</td>");
		hb.append("</tr></table><img src=L2UI.SquareGray width=620 height=1></td></tr></table>");
		hb.append("<br>");
		
		hb.append("</center></body></html>");
		BaseBBSManager.separateAndSend(hb.toString(), player);
	}
	
	private static void showMarketBoardByGrade(Player player, int page, String search)
	{
		boolean src = !search.equals("*null*");
		 
		HashMap<Integer, ArrayList<AuctionHolder>> items = new HashMap<>();
		int curr = 1;
		int counter = 0;
 
		ArrayList<AuctionHolder> temp = new ArrayList<>();
		for (Map.Entry<Integer, AuctionHolder> entry : AuctionTableCommunity.getInstance().getItems().entrySet())
		{
			if (entry.getValue().getOwnerId() != player.getObjectId() && (!src || (src && ItemTable.getInstance().getTemplate(entry.getValue().getItemId()).getCrystalType().toString().equals(search))))
			{
				temp.add(entry.getValue());
 
				counter++;
 
				if (counter == 9)
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
			player.sendMessage("Invalid page. Please try again.");
			return;
		}
 
		HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY);
		hb.append("<html><body>");
		hb.append("<br><br>");
		hb.append("<center><table><tr>");
		hb.append("<td><button value=\"Auction List\" action=\"bypass bp_showMarketPlace 1 - *null*\" width=114 height=31 back=\"L2UI_CH3.bigbutton2_over\" fore=\"L2UI_CH3.bigbutton2\"></td>");
		hb.append("<td><button value=\"Create Auction\" action=\"bypass bp_addpanel 1\" width=114 height=31 back=\"L2UI_CH3.bigbutton2_over\" fore=\"L2UI_CH3.bigbutton2\"></td>");
		hb.append("<td><button value=\"My Auction\" action=\"bypass bp_myitems 1\" width=114 height=31 back=\"L2UI_CH3.bigbutton2_over\" fore=\"L2UI_CH3.bigbutton2\"></td>");
		hb.append("</tr></table>");
		hb.append("<br>");
		hb.append("<table width=570><tr><td>");
		hb.append("<img src=L2UI.SquareGray width=620 height=1>");
		hb.append("<table bgcolor=000000><tr><td>");
		
		hb.append("<table width=130 height=230><tr><td>");
		hb.append("<table width=130><tr><td><center><font color=F28034>Item Name:</font></center></td></tr></table></td></tr>");
		hb.append("<tr><td><table width=130><tr><td><center><edit var=srch width=110 height=13></center></td></tr></table></td></tr>");
		hb.append("<tr><td><table width=130><tr><td align=center><button value=\"Search\" action=\"bypass bp_showMarketPlace 1 - $srch\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td></tr></table></td></tr>");
		
		hb.append("<tr><td height=5></td></tr>");
		hb.append("<tr><td><table width=130><tr><td><center><font color=F28034>Grade:</font></center></td></tr></table></td></tr>");
		hb.append("<tr><td><table width=130><tr><td align=center><combobox width=110 height=15 var=gde list=NONE;D;C;B;A;S;></td></tr></table></td></tr>");
		hb.append("<tr><td><table width=130><tr><td align=center><button value=\"APPLY\" action=\"bypass bp_showByGrade 1 - $gde\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td></tr></table></td></tr>");
		
		hb.append("<tr><td height=5></td></tr>");
		hb.append("<tr><td><table width=130><tr><td><center><font color=F28034>Category:</font></center></td></tr></table></td></tr>");
		hb.append("<tr><td><table width=130><tr><td align=center><combobox width=110 height=15 var=ctg list=ALL;WEAPON;ARMOR;JEWELS;ACCESSORY;MISC;SCROLL;></td></tr></table></td></tr>");
		hb.append("<tr><td><table width=130><tr><td align=center><button value=\"APPLY\" action=\"bypass bp_showByCategory 1 - $ctg\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td></tr></table></td></tr>");
		hb.append("</table><br></td>");
		
		hb.append("<td>");
		hb.append("<img src=L2UI.SquareGray width=500 height=1>");
		hb.append("<table width=480 height=340 bgcolor=000000>");
		hb.append("<tr><td><center>");
		hb.append("<table width=380><tr><td>");
		hb.append("<table width=500>");
		hb.append("<tr>");
		hb.append("<td width=40><font color=F28034>Item</font></td>");
		hb.append("<td width=230></td>");
		hb.append("<td width=40 align=center><font color=F28034>Grade</font></td>");
		hb.append("<td width=80 align=center><font color=F28034>Count</font></td>");
		hb.append("<td width=80 align=right><font color=F28034>Sale Price</font></td>");
		hb.append("<td width=40></td>");
		hb.append("</tr>");
		
		for (AuctionHolder item : items.get(page))
		{
			String gradeIcon = "N/A";
			
			switch (ItemTable.getInstance().getTemplate(item.getItemId()).getCrystalType())
			{
				default:
				case NONE:
					gradeIcon = "N/A";
					break; 
				case D:
					gradeIcon = "<img src=symbol.grade_d width=16 height=16>";
					break;
				case C:
					gradeIcon = "<img src=symbol.grade_c width=16 height=16>";
					break;
				case B:
					gradeIcon = "<img src=symbol.grade_b width=16 height=16>";
					break;
				case A:
					gradeIcon = "<img src=symbol.grade_a width=16 height=16>";
					break;
				case S:
					gradeIcon = "<img src=symbol.grade_s width=16 height=16>";
					break;
			}
			
			String priceIcon = "N/A";
			
			switch (ItemTable.getInstance().getTemplate(item.getCostId()).getItemId())
			{
			    default:
			    case 0:
				    priceIcon = "N/A";
				    break; 
			    case 57:
			    	priceIcon = "<img src=alexarnhold.adenaauction width=16 height=16>";
				    break;
			    case 9511:
			    	priceIcon = "<img src=alexarnhold.donatecoinauction width=16 height=16>";
				    break;
			}
			
			hb.append("<tr>");
			hb.append("<td width=32 height=37><img src=\""+IconTable.getIcon(item.getItemId())+"\" width=32 height=32></td>");
			
			if (item.getEnchant() > 0)
				hb.append("<td width=230 height=25><a action=\"bypass bp_buy "+item.getAuctionId()+"\">"+ItemTable.getInstance().getTemplate(item.getItemId()).getName()+" +"+item.getEnchant()+"</a></td>");
			else
				hb.append("<td width=230 height=25><a action=\"bypass bp_buy "+item.getAuctionId()+"\">"+ItemTable.getInstance().getTemplate(item.getItemId()).getName()+"</a></td>");
			
			hb.append("<td width=40 height=25 align=center>"+gradeIcon+"</td>");
			hb.append("<td width=80 height=20 align=center>"+item.getCount()+"</td>");
			hb.append("<td width=80 align=right><font color=LEVEL>"+StringUtil.formatNumber(item.getCostCount())+"</font></td><br1>");
			hb.append("<td width=40 height=25 align=left><font color=LEVEL>"+priceIcon+"</font></td><br1>");
			hb.append("</tr>");
		}
		
		hb.append("</table>");
		hb.append("</td></tr></table>");
		hb.append("</center></td></tr>");
		hb.append("</table>");
		hb.append("<img src=L2UI.SquareGray width=580 height=1>");
		hb.append("<table width=300>");
		hb.append("<tr>");
		if (items.keySet().size() > 1)
		{
			hb.append("<td width=100></td>");
			if (page > 1)
				hb.append("<td width=30 align=center valign=center><button value=\"PREV\" action=\"bypass bp_showByGrade " + (page-1) + " - " + search + "\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td>");

			hb.append("<td width=30 align=center valign=center>Page 1</td>");
			
			if (items.keySet().size() > page)
				hb.append("<td width=30 align=center valign=center><button value=\"NEXT\" action=\"bypass bp_showByGrade " + (page+1) + " - " + search + "\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td>");
		}
		hb.append("</tr>");
		hb.append("</table>");
		hb.append("</td>");
		hb.append("</tr></table><img src=L2UI.SquareGray width=620 height=1></td></tr></table>");
		hb.append("<br>");
		
		hb.append("</center></body></html>");
		BaseBBSManager.separateAndSend(hb.toString(), player);
	}
	
	private static void showMarketBoardByCategory(Player player, int page, String search)
	{
		boolean src = !search.equals("*null*");
		 
		HashMap<Integer, ArrayList<AuctionHolder>> items = new HashMap<>();
		int curr = 1;
		int counter = 0;
 
		ArrayList<AuctionHolder> temp = new ArrayList<>();
		for (Map.Entry<Integer, AuctionHolder> entry : AuctionTableCommunity.getInstance().getItems().entrySet())
		{
			if (entry.getValue().getOwnerId() != player.getObjectId() && (!src || (src && ItemTable.getInstance().getTemplate(entry.getValue().getItemId()).searchItemByType(search))))
			{
				temp.add(entry.getValue());
 
				counter++;
 
				if (counter == 9)
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
			player.sendMessage("Invalid page. Please try again.");
			return;
		}
 
		HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY);
		hb.append("<html><body>");
		hb.append("<br><br>");
		hb.append("<center><table><tr>");
		hb.append("<td><button value=\"Auction List\" action=\"bypass bp_showMarketPlace 1 - *null*\" width=114 height=31 back=\"L2UI_CH3.bigbutton2_over\" fore=\"L2UI_CH3.bigbutton2\"></td>");
		hb.append("<td><button value=\"Create Auction\" action=\"bypass bp_addpanel 1\" width=114 height=31 back=\"L2UI_CH3.bigbutton2_over\" fore=\"L2UI_CH3.bigbutton2\"></td>");
		hb.append("<td><button value=\"My Auction\" action=\"bypass bp_myitems 1\" width=114 height=31 back=\"L2UI_CH3.bigbutton2_over\" fore=\"L2UI_CH3.bigbutton2\"></td>");
		hb.append("</tr></table>");
		hb.append("<br>");
		hb.append("<table width=570><tr><td>");
		hb.append("<img src=L2UI.SquareGray width=620 height=1>");
		hb.append("<table bgcolor=000000><tr><td>");
		
		hb.append("<table width=130 height=230><tr><td>");
		hb.append("<table width=130><tr><td><center><font color=F28034>Item Name:</font></center></td></tr></table></td></tr>");
		hb.append("<tr><td><table width=130><tr><td><center><edit var=srch width=110 height=13></center></td></tr></table></td></tr>");
		hb.append("<tr><td><table width=130><tr><td align=center><button value=\"Search\" action=\"bypass bp_showMarketPlace 1 - $srch\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td></tr></table></td></tr>");
		
		hb.append("<tr><td height=5></td></tr>");
		hb.append("<tr><td><table width=130><tr><td><center><font color=F28034>Grade:</font></center></td></tr></table></td></tr>");
		hb.append("<tr><td><table width=130><tr><td align=center><combobox width=110 height=15 var=gde list=NONE;D;C;B;A;S;></td></tr></table></td></tr>");
		hb.append("<tr><td><table width=130><tr><td align=center><button value=\"APPLY\" action=\"bypass bp_showByGrade 1 - $gde\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td></tr></table></td></tr>");
		
		hb.append("<tr><td height=5></td></tr>");
		hb.append("<tr><td><table width=130><tr><td><center><font color=F28034>Category:</font></center></td></tr></table></td></tr>");
		hb.append("<tr><td><table width=130><tr><td align=center><combobox width=110 height=15 var=ctg list=ALL;WEAPON;ARMOR;JEWELS;ACCESSORY;MISC;SCROLL;></td></tr></table></td></tr>");
		hb.append("<tr><td><table width=130><tr><td align=center><button value=\"APPLY\" action=\"bypass bp_showByCategory 1 - $ctg\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td></tr></table></td></tr>");
		hb.append("</table><br></td>");
		
		hb.append("<td>");
		hb.append("<img src=L2UI.SquareGray width=500 height=1>");
		hb.append("<table width=480 height=340 bgcolor=000000>");
		hb.append("<tr><td><center>");
		hb.append("<table width=380><tr><td>");
		hb.append("<table width=500>");
		hb.append("<tr>");
		hb.append("<td width=40><font color=F28034>Item</font></td>");
		hb.append("<td width=230></td>");
		hb.append("<td width=40 align=center><font color=F28034>Grade</font></td>");
		hb.append("<td width=80 align=center><font color=F28034>Count</font></td>");
		hb.append("<td width=80 align=right><font color=F28034>Sale Price</font></td>");
		hb.append("<td width=40></td>");
		hb.append("</tr>");
		
		for (AuctionHolder item : items.get(page))
		{
			String gradeIcon = "N/A";
			
			switch (ItemTable.getInstance().getTemplate(item.getItemId()).getCrystalType())
			{
				default:
				case NONE:
					gradeIcon = "N/A";
					break; 
				case D:
					gradeIcon = "<img src=symbol.grade_d width=16 height=16>";
					break;
				case C:
					gradeIcon = "<img src=symbol.grade_c width=16 height=16>";
					break;
				case B:
					gradeIcon = "<img src=symbol.grade_b width=16 height=16>";
					break;
				case A:
					gradeIcon = "<img src=symbol.grade_a width=16 height=16>";
					break;
				case S:
					gradeIcon = "<img src=symbol.grade_s width=16 height=16>";
					break;
			}
			
			String priceIcon = "N/A";
			
			switch (ItemTable.getInstance().getTemplate(item.getCostId()).getItemId())
			{
			    default:
			    case 0:
				    priceIcon = "N/A";
				    break; 
			    case 57:
			    	priceIcon = "<img src=alexarnhold.adenaauction width=16 height=16>";
				    break;
			    case 9511:
			    	priceIcon = "<img src=alexarnhold.donatecoinauction width=16 height=16>";
				    break;
			}
			
			hb.append("<tr>");
			hb.append("<td width=32 height=37><img src=\""+IconTable.getIcon(item.getItemId())+"\" width=32 height=32></td>");
			
			if (item.getEnchant() > 0)
				hb.append("<td width=230 height=25><a action=\"bypass bp_buy "+item.getAuctionId()+"\">"+ItemTable.getInstance().getTemplate(item.getItemId()).getName()+" +"+item.getEnchant()+"</a></td>");
			else
				hb.append("<td width=230 height=25><a action=\"bypass bp_buy "+item.getAuctionId()+"\">"+ItemTable.getInstance().getTemplate(item.getItemId()).getName()+"</a></td>");
			
			hb.append("<td width=40 height=25 align=center>"+gradeIcon+"</td>");
			hb.append("<td width=80 height=20 align=center>"+item.getCount()+"</td>");
			hb.append("<td width=80 align=right><font color=LEVEL>"+StringUtil.formatNumber(item.getCostCount())+"</font></td><br1>");
			hb.append("<td width=40 height=25 align=left><font color=LEVEL>"+priceIcon+"</font></td><br1>");
			hb.append("</tr>");
		}
		
		hb.append("</table>");
		hb.append("</td></tr></table>");
		hb.append("</center></td></tr>");
		hb.append("</table>");
		hb.append("<img src=L2UI.SquareGray width=580 height=1>");
		hb.append("<table width=300>");
		hb.append("<tr>");
		if (items.keySet().size() > 1)
		{
			hb.append("<td width=100></td>");
			if (page > 1)
				hb.append("<td width=30 align=center valign=center><button value=\"PREV\" action=\"bypass bp_showByCategory " + (page-1) + " - " + search + "\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td>");

			hb.append("<td width=30 align=center valign=center>Page 1</td>");
			
			if (items.keySet().size() > page)
				hb.append("<td width=30 align=center valign=center><button value=\"NEXT\" action=\"bypass bp_showByCategory " + (page+1) + " - " + search + "\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td>");
		}
		hb.append("</tr>");
		hb.append("</table>");
		hb.append("</td>");
		hb.append("</tr></table><img src=L2UI.SquareGray width=620 height=1></td></tr></table>");
		hb.append("<br>");
		
		hb.append("</center></body></html>");
		BaseBBSManager.separateAndSend(hb.toString(), player);
	}
	
	private static void showAddBoard(Player player, int page)
	{
	    HashMap<Integer, ArrayList<ItemInstance>> items = new HashMap<>();
	    int curr = 1;
	    int counter = 0;

	    ArrayList<ItemInstance> temp = new ArrayList<>();
	    for (ItemInstance item : player.getInventory().getItems())
	    {
	        if (item.getItemId() != Config.TICKET_MARKE_PLACE && item.isTradable() && checkItem(item) && !checkItemMIST(item))
	        {
	            temp.add(item);

	            counter++;

	            if (counter == 9)
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
	        player.sendMessage("Invalid page. Please try again.");
	        return;
	    }

	    HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY);
	    hb.append("<html><body>");
	    hb.append("<br><br>");
	    hb.append("<center><table><tr>");
	    hb.append("<td><button value=\"Auction List\" action=\"bypass bp_showMarketPlace 1 - *null*\" width=114 height=31 back=\"L2UI_CH3.bigbutton2_over\" fore=\"L2UI_CH3.bigbutton2\"></td>");
	    hb.append("<td><button value=\"Create Auction\" action=\"bypass bp_addpanel 1\" width=114 height=31 back=\"L2UI_CH3.bigbutton2_over\" fore=\"L2UI_ch3.bigbutton2\"></td>");
	    hb.append("<td><button value=\"My Auction\" action=\"bypass bp_myitems 1\" width=114 height=31 back=\"L2UI_CH3.bigbutton2_over\" fore=\"L2UI_ch3.bigbutton2\"></td>");
	    hb.append("</tr></table>");
	    hb.append("<br>");

	    // AVISO DA TAXA DO MARKETPLACE
	    hb.append("<font color=FF9999><center>Attention: A marketplace fee of <font color=LEVEL>" + getMarketPlaceFee() + "</font> will be charged upon selling items.</center></font><br>");

	    hb.append("<img src=L2UI.SquareGray width=620 height=1>");
	    hb.append("<table width=610 bgcolor=000000>");
	    hb.append("<tr>");
	    hb.append("<td><font color=F28034>Item</font></td>");
	    hb.append("<td width=220></td>");
	    hb.append("<td width=90 align=center><font color=F28034></font></td>");
	    hb.append("<td width=140 align=center><font color=F28034></font></td>");
	    hb.append("<td width=50 align=center><font color=F28034></font></td>");
	    hb.append("</tr>");

	    for (ItemInstance item : items.get(page))
	    {
	        hb.append("<tr>");
	        hb.append("<td width=32 height=37><img src=\"" + IconTable.getIcon(item.getItemId()) + "\" width=32 height=32></td>");

	        if (item.getEnchantLevel() > 0)
	        {
	            hb.append("<td width=220 height=23>" + ItemTable.getInstance().getTemplate(item.getItemId()).getName() + " +" + item.getEnchantLevel() + "</td>");
	        }
	        else
	        {
	            hb.append("<td width=220 height=23>" + ItemTable.getInstance().getTemplate(item.getItemId()).getName() + "</td>");
	        }
	        hb.append("<td width=90 height=23 align=center></td>");
	        hb.append("<td width=140 align=center></td><br1>");
	        hb.append("<td width=50 height=22><button value=\"SELECT\" action=\"bypass bp_additem " + item.getObjectId() + "\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td>");
	        hb.append("</tr>");
	    }
	    hb.append("</table>");

	    hb.append("<img src=L2UI.SquareGray width=520 height=1>");
	    hb.append("<table>");
	    hb.append("<tr>");
	    if (items.keySet().size() > 1)
	    {
	        if (page > 1)
	            hb.append("<td align=center valign=center><button value=\"PREV\" action=\"bypass bp_addpanel " + (page - 1) + "\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td>");

	        hb.append("<td width=50 align=center valign=center>Page " + page + "</td>");

	        if (items.keySet().size() > page)
	            hb.append("<td align=center valign=center><button value=\"NEXT\" action=\"bypass bp_addpanel " + (page + 1) + "\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td>");
	    }
	    hb.append("</tr>");
	    hb.append("</table>");
	    hb.append("</center></body></html>");
	    BaseBBSManager.separateAndSend(hb.toString(), player);
	}

 
	private static void showAddBoard2(Player player, int itemId)
	{
	    ItemInstance item = player.getInventory().getItemByObjectId(itemId);

	    HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY);
	    hb.append("<html><body>");
	    hb.append("<br><br>");
	    hb.append("<center><table><tr>");
	    hb.append("<td><button value=\"Auction List\" action=\"bypass bp_showMarketPlace 1 - *null*\" width=114 height=31 back=\"L2UI_CH3.bigbutton2_over\" fore=\"L2UI_ch3.bigbutton2\"></td>");
	    hb.append("<td><button value=\"Create Auction\" action=\"bypass bp_addpanel 1\" width=114 height=31 back=\"L2UI_ch3.bigbutton2_over\" fore=\"L2UI_ch3.bigbutton2\"></td>");
	    hb.append("<td><button value=\"My Auction\" action=\"bypass bp_myitems 1\" width=114 height=31 back=\"L2UI_ch3.bigbutton2_over\" fore=\"L2UI_ch3.bigbutton2\"></td>");
	    hb.append("</tr></table>");
	    hb.append("<br>");

	    int feePercent = (int) (Config.AUCTION_TAX_RATE * 100);
	    int minCount = Config.AUCTION_TAX_MINIMUM_ITEM_COUNT;

	    // Pega o item da taxa usando TICKET_MARKE_PLACE
	    int currencyItemId = Config.TICKET_MARKE_PLACE;
	    String currencyName = ItemTable.getInstance().getTemplate(currencyItemId).getName();

	    hb.append("<font color=FF9999><center>Attention: A marketplace fee of <font color=LEVEL>" + feePercent + "%</font> will be charged on sales with a minimum price of <font color=LEVEL>" + minCount + " " + currencyName + "</font>.</center></font><br>");

	    hb.append("<img src=L2UI.SquareGray width=620 height=1>");
	    hb.append("<table width=610 bgcolor=000000>");
	    hb.append("<tr>");
	    hb.append("<td align=center>");
	    hb.append("<img src=\"" + IconTable.getIcon(item.getItemId()) + "\" width=32 height=32>");

	    if (item.getEnchantLevel() > 0)
	        hb.append("<br>Item: " + ItemTable.getInstance().getTemplate(item.getItemId()).getName() + " +" + item.getEnchantLevel());
	    else
	        hb.append("<br>Item: " + ItemTable.getInstance().getTemplate(item.getItemId()).getName());

	    if (item.isStackable() && checkItem(item))
	    {
	        hb.append("<br>Set amount of items to sell:");
	        hb.append("<br1><edit var=amm type=number width=170 height=15>");
	    }

	    hb.append("<br>Select price:");
	    hb.append("<br1><combobox width=120 height=17 var=ebox list=Donate>");
	    hb.append("<br><edit var=count type=number width=170 height=15>");
	    hb.append("<br><button value=\"ADD ITEM\" action=\"bypass bp_addit2 " + itemId + " $ebox $count " + (item.isStackable() ? "$amm" : "1") + "\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\">");
	    hb.append("</td>");
	    hb.append("</tr>");
	    hb.append("</table>");
	    hb.append("<img src=L2UI.SquareGray width=620 height=1>");
	    hb.append("</center></body></html>");
	    BaseBBSManager.separateAndSend(hb.toString(), player);
	}


	
	public static String getMarketPlaceFee()
	{
		int feeId = Config.MARKETPLACE_FEE[0];
		int feeCount = Config.MARKETPLACE_FEE[1];

		return StringUtil.concat(String.valueOf(feeCount), " - ", ItemTable.getInstance().getTemplate(feeId).getName());
	}
	
	private static void showMyItemsBoard(Player player, int page)
	{
		Map<Integer, ArrayList<AuctionHolder>> items = new ConcurrentHashMap<>();
		
		int curr = 1;
		int counter = 0;
 
		ArrayList<AuctionHolder> temp = new ArrayList<>();
		for (Map.Entry<Integer, AuctionHolder> entry : AuctionTableCommunity.getInstance().getItems().entrySet())
		{
			if (entry.getValue().getOwnerId() == player.getObjectId())
			{
				temp.add(entry.getValue());
 
				counter++;
 
				if (counter == 9)
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
			//showChatWindow(player);
			player.sendMessage("Invalid page. Please try again.");
			return;
		}
 
		HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY);
		hb.append("<html><body>");
		hb.append("<br><br>");
		hb.append("<center><table><tr>");
		hb.append("<td><button value=\"Auction List\" action=\"bypass bp_showMarketPlace 1 - *null*\" width=114 height=31 back=\"L2UI_CH3.bigbutton2_over\" fore=\"L2UI_CH3.bigbutton2\"></td>");
		hb.append("<td><button value=\"Create Auction\" action=\"bypass bp_addpanel 1\" width=114 height=31 back=\"L2UI_CH3.bigbutton2_over\" fore=\"L2UI_CH3.bigbutton2\"></td>");
		hb.append("<td><button value=\"My Auction\" action=\"bypass bp_myitems 1\" width=114 height=31 back=\"L2UI_CH3.bigbutton2_over\" fore=\"L2UI_CH3.bigbutton2\"></td>");
		hb.append("</tr></table>");
		hb.append("<br>");
		
		hb.append("<img src=L2UI.SquareGray width=620 height=1>");
		hb.append("<table width=610 bgcolor=000000>");
		hb.append("<tr>");
		hb.append("<td><font color=F28034>Item</font></td>");
		hb.append("<td width=220></td>");
		hb.append("<td width=90 align=center><font color=F28034>Count</font></td>");
		hb.append("<td width=140 align=center><font color=F28034>Sale Price</font></td>");
		hb.append("<td width=50 align=center><font color=F28034></font></td>");
		hb.append("</tr>");

		for (AuctionHolder item : items.get(page))
		{
			hb.append("<tr>");
			hb.append("<td width=32 height=37><img src=\""+IconTable.getIcon(item.getItemId())+"\" width=32 height=32></td>");
			
			if (item.getEnchant() > 0)
				hb.append("<td width=220 height=23>"+ItemTable.getInstance().getTemplate(item.getItemId()).getName()+" +"+item.getEnchant()+"</td>");
			else
				hb.append("<td width=220 height=23>"+ItemTable.getInstance().getTemplate(item.getItemId()).getName()+"</td>");

			hb.append("<td width=90 height=23 align=center>"+item.getCount()+"</td>");
			hb.append("<td width=140 align=center>"+StringUtil.formatNumber(item.getCostCount())+"</td><br1>");
			hb.append("<td width=50 height=22><button value=\"REMOVE\" action=\"bypass bp_removeMarket "+item.getAuctionId()+"\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td>");
			hb.append("</tr>");
		}
		
		hb.append("</table>");
		hb.append("<img src=L2UI.SquareGray width=620 height=1>");
		hb.append("<table>");
		hb.append("<tr>");
		if (items.keySet().size() > 1)
		{
			if (page > 1)
				hb.append("<td align=center valign=center><button value=\"PREV\" action=\"bypass bp_myitems "+(page-1)+"\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td>");

			hb.append("<td width=50 align=center valign=center>Page " + page + "</td>");
			
			if (items.keySet().size() > page)
				hb.append("<td align=center valign=center><button value=\"NEXT\" action=\"bypass bp_myitems "+(page+1)+"\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td>");
		}
		hb.append("</tr>");
		hb.append("</table>");
		
		hb.append("</center></body></html>");
		BaseBBSManager.separateAndSend(hb.toString(), player);
	}
	
	private static void showBuyBoard(Player player, AuctionHolder item)
	{
	//    System.out.println("showBuyBoard chamado para jogador: " + player.getName() + ", auctionId: " + item.getAuctionId());

	    HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY);
	    hb.append("<html><body>");
	    hb.append("<br><br>");
	    hb.append("<center><table><tr>");
	    hb.append("<td><button value=\"Auction List\" action=\"bypass bp_showMarketPlace 1 - *null*\" width=114 height=31 back=\"L2UI_CH3.bigbutton2_over\" fore=\"L2UI_CH3.bigbutton2\"></td>");
	    hb.append("<td><button value=\"Create Auction\" action=\"bypass bp_addpanel 1\" width=114 height=31 back=\"L2UI_CH3.bigbutton2_over\" fore=\"L2UI_CH3.bigbutton2\"></td>");
	    hb.append("<td><button value=\"My Auction\" action=\"bypass bp_myitems 1\" width=114 height=31 back=\"L2UI_CH3.bigbutton2_over\" fore=\"L2UI_CH3.bigbutton2\"></td>");
	    hb.append("</tr></table>");
	    hb.append("<br>");

	    hb.append("<img src=L2UI.SquareGray width=620 height=1>");
	    hb.append("<table width=600 bgcolor=000000>");
	    hb.append("<tr>");
	    hb.append("<td align=center>");

	    String itemName = ItemTable.getInstance().getTemplate(item.getItemId()).getName();
	    String currencyName = ItemTable.getInstance().getTemplate(item.getCostId()).getName();

	 //   System.out.println("Item a mostrar: " + itemName + ", Enchant: " + item.getEnchant());
	  //  System.out.println("Preço: " + item.getCostCount() + " " + currencyName);

	    if (item.getEnchant() > 0)
	    {
	        hb.append("<img src=\"" + IconTable.getIcon(item.getItemId()) + "\" width=32 height=32> <br>");
	        hb.append("Item: <font color=LEVEL>" + item.getCount() + "</font> - <font color=e6dcbe>" + itemName + "</font> +<font color=LEVEL>" + item.getEnchant() + "</font><br>");
	    }
	    else
	    {
	        hb.append("<img src=\"" + IconTable.getIcon(item.getItemId()) + "\" width=32 height=32> <br>");
	        hb.append("Item: <font color=LEVEL>" + item.getCount() + "</font> - <font color=e6dcbe>" + itemName + "</font><br>");
	    }

	    long cost = item.getCostCount();
	    long tax = 0;
	    if (cost >= Config.AUCTION_TAX_MINIMUM_ITEM_COUNT)
	        tax = (long) Math.ceil(cost * Config.AUCTION_TAX_RATE);
	    long finalAmount = Math.max(0, cost - tax);

	 //   System.out.println("Taxa calculada: " + tax + ", Valor líquido para vendedor: " + finalAmount);

	    if (tax > 0)
	    {
	        hb.append("<font color=FF9999>Marketplace fee: " + tax + " (" + (int)(Config.AUCTION_TAX_RATE * 100) + "%)</font><br1>");
	    }
	    hb.append("<font color=LEVEL>Seller receives: " + finalAmount + " " + currencyName + "</font><br>");

	    hb.append("</td>");
	    hb.append("</tr>");
	    hb.append("</table>");
	    hb.append("<img src=L2UI.SquareGray width=620 height=1>");
	    hb.append("<table>");
	    hb.append("<tr>");
	    hb.append("<td><button value=\"BUY\" action=\"bypass bp_confirmBuy " + item.getAuctionId() + "\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td>");
	    hb.append("<td><button value=\"CANCEL\" action=\"bypass bp_showMarketPlace 1 - *null*\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td>");
	    hb.append("</tr>");
	    hb.append("</table>");

	    hb.append("</center></body></html>");
	    BaseBBSManager.separateAndSend(hb.toString(), player);

	//    System.out.println("showBuyBoard enviado para jogador: " + player.getName());
	}
	
	private static void addItemToOffline(int playerId, int itemId, int count)
	{
		Item item = ItemTable.getInstance().getTemplate(itemId);
		int objectId = IdFactory.getInstance().getNextId();
		
		try (Connection con = ConnectionPool.getConnection())
		{
			if (count > 1 && !item.isStackable())
				return;
			
			PreparedStatement statement = con.prepareStatement("INSERT INTO items (owner_id,item_id,count,loc,loc_data,enchant_level,object_id,custom_type1,custom_type2,mana_left,time) VALUES (?,?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, playerId);
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
			statement.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public String[] getBypassHandlersList()
	{
		return new String[] { "bp_showMarketPlace", "bp_showByGrade", "bp_showByCategory", "bp_buy", "bp_confirmBuy", "bp_addpanel", "bp_additem", "bp_addit2", "bp_myitems", "bp_removeMarket" };
	}
}