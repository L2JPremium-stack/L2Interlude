package net.sf.l2j.gameserver.model.actor.instance;

import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.sf.l2j.Config;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.event.tournament.ArenaTask;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.datatables.TeleportLocationTable;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.model.L2TeleportLocation;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.model.zone.type.L2TimeFarmZone;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.timezone.TimeFarmZoneManager;

/**
 * @author NightMarez
 */
public final class L2TeleporterInstance extends L2NpcInstance
{
	private static final int COND_ALL_FALSE = 0;
	private static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	private static final int COND_OWNER = 2;
	private static final int COND_REGULAR = 3;
	
	public L2TeleporterInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		int condition = validateCondition(player);
		
		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command
		
		if (player.isArenaProtection())
		{
			if (!ArenaTask.is_started())
				player.setArenaProtection(false);
			else
				player.sendMessage("Remove your participation from the Tournament event!");
			return;
		}
		
		if (actualCommand.equalsIgnoreCase("goto"))
		{
			if (st.countTokens() <= 0)
				return;
			
			// No interaction possible with the NPC.
			if (!canInteract(player))
				return;
			
			if (condition == COND_REGULAR || condition == COND_OWNER)
			{
				doTeleport(player, Integer.parseInt(st.nextToken()));
				return;
			}
		}
		else if (command.startsWith("Chat"))
		{
			Calendar cal = Calendar.getInstance();
			int val = 0;
			try
			{
				val = Integer.parseInt(command.substring(5));
			}
			catch (IndexOutOfBoundsException ioobe)
			{
			}
			catch (NumberFormatException nfe)
			{
			}
			
			if (val == 1 && cal.get(Calendar.HOUR_OF_DAY) >= 20 && cal.get(Calendar.HOUR_OF_DAY) <= 23 && (cal.get(Calendar.DAY_OF_WEEK) == 1 || cal.get(Calendar.DAY_OF_WEEK) == 7))
			{
				showHalfPriceHtml(player);
				return;
			}
			showChatWindow(player, val);
		}
		else if (command.startsWith("solo_farmzone"))
		{
			TeleportSoloZone(player);
		}
		else if (command.startsWith("timefarmzone"))
		{
			TeleportTimeFarmZone(player);
		}
		else
			super.onBypassFeedback(player, command);
	}
	
	private static void TeleportTimeFarmZone(Player player)
	{
		L2TimeFarmZone currentZone = TimeFarmZoneManager.getCurrentZone();
		if (currentZone == null)
		{
			player.sendMessage("There is no active Time Farm Zone at the moment.");
			return;
		}
		
		List<Location> spawns = currentZone.getSpawns();
		Location loc;
		
		if (!spawns.isEmpty())
			loc = spawns.get(Rnd.get(spawns.size())); // 🎯 spawn aleatório da zona
		else
			loc = currentZone.getZoneCenter(); // fallback caso não haja spawns
			
		if (player.isDead())
			player.doRevive();
		
		player.teleToLocation(loc, 20);
	}
	
	public static void TeleportSoloZone(Player activeChar)
	{
		// Lista de locais de teleporte
		final int[][] teleportLocations =
		{
			Config.GK_SPAWN_SOLOFARM_1,
			Config.GK_SPAWN_SOLOFARM_2,
			Config.GK_SPAWN_SOLOFARM_3,
			Config.GK_SPAWN_SOLOFARM_4,
			Config.GK_SPAWN_SOLOFARM_5,
			Config.GK_SPAWN_SOLOFARM_6
		};
		
		int[] loc = teleportLocations[Rnd.get(teleportLocations.length)];
		
		if (Config.OPEN_EFFECT_CLASSIC_TELEPORTER)
		{
			MagicSkillUse MSU = new MagicSkillUse(activeChar, activeChar, 2036, 1, 200, 0);
			activeChar.broadcastPacket(MSU);
			activeChar.sendPacket(MSU);
			
			executorService.submit(() -> {
				try
				{
					TimeUnit.MILLISECONDS.sleep(450); // Espera o efeito
					activeChar.teleToLocation(loc[0], loc[1], loc[2], 0);
				}
				catch (InterruptedException e)
				{
					Thread.currentThread().interrupt();
				}
			});
		}
		else
		{
			activeChar.teleToLocation(loc[0], loc[1], loc[2], 0);
		}
	}
	
	// Shutdown the executor service properly to avoid memory leaks (in your main game loop or shutdown routine)
	public static void shutdownExecutor()
	{
		executorService.shutdown();
		try
		{
			if (!executorService.awaitTermination(60, TimeUnit.SECONDS))
			{
				executorService.shutdownNow();
			}
		}
		catch (InterruptedException e)
		{
			executorService.shutdownNow();
		}
	}
	
	private static final ExecutorService executorService = Executors.newCachedThreadPool();
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String filename = "";
		if (val == 0)
			filename = "" + npcId;
		else
			filename = npcId + "-" + val;
		
		return "data/html/teleporter/" + filename + ".htm";
	}
	
	private void showHalfPriceHtml(Player player)
	{
		if (player == null)
			return;
		
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		
		String content = HtmCache.getInstance().getHtm("data/html/teleporter/half/" + getNpcId() + ".htm");
		if (content == null)
			content = HtmCache.getInstance().getHtmForce("data/html/teleporter/" + getNpcId() + "-1.htm");
		
		html.setHtml(content);
		html.replace("%objectId%", getObjectId());
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
	
	@Override
	public void showChatWindow(Player player)
	{
		String filename = "data/html/teleporter/castleteleporter-no.htm";
		
		int condition = validateCondition(player);
		if (condition == COND_REGULAR)
		{
			super.showChatWindow(player);
			return;
		}
		else if (condition > COND_ALL_FALSE)
		{
			if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
				filename = "data/html/teleporter/castleteleporter-busy.htm"; // Busy because of siege
			else if (condition == COND_OWNER) // Clan owns castle
				filename = getHtmlPath(getNpcId(), 0); // Owner message window
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", getObjectId());
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
	
	private void doTeleport(Player player, int val)
	{
		L2TeleportLocation list = TeleportLocationTable.getInstance().getTemplate(val);
		if (list != null)
		{
			// you cannot teleport to village that is in siege
			if (SiegeManager.getSiege(list.getLocX(), list.getLocY(), list.getLocZ()) != null)
			{
				player.sendPacket(SystemMessageId.NO_PORT_THAT_IS_IN_SIGE);
				return;
			}
			
			if (MapRegionTable.townHasCastleInSiege(list.getLocX(), list.getLocY()) && isInsideZone(ZoneId.TOWN))
			{
				player.sendPacket(SystemMessageId.NO_PORT_THAT_IS_IN_SIGE);
				return;
			}
			
			if (!Config.KARMA_PLAYER_CAN_USE_GK && player.getKarma() > 0) // karma
			{
				player.sendMessage("Go away, you're not welcome here.");
				return;
			}
			
			if (list.getIsForNoble() && !player.isNoble())
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/html/teleporter/nobleteleporter-no.htm");
				html.replace("%objectId%", getObjectId());
				html.replace("%npcname%", getName());
				player.sendPacket(html);
				return;
			}
			
			if (player.isAlikeDead())
				return;
			
			Calendar cal = Calendar.getInstance();
			int price = list.getPrice();
			
			if (!list.getIsForNoble())
			{
				if (cal.get(Calendar.HOUR_OF_DAY) >= 20 && cal.get(Calendar.HOUR_OF_DAY) <= 23 && (cal.get(Calendar.DAY_OF_WEEK) == 1 || cal.get(Calendar.DAY_OF_WEEK) == 7))
					price /= 2;
			}
			
			// if (Config.ALT_GAME_FREE_TELEPORT || player.destroyItemByItemId("Teleport " + (list.getIsForNoble() ? " nobless" : ""), 57, price, this, true))
			// if (Config.ALT_GAME_FREE_TELEPORT && player.getLevel() <= Config.FREE_TELEPORT_UNTIL || player.destroyItemByItemId("Teleport ", (list.getIsForNoble()) ? 6651 : 57, price, this, true))
			// {
			// if(Config.OPEN_EFFECT_CLASSIC_TELEPORTER)
			// {
			// MagicSkillUse MSU = new MagicSkillUse(player, player, 2036, 1, 1, 0);
			// player.broadcastPacket(MSU);
			// player.sendPacket(MSU);
			// ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			// {
			// @Override
			// public void run()
			// {
			// //player.teleToLocation(list, 20);
			// player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ(), 20);
			// // you cannot teleport to village that is in siege
			// if (player.isArenaProtection())
			// {
			// player.sendMessage("You cannot teleport when registered on tournament.");
			// return;
			// }
			// }
			// }, 450);
			// }
			// else
			// //player.teleToLocation(list, 20);
			// player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ(), 20);
			//
			// // you cannot teleport to village that is in siege
			// if (player.isArenaProtection())
			// {
			// player.sendMessage("You cannot teleport when registered on tournament.");
			// return;
			// }
			// }
			// //player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ(), 20);
			// }
			// else
			// _log.warning("No teleport destination with id:" + val);
			//
			// player.sendPacket(ActionFailed.STATIC_PACKET);
			// }
			if (Config.ALT_GAME_FREE_TELEPORT && player.getLevel() <= Config.FREE_TELEPORT_UNTIL || player.destroyItemByItemId("Teleport", (list.getIsForNoble()) ? 6651 : 57, price, this, true))
			{
				if (Config.OPEN_EFFECT_CLASSIC_TELEPORTER)
				{
					// Criando e enviando o efeito de teleporte (habilidade 2036)
					MagicSkillUse MSU = new MagicSkillUse(player, player, 2036, 1, 1, 0);
					player.broadcastPacket(MSU);
					player.sendPacket(MSU);
					
					// Usando ExecutorService em vez de ThreadPoolManager
					executorService.submit(() -> {
						try
						{
							// Espera 450ms para o efeito de teleporte
							TimeUnit.MILLISECONDS.sleep(450);
							
							// Verifica se o jogador está com proteção de arena antes de teleportar
							if (player.isArenaProtection())
							{
								player.sendMessage("You cannot teleport when registered on tournament.");
								return;
							}
							
							// Teleporta o jogador para a nova localização
							player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ(), 20);
						}
						catch (InterruptedException e)
						{
							Thread.currentThread().interrupt(); // Interrompe se o thread for cancelado
						}
					});
				}
				else
				{
					// Caso o efeito de teleporte não esteja habilitado, faz o teleporte diretamente
					player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ(), 20);
				}
			}
			else
			{
				// Se não for possível teleportar (sem saldo ou condição não atendida)
				_log.warning("No teleport destination with id:" + val);
			}
			
			// Garantindo que o pacote de falha de ação seja enviado
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	private int validateCondition(Player player)
	{
		if (CastleManager.getInstance().getCastleIndex(this) < 0) // Teleporter isn't on castle ground
			return COND_REGULAR; // Regular access
			
		if (getCastle().getSiege().isInProgress()) // Teleporter is on castle ground and siege is in progress
			return COND_BUSY_BECAUSE_OF_SIEGE; // Busy because of siege
			
		if (player.getClan() != null && getCastle().getOwnerId() == player.getClanId()) // Teleporter is on castle ground and player is in a clan
			return COND_OWNER;
		
		return COND_ALL_FALSE;
	}
}