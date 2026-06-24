package net.sf.l2j.solofarm.instancemanager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.event.tournament.InstanceHolder;
import net.sf.l2j.event.tournament.InstanceManager;
import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage.SMPOS;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.TutorialShowHtml;
import net.sf.l2j.solofarm.data.SoloFarmData;
import net.sf.l2j.solofarm.data.sql.SoloFarmDAO;
import net.sf.l2j.solofarm.holder.SoloFarmConfig;
import net.sf.l2j.solofarm.holder.SoloFarmReward;
import net.sf.l2j.solofarm.holder.SoloFarmSession;
import net.sf.l2j.solofarm.holder.SoloFarmSpawn;

public class SoloFarmManager
{
	private final Map<Integer, SoloFarmSession> _sessions = new ConcurrentHashMap<>();
	private final InstanceHolder _instance = InstanceManager.getInstance().createInstance();
	
	public boolean buyMonsters(Player player, int amount)
	{
		final SoloFarmConfig config = SoloFarmData.getInstance().getConfig();
		if (config == null || !config.isEnabled())
			return false;
		
		if (amount < config.getMinBuy() || amount > config.getMaxBuy())
			return false;
		
		final long price = config.calculatePrice(amount);
		
		if (!player.destroyItemByItemId("SoloFarmBuy", config.getPriceItemId(), (int) price, player, true))
			return false;
		
		SoloFarmDAO.addBalance(player.getObjectId(), amount);
		player.sendMessage("You bought " + amount + " monsters for SoloFarm.");
		return true;
	}
	
	public int getBalance(Player player)
	{
		return SoloFarmDAO.getBalance(player.getObjectId());
	}
	
	public int[] getDetalhes(Player player)
	{
		return SoloFarmDAO.getStats(player.getObjectId());
	}
	
	public boolean canEnter(Player player)
	{
		if (player == null)
			return false;
		
		if (player.isInOlympiadMode() || player.isDead() || player.isAlikeDead())
			return false;
		
		if (_sessions.containsKey(player.getObjectId()))
			return false;
		
		return getBalance(player) > 0;
	}
	
	public void enter(Player player)
	{
		final SoloFarmConfig config = SoloFarmData.getInstance().getConfig();
		if (config == null || !config.isEnabled())
			return;
		
		if (!canEnter(player))
		{
			player.sendMessage("You do not have purchased monsters.");
			return;
		}
		
		final int balance = getBalance(player);
		
		final SoloFarmSession session = new SoloFarmSession(player.getObjectId());
		session.setRemainingToKill(balance);
		session.setEndTime(System.currentTimeMillis() + (config.getInstanceMinutes() * 60_000L));
		
		_sessions.put(player.getObjectId(), session);
		player.setInstance(_instance, true);
		player.teleToLocation(config.getEntryX(), config.getEntryY(), config.getEntryZ(), 0);
		SoloFarmDAO.updateLastEnterTime(player.getObjectId(), System.currentTimeMillis());
		
		fillArena(player);
		
		ThreadPool.schedule(() -> {
			final SoloFarmSession current = _sessions.get(player.getObjectId());
			if (current != null)
				finish(player, false);
		}, config.getInstanceMinutes() * 60_000L);
		
		sendTutorial(player);
	}
	
	public void fillArena(Player player)
	{
		final SoloFarmConfig config = SoloFarmData.getInstance().getConfig();
		final SoloFarmSession session = _sessions.get(player.getObjectId());
		
		if (config == null || session == null)
			return;
		
		int toSpawn = Math.min(config.getMaxAliveMonsters() - session.getAliveMonsters(), session.getRemainingToKill() - session.getAliveMonsters());
		
		for (int i = 0; i < toSpawn; i++)
		{
			spawnOne(player, session);
		}
	}
	
	private static void spawnOne(Player player, SoloFarmSession session)
	{
		if (SoloFarmSpawnTable.getInstance().getSpawns().isEmpty())
			return;
		
		final SoloFarmSpawn spawn = Rnd.get(SoloFarmSpawnTable.getInstance().getSpawns());
		
		final L2Npc npc = addSpawnForPlayer(player, spawn);
		if (npc == null)
			return;
		
		session.getSpawnedNpcObjectIds().add(npc.getObjectId());
		session.increaseAlive();
	}
	
	private static L2Npc addSpawnForPlayer(Player player, SoloFarmSpawn spawn)
	{
		try
		{
			final NpcTemplate template = NpcTable.getInstance().getTemplate(spawn.getNpcId());
			if (template == null)
				return null;
			
			final L2Spawn spawnDat = new L2Spawn(template);
			
			spawnDat.setLoc(spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getHeading());
			
			spawnDat.setRespawnDelay(0);
			
			final L2Npc npc = spawnDat.doSpawn(false);
			npc.setInstance(player.getInstance(), false);
			
			return npc;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void onKill(Attackable npc, Player killer)
	{
		if (killer == null)
			return;
		
		final SoloFarmSession session = _sessions.get(killer.getObjectId());
		if (session == null)
			return;
		
		if (!session.getSpawnedNpcObjectIds().remove(npc.getObjectId()))
			return;
		
		session.decreaseAlive();
		
		if (!SoloFarmDAO.consumeOneKill(killer.getObjectId()))
		{
			finish(killer, false);
			return;
		}
		
		session.decreaseRemaining();
		
		applyRewards(killer);
		
		if (session.getRemainingToKill() <= 0)
		{
			finish(killer, true);
			return;
		}
		
		final SoloFarmConfig config = SoloFarmData.getInstance().getConfig();
		
		sendRemainingMessage(killer, session);
		ThreadPool.schedule(() -> fillArena(killer), 1000L * config.getRespawnOnKillDelay());
	}
	
	private static void applyRewards(Player player)
	{
		final SoloFarmConfig config = SoloFarmData.getInstance().getConfig();
		
		for (SoloFarmReward reward : config.getRewards())
		{
			if (reward.getChance() <= 0)
				continue;
			
			if (Rnd.get(100) >= reward.getChance())
				continue;
			
			int count = Rnd.get(reward.getMin(), reward.getMax());
			
			if (count <= 0)
				continue;
			
			player.addItem("SoloFarmReward", reward.getItemId(), count, player, true);
		}
	}
	
	private static void sendRemainingMessage(Player player, SoloFarmSession session)
	{
		int remaining = session.getRemainingToKill();
		
		ExShowScreenMessage msg = new ExShowScreenMessage("Remaining Monsters: " + remaining, 2000, SMPOS.BOTTOM_RIGHT, false);
		
		player.sendPacket(msg);
	}
	
	public void finish(Player player, boolean success)
	{
		final SoloFarmConfig config = SoloFarmData.getInstance().getConfig();
		final SoloFarmSession session = _sessions.remove(player.getObjectId());
		
		if (config == null || session == null)
			return;
		
		InstanceManager.getInstance().deleteInstance(_instance.getId());
		
		if (success)
			player.sendMessage("All purchased monsters have been defeated.");
		else
			player.sendMessage("SoloFarm instance closed.");
		
		player.setInstance(InstanceManager.getInstance().getInstance(0), true);
		player.teleToLocation(config.getExitX(), config.getExitY(), config.getExitZ(), 20);
		
		for (int objectId : session.getSpawnedNpcObjectIds())
		{
			L2Object obj = L2World.getInstance().getObject(objectId);
			
			if (obj instanceof L2Npc)
			{
				L2Npc npc = (L2Npc) obj;
				npc.deleteMe();
			}
		}
	}
	
	public void onLogout(Player player)
	{
		if (_sessions.containsKey(player.getObjectId()))
		{
			final SoloFarmConfig config = SoloFarmData.getInstance().getConfig();
			final SoloFarmSession session = _sessions.remove(player.getObjectId());
			
			if (config == null || session == null)
				return;
			
			InstanceManager.getInstance().deleteInstance(_instance.getId());
			player.setInstance(InstanceManager.getInstance().getInstance(0), true);
			player.setXYZ(config.getExitX(), config.getExitY(), config.getExitZ());
			
			for (int objectId : session.getSpawnedNpcObjectIds())
			{
				L2Object obj = L2World.getInstance().getObject(objectId);
				
				if (obj instanceof L2Npc)
				{
					L2Npc npc = (L2Npc) obj;
					npc.deleteMe();
				}
			}
		}
	}
	
	public void showMainPage(Player player)
	{
		final int balance = SoloFarmManager.getInstance().getBalance(player);
		final SoloFarmConfig config = SoloFarmData.getInstance().getConfig();
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/mods/solofarm/index.htm");
		
		html.replace("%objectId%", String.valueOf(0));
		html.replace("%balance%", String.format("%,d", balance));
		html.replace("%price%", String.format("%,d", config.getPricePerMob()));
		html.replace("%min%", String.valueOf(config.getMinBuy()));
		html.replace("%max%", String.valueOf(config.getMaxBuy()));
		html.replace("%time%", String.valueOf(config.getInstanceMinutes()));
		html.replace("%alive%", String.valueOf(config.getMaxAliveMonsters()));
		html.replace("%cost500%", String.format("%,d", config.calculatePrice(500)));
		html.replace("%cost1000%", String.format("%,d", config.calculatePrice(1000)));
		
		player.sendPacket(html);
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public void sendTutorial(Player player)
	{
		String html = "<html><body><center>" +
			
			"<table width=260>" +
			
			// HEADER
			"<tr><td align=center>" + "<font color=\"LEVEL\">Solo Farm Instance</font><br>" + "<img src=\"L2UI.SquareWhite\" width=260 height=1>" + "</td></tr>" +
			
			// WELCOME
			"<tr><td align=center>" + "<br>" + "<font color=\"AAAAAA\">Welcome, %player%!</font>" + "</td></tr>" +
			
			// INFO
			"<tr><td>" + "<br>" + "<font color=\"AAAAAA\">" + "• Private instance<br1>" + "• Kill all monsters<br1>" + "• Rewards per kill" + "</font>" + "</td></tr>" +
			
			// EXIT TITLE
			"<tr><td align=center>" + "<br>" + "<font color=\"FF9900\">Exit</font>" + "</td></tr>" +
			
			// EXIT INFO
			"<tr><td align=center>" + "<font color=\"AAAAAA\">Use button anytime</font>" + "</td></tr>" +
			
			// BUTTON
			"<tr><td align=center>" + "<br>" +
			
			"<button value=\"Exit\" action=\"link solofarm_exit\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\">" +
			
			"</td></tr>" +
			
			"</table>" +
			
			"</center></body></html>";
			
		html = html.replace("%player%", player.getName());
		
		player.sendPacket(new TutorialShowHtml(html));
	}
	
	public void sendInfoSoloFarm(Player player)
	{
		final SoloFarmConfig config = SoloFarmData.getInstance().getConfig();
		
		String html = "<html><body><center>" +
			
			"<table width=260>" +
			
			// HEADER
			"<tr><td align=center>" + "<font color=\"LEVEL\">Solo Farm Instance</font><br>" + "<img src=\"L2UI.SquareWhite\" width=260 height=1>" + "</td></tr>" +
			
			// PLAYER
			"<tr><td align=center>" + "<br>" + "<font color=\"AAAAAA\">Adventurer:</font><br1>" + "<font color=\"00FF00\">%player%</font>" + "</td></tr>" +
			
			// STORY
			"<tr><td>" + "<br>" + "<font color=\"FF9900\">History</font><br1>" + "<font color=\"AAAAAA\">" + "A hidden dimension was discovered where monsters multiply endlessly.<br1>" + "Only the strongest warriors dare to enter alone.<br1>" + "Inside, you face your own limits... and your greed." + "</font>" + "</td></tr>" +
			
			// RULES
			"<tr><td>" + "<br>" + "<font color=\"FF9900\">Rules</font><br1>" + "<font color=\"AAAAAA\">" + "• Private solo instance<br1>" + "• Monsters spawn continuously<br1>" + "• Rewards per kill<br1>" + "• Auto exit when finished" + "</font>" + "</td></tr>" +
			
			// PRICE INFO
			"<tr><td align=center>" + "<br>" + "<img src=\"L2UI.SquareWhite\" width=260 height=1><br1>" + "<font color=\"FF9900\">Entry System</font><br1><br1>" +
			
			"<font color=\"AAAAAA\">Price per monster</font><br1>" + "<font color=\"FFD700\">%price%</font><br1><br1>" +
			
			"<font color=\"AAAAAA\">Allowed Purchase</font><br1>" + "<font color=\"FFFFFF\">%min% - %max%</font>" + "</td></tr>" +
			
			// INSTANCE INFO
			"<tr><td align=center>" + "<br1>" + "<img src=\"L2UI.SquareWhite\" width=260 height=1><br1>" + "<font color=\"FF9900\">Instance Info</font><br1><br1>" +
			
			"<font color=\"AAAAAA\">Time Limit</font><br1>" + "%time% minutes<br1><br1>" +
			
			"<font color=\"AAAAAA\">Max Monsters Alive</font><br1>" + "%alive%" + "</td></tr>" +
			
			// ACTION
			"<tr><td align=center>" + "<br1>" + "<img src=\"L2UI.SquareWhite\" width=260 height=1><br1><br1>" +
			
			"<button value=\"Close\" action=\"link close\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\">" + "</td></tr>" +
			
			"</table>" +
			
			"</center></body></html>";
			
		html = html.replace("%player%", player.getName());
		html = html.replace("%price%", String.format("%,d Adena", config.getPricePerMob()));
		html = html.replace("%min%", String.valueOf(config.getMinBuy()));
		html = html.replace("%max%", String.valueOf(config.getMaxBuy()));
		html = html.replace("%time%", String.valueOf(config.getInstanceMinutes()));
		html = html.replace("%alive%", String.valueOf(config.getMaxAliveMonsters()));
		
		player.sendPacket(new TutorialShowHtml(html));
	}
	
	public void sendDetalhesSoloFarm(Player player)
	{
		final int[] stats = SoloFarmDAO.getStats(player.getObjectId());
		
		final int balance = stats[0];
		final int totalBought = stats[1];
		final int totalKilled = stats[2];
		
		int progress = 0;
		
		if (totalBought > 0)
			progress = (int) ((totalKilled * 100.0) / totalBought);
		String color = progress > 70 ? "00FF00" : progress > 30 ? "FFFF00" : "FF0000";
		String html = "<html><body><center>" +
			
			"<table width=260>" +
			
			// HEADER
			"<tr><td align=center>" + "<font color=\"LEVEL\">Solo Farm Stats</font><br>" + "<img src=\"L2UI.SquareWhite\" width=260 height=1>" + "</td></tr>" +
			
			// PLAYER
			"<tr><td align=center>" + "<br>" + "<font color=\"AAAAAA\">Player</font><br1>" + "<font color=\"00FF00\">" + player.getName() + "</font>" + "</td></tr>" +
			
			// STATS
			"<tr><td>" + "<br>" + "<font color=\"FF9900\">Statistics</font><br1>" + "<font color=\"AAAAAA\">" + "Total Bought: " + format(totalBought) + "<br1>" + "Total Killed: " + format(totalKilled) + "<br1>" + "Remaining: " + format(balance) + "</font>" + "</td></tr>" +
			
			// PROGRESS
			"<tr><td align=center>" + "<br>" + "<img src=\"L2UI.SquareWhite\" width=260 height=1><br>" + "<font color=\"FF9900\">Progress</font><br><br>" +
			
			"<font color=\"" + color + "\">" + progress + "%</font>" + "</td></tr>" +
			
			// ACTION
			"<tr><td align=center>" + "<br>" + "<button value=\"Close\" action=\"link close\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\">" + "</td></tr>" +
			
			"</table>" +
			
			"</center></body></html>";
			
		player.sendPacket(new TutorialShowHtml(html));
	}
	
	private static String format(int value)
	{
		return String.format("%,d", value);
	}
	
	public static SoloFarmManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		private static final SoloFarmManager INSTANCE = new SoloFarmManager();
	}
}
