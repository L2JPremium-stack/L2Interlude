package net.sf.l2j.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.EtcStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;

public class L2AioSellerInstance extends L2NpcInstance
{
   public L2AioSellerInstance(int objectId, NpcTemplate template)
   {
      super(objectId, template);
   }

   @Override
   public void onAction(Player player)
   {
      player.setCurrentFolkNPC(this);

      // Check if the L2PcInstance already target the L2NpcInstance
      if (this != player.getTarget())
      {
         // Set the target of the L2PcInstance player
         player.setTarget(this);

         // Send a Server->Client packet MyTargetSelected to the L2PcInstance player
         MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
         player.sendPacket(my);
         my = null;

         // Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
         player.sendPacket(new ValidateLocation(this));
      }
      else
      {
         // Calculate the distance between the L2PcInstance and the L2NpcInstance
         if (!canInteract(player))
         {
            // Notify the L2PcInstance AI with AI_INTENTION_INTERACT
            player.getAI().setIntention(CtrlIntention.INTERACT, this);
         }
         else
         {
            showMessageWindow(player);
         }
      }
      // Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
      player.sendPacket(ActionFailed.STATIC_PACKET);
   }

   private void showMessageWindow(Player player)
   {
      String filename = "data/html/mods/Aio Shop/start.htm";

      NpcHtmlMessage html = new NpcHtmlMessage(1);
      html.setFile(filename);
      html.replace("%objectId%", String.valueOf(getObjectId()));
   //   html.replace("%servername%", Config.ALT_Server_Name);
      player.sendPacket(html);
      filename = null;
      html = null;
   }

   @Override
   public void onBypassFeedback(Player player, String command)
   {
      if (command.startsWith("add_aio"))
      {
         StringTokenizer st = new StringTokenizer(command);
         st.nextToken();

         String priceId = null, priceCount = null, time = null;
         int aioPriceId = 0, aioPriceCount = 0, aioTime = 0; 

         if (st.hasMoreTokens())
         {
            priceId = st.nextToken();
            priceCount = st.nextToken();
            time = st.nextToken();

            try
            {
               aioPriceId = Integer.parseInt(priceId);
               aioPriceCount = Integer.parseInt(priceCount);
               aioTime = Integer.parseInt(time);
            }
            catch(NumberFormatException e) {}
         }
         else
         {
            _log.warning("Could not update aio status of player " + player.getName());
            return;
         }

         makeAioCharacter(player, aioPriceId, aioPriceCount, aioTime);
      }
      else if (command.startsWith("remove_aio"))
         removeAio(player);

      showMessageWindow(player);
   }

   
   public void makeAioCharacter(Player player, int itemId, int itemCount, int aioTime)
   {
	   if(player.isAio())
	   {
		   player.sendMessage("You have AIO Status!");
		   return;
	   }
	   if(player.isSubClassActive())
	   {
		   player.sendMessage("No possible active in SubClass!");
		   return; 
	   }
      ItemInstance itemInstance = player.getInventory().getItemByItemId(itemId);

      if (itemInstance == null || !itemInstance.isStackable() && player.getInventory().getInventoryItemCount(itemId, -1) < (itemCount))
      {
            player.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
         return;
      }
      else if (itemInstance.isStackable())
      {
         if (!player.destroyItemByItemId("Aio", itemId, itemCount, player.getTarget(), true))
         {
                player.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ITEMS));
            return;
         }
      }
      else
         for (int i = 0; i < (itemCount); i++)
            player.destroyItemByItemId("Aio", itemId, 1, player.getTarget(), true);

      doAio(player, aioTime);
   }
   public void doAio(Player player, int days)
   {
      if(player == null)
         return;
      
      if(player.isAio())
      {
         player.sendMessage("You have AIO Status!");
         return;
      }
      if(player.isSubClassActive())
      {
    	  player.sendMessage("No possible active in SubClass!");
          return; 
      }

        int daysLeft = player.getAioEndTime() <= 0 ? 0 : (int) ((player.getAioEndTime() - System.currentTimeMillis()) / 86400000);
      player.setAio(true);
      player.setEndTime("aio", days + daysLeft);

      player.getStat().addExp(player.getStat().getExpForLevel(81));

 //     if(Config.ALLOW_AIO_NCOLOR && player.isAio())
  //       player.getAppearance().setNameColor(Config.AIO_NCOLOR);

   //   if(Config.ALLOW_AIO_TCOLOR && player.isAio())
    //     player.getAppearance().setTitleColor(Config.AIO_TCOLOR);

      /* Give Aio Dual */
  //    ItemInstance item;
 //     if(player.getInventory().getItemByItemId(Config.DUAL_AIO_ID) == null)
  //    {
   //      item = player.getInventory().addItem("", Config.DUAL_AIO_ID, 1, player, null);
    //     InventoryUpdate iu = new InventoryUpdate();
     //    iu.addItem(item);
      //   player.sendPacket(iu);
     // }
      if(Config.ALLOW_AIO_ITEM)
      {
    	  player.getInventory().addItem("", Config.AIO_ITEMID, 1, player, null);
    	  player.getInventory().equipItem(player.getInventory().getItemByItemId(Config.AIO_ITEMID));
    	  
      }

      player.rewardAioSkills();
      player.sendPacket(new EtcStatusUpdate(player));
      player.sendSkillList();
      player.broadcastUserInfo();

      player.sendMessage("You are now an Aio, Congratulations!");
   }

   public void removeAio(Player player)
   {
      if(!player.isAio())
      {
         player.sendMessage("You are not an AIO.");
         return;
      }

      player.setAio(false);
      player.setAioEndTime(0);

  //    player.getAppearance().setNameColor(0xFFFFFF);
   //   player.getAppearance().setTitleColor(0xFFFF77);

      /* Remove Aio Dual */
      if(Config.ALLOW_AIO_ITEM)
      {
    	  player.getInventory().destroyItemByItemId("", Config.AIO_ITEMID, 1, player, null);
    	  player.getWarehouse().destroyItemByItemId("", Config.AIO_ITEMID, 1, player, null);
      }
            
      player.lostAioSkills();
      player.sendPacket(new EtcStatusUpdate(player));
      player.sendSkillList();
      player.broadcastUserInfo();

      player.sendMessage("Now You are not an Aio..");
   }
}