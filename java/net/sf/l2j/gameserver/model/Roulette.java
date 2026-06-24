package net.sf.l2j.gameserver.model;

import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.sf.l2j.Config;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.datatables.xml.IconTable;
import net.sf.l2j.gameserver.datatables.xml.RouletteData;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;

public class Roulette
{
	private ScheduledFuture<?> timerTask;
	private ScheduledFuture<?> _taskCancel;
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	
	public Roulette(Player player)
	{
		_taskCancel = scheduler.schedule(() -> {
			_spinStop(player);
			
			timerTask = scheduler.scheduleAtFixedRate(() -> spinRolette(player, false), 0, 300, TimeUnit.MILLISECONDS);
			
			_taskCancel = scheduler.schedule(() -> {
				_spinStop(player);
				
				timerTask = scheduler.scheduleAtFixedRate(() -> spinRolette(player, false), 0, 500, TimeUnit.MILLISECONDS);
				
				_taskCancel = scheduler.schedule(() -> {
					_spinStop(player);
					
					timerTask = scheduler.scheduleAtFixedRate(() -> spinRolette(player, false), 0, 1000, TimeUnit.MILLISECONDS);
					
					_taskCancel = scheduler.schedule(() -> {
						_spinStop(player);
						spinRolette(player, true);
					}, 1000 * 3, TimeUnit.MILLISECONDS);
				}, 1000 * 3, TimeUnit.MILLISECONDS);
			}, 1000 * 3, TimeUnit.MILLISECONDS);
		}, 0, TimeUnit.MILLISECONDS);
	}
	
	private void _spinStop(Player player)
	{
		if (timerTask != null)
		{
			timerTask.cancel(false);
			_taskCancel.cancel(false);
			timerTask = null;
		}
	}
	
	private static void spinRolette(Player player, boolean stop)
	{
		NpcHtmlMessage htm = new NpcHtmlMessage(0);
		htm.setFile("data/html/" + "mods/Roleta/items/ROULETTE.htm");
		
		int pos1 = 0, pos2 = 0, pos3 = 0, pos4 = 0, pos5 = 0;
		ItemInstance _pos1 = null, _pos2 = null, _pos3 = null, _pos4 = null, _pos5 = null;
		int percent1 = 0, percent2 = 0, percent3 = 0, count3 = 1, percent4 = 0, percent5 = 0;
		boolean finish = false;
		TreeMap<Integer, net.sf.l2j.gameserver.instancemanager.custom.Roulette> Roulette = RouletteData.getInstance().get_rewards();
		
		for (int i = Rnd.get(Roulette.size()); !finish; i = Rnd.get(Roulette.size()))
		{
			if (Roulette.get(i) != null && Roulette.get(i).get_chance() > Rnd.get(100))
			{
				if (pos1 == 0)
				{
					pos1 = Roulette.get(i).get_itemId();
					percent1 = Roulette.get(i).get_chance();
					_pos1 = new ItemInstance(0, pos1);
					if (Roulette.get(i).get_enchant() > 0 && _pos1.isEquipable())
						_pos1.setEnchantLevel(Roulette.get(i).get_enchant());
				}
				else if (pos2 == 0)
				{
					pos2 = Roulette.get(i).get_itemId();
					percent2 = Roulette.get(i).get_chance();
					_pos2 = new ItemInstance(0, pos2);
					if (Roulette.get(i).get_enchant() > 0 && _pos2.isEquipable())
						_pos2.setEnchantLevel(Roulette.get(i).get_enchant());
				}
				else if (pos3 == 0)
				{
					pos3 = Roulette.get(i).get_itemId();
					_pos3 = new ItemInstance(0, pos3);
					percent3 = Roulette.get(i).get_chance();
					if (Roulette.get(i).get_enchant() > 0 && _pos3.isEquipable())
						_pos3.setEnchantLevel(Roulette.get(i).get_enchant());
					count3 = Roulette.get(i).get_count();
				}
				else if (pos4 == 0)
				{
					pos4 = Roulette.get(i).get_itemId();
					_pos4 = new ItemInstance(0, pos4);
					percent4 = Roulette.get(i).get_chance();
					if (Roulette.get(i).get_enchant() > 0 && _pos4.isEquipable())
						_pos4.setEnchantLevel(Roulette.get(i).get_enchant());
				}
				else if (pos5 == 0)
				{
					pos5 = Roulette.get(i).get_itemId();
					_pos5 = new ItemInstance(0, pos5);
					percent5 = Roulette.get(i).get_chance();
					if (Roulette.get(i).get_enchant() > 0 && _pos5.isEquipable())
						_pos5.setEnchantLevel(Roulette.get(i).get_enchant());
				}
				else
				{
					finish = true;
					break;
				}
			}
		}
		if (_pos1 != null && _pos2 != null && _pos3 != null && _pos4 != null && _pos5 != null)
		{
			String html = "<table cellpadding=\"0\" cellspacing=\"10\" width=32>\n" + "    <tr>\n";
			
			String template = "        <td align=center height=37 width=32>\n" + "            <table cellpadding=\"0\" cellspacing=\"5\" bgcolor=%s width=32>\n" + "                <tr>\n" + "                    <td align=center height=37 width=32>\n" + "                        <img src=%s width=32 height=32>\n" + "                    </td>\n" + "                </tr>\n" + "            </table>\n" + "        </td>\n";
			
			html += String.format(template, getBgColor(percent1), IconTable.getIcon(_pos1.getItemId()));
			html += String.format(template, getBgColor(percent2), IconTable.getIcon(_pos2.getItemId()));
			html += String.format(template, getBgColor(percent3), IconTable.getIcon(_pos3.getItemId()));
			html += String.format(template, getBgColor(percent4), IconTable.getIcon(_pos4.getItemId()));
			html += String.format(template, getBgColor(percent5), IconTable.getIcon(_pos5.getItemId()));
			
			html += "    </tr>\n" + "</table>";
			
			htm.replace("%content%", html);
			htm.replace("%name%", (_pos3.getEnchantLevel() > 0 ? "+" + _pos3.getEnchantLevel() + " " : "") + _pos3.getItemName());
			htm.replace("%Chance%", percent3 + "%");
			player.sendPacket(htm);
			if (stop)
			{
				if (pos3 != 0)
				{
					if (!player.destroyItemByItemId("Roleta", Config.CUSTOM_ITEM_ROULETTE, Config.ITEM_COUNT_ROLETA, player, true))
					{
						player.broadcastPacket(new SocialAction(player, 13));
						player.sendMessage("Roulette is failed. You do not have the necessary item in your bag.");
						return;
					}
					// Add the item to inventory
					final ItemInstance item = player.getInventory().addItem("Roleta", _pos3.getItemId(), count3, player, player);
					
					if (item != null)
					{
						if (item.isEquipable())
							item.setEnchantLevel(_pos3.getEnchantLevel());
						
						// Send inventory update packet
						InventoryUpdate iu = new InventoryUpdate();
						iu.addModifiedItem(item);
						player.sendPacket(iu);
						
						// Update current load as well
						final StatusUpdate su = new StatusUpdate(player);
						player.sendPacket(su);
						for (Player players : L2World.getInstance().getPlayers())
						{
							if (players != null && players.isOnline())
							{
								players.sendPacket(new CreatureSay(0, 18, "Roulette", "Roulette: " + player.getName() + " have earned " + (_pos3.getEnchantLevel() > 0 ? "+" + _pos3.getEnchantLevel() + " " : "") + _pos3.getItemName() + "!"));
							}
						}
						// Prepare the message with the good reason.
						// final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1).addItemName(item.getItemId() + item.getEnchantLevel());
						// player.sendPacket(sm);
						player.broadcastPacket(new MagicSkillUse(player, player, 2024, 1, 100, 0));
						player.broadcastPacket(new SocialAction(player, 3));
					}
				}
			}
		}
	}
	
	private static String getBgColor(int percent)
	{
		final int HIGH_THRESHOLD = 80;
		final int MEDIUM_HIGH_THRESHOLD = 70;
		final int MEDIUM_LOW_THRESHOLD = 50;
		final int LOW_MEDIUM_THRESHOLD = 30;
		final int LOW_THRESHOLD = 15;
		final int MINIMUM_THRESHOLD = 5;
		
		if (percent > HIGH_THRESHOLD)
			return "00FF00"; // Green - Color for percentages greater than 80%
		else if (percent >= MEDIUM_HIGH_THRESHOLD && percent < HIGH_THRESHOLD)
			return "00FF00"; // Lime Green - Color for percentages greater than or equal to 70% and less than 80%
		else if (percent >= MEDIUM_LOW_THRESHOLD && percent < MEDIUM_HIGH_THRESHOLD)
			return "00FF00"; // Yellow - Color for percentages greater than or equal to 50% and less than 70%
		else if (percent >= LOW_MEDIUM_THRESHOLD && percent < MEDIUM_LOW_THRESHOLD)
			return "00FF00"; // Orange Red - Color for percentages greater than or equal to 30% and less than 50%
		else if (percent >= LOW_THRESHOLD && percent < LOW_MEDIUM_THRESHOLD)
			return "00FF00"; // Orange Red - Color for percentages greater than or equal to 15% and less than 30%
		else if (percent >= MINIMUM_THRESHOLD && percent < LOW_THRESHOLD)
			return "FF0000"; // Purple - Color for percentages greater than or equal to 5% and less than 15%
		else
			return "FF0000"; // Red - Default color for percentages less than 5%
	}
}