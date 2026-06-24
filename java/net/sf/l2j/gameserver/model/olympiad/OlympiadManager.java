package net.sf.l2j.gameserver.model.olympiad;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import net.sf.l2j.Config;
import net.sf.l2j.event.bossevent.KTBEvent;
import net.sf.l2j.event.ctf.CTFEvent;
import net.sf.l2j.event.deathmath.DMEvent;
import net.sf.l2j.event.fortress.FOSEvent;
import net.sf.l2j.event.lastman.LMEvent;
import net.sf.l2j.event.tvt.TvTEvent;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.type.CrystalType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.StatsSet;

/**
 * @author DS
 */
public class OlympiadManager
{
	private final List<Integer> _nonClassBasedRegisters;
	private final Map<Integer, List<Integer>> _classBasedRegisters;
	
	protected OlympiadManager()
	{
		_nonClassBasedRegisters = new CopyOnWriteArrayList<>();
		_classBasedRegisters = new ConcurrentHashMap<>();
	}
	
	public static final OlympiadManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public final List<Integer> getRegisteredNonClassBased()
	{
		return _nonClassBasedRegisters;
	}
	
	public final Map<Integer, List<Integer>> getRegisteredClassBased()
	{
		return _classBasedRegisters;
	}
	
	protected final List<List<Integer>> hasEnoughRegisteredClassed()
	{
		cleanupRegisteredInvalid();
		List<List<Integer>> result = null;
		for (Map.Entry<Integer, List<Integer>> classList : _classBasedRegisters.entrySet())
		{
			if (classList.getValue() != null && classList.getValue().size() >= Config.ALT_OLY_CLASSED)
			{
				if (result == null)
					result = new ArrayList<>();
				
				result.add(classList.getValue());
			}
		}
		return result;
	}
	
	protected final boolean hasEnoughRegisteredNonClassed()
	{
		cleanupRegisteredInvalid();
		return _nonClassBasedRegisters.size() >= Config.ALT_OLY_NONCLASSED;
	}
	
	/**
	 * Removes offline/stale object ids from olympiad waiting lists.
	 * Registration lists must not keep ids forever when a player logs out,
	 * crashes or is removed before the game manager consumes the list.
	 */
	private final void cleanupRegisteredInvalid()
	{
		for (Integer objId : _nonClassBasedRegisters)
		{
			final Player player = L2World.getInstance().getPlayer(objId);
			if (player == null || !player.isOnline())
				_nonClassBasedRegisters.remove(objId);
		}
		
		for (Map.Entry<Integer, List<Integer>> entry : _classBasedRegisters.entrySet())
		{
			final List<Integer> list = entry.getValue();
			if (list == null || list.isEmpty())
			{
				_classBasedRegisters.remove(entry.getKey());
				continue;
			}
			
			for (Integer objId : list)
			{
				final Player player = L2World.getInstance().getPlayer(objId);
				if (player == null || !player.isOnline())
					list.remove(objId);
			}
			
			if (list.isEmpty())
				_classBasedRegisters.remove(entry.getKey());
		}
	}
	
	protected final void clearRegistered()
	{
		_nonClassBasedRegisters.clear();
		_classBasedRegisters.clear();
	}
	
	public final boolean isRegistered(Player noble)
	{
		return isRegistered(noble, false);
	}
	
	private final boolean isRegistered(Player player, boolean showMessage)
	{
		final Integer objId = Integer.valueOf(player.getObjectId());
		
		if (_nonClassBasedRegisters.contains(objId))
		{
			if (showMessage)
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_NO_CLASS_GAMES));
			
			return true;
		}
		
		final List<Integer> classed = _classBasedRegisters.get(player.getBaseClass());
		if (classed != null && classed.contains(objId))
		{
			if (showMessage)
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_CLASSIFIED_GAMES));
			
			return true;
		}
		
		return false;
	}
	
	public final boolean isRegisteredInComp(Player noble)
	{
		return isRegistered(noble, false) || isInCompetition(noble, false);
	}
	
	private static final boolean isInCompetition(Player player, boolean showMessage)
	{
		if (!Olympiad._inCompPeriod)
			return false;
		
		for (int i = OlympiadGameManager.getInstance().getNumberOfStadiums(); --i >= 0;)
		{
			AbstractOlympiadGame game = OlympiadGameManager.getInstance().getOlympiadTask(i).getGame();
			if (game == null)
				continue;
			
			if (game.containsParticipant(player.getObjectId()))
			{
				if (showMessage)
					player.sendPacket(SystemMessageId.YOU_HAVE_ALREADY_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_AN_EVENT);
				
				return true;
			}
		}
		return false;
	}
	
	public final boolean registerNoble(Player player, CompetitionType type)
	{
		cleanupRegisteredInvalid();
		if (!Config.OLY_FIGHT)
		{
			player.sendMessage("The Olympiad Game Is disabled...");
			return false;
		}
		if (!Olympiad._inCompPeriod)
		{
			player.sendPacket(SystemMessageId.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
			return false;
		}
		// Olympiad dualbox protection
		if (player._active_boxes > 1 && !Config.ALLOW_DUALBOX_OLY)
		{
			final List<String> players_in_boxes = player.active_boxes_characters;
			
			if (players_in_boxes != null && players_in_boxes.size() > 1)
				for (final String character_name : players_in_boxes)
				{
					final Player activeChar = L2World.getInstance().getPlayer(character_name);
					if (activeChar != null && (activeChar.getOlympiadGameId() > 0 || activeChar.isInOlympiadMode() || OlympiadManager.getInstance().isRegistered(activeChar)))
					{
						activeChar.sendMessage("You are already participating in Olympiad with another char!");
						return false;
					}
				}
		}
		if (Config.OLLY_GRADE_A)
		{
			ItemInstance item;
			for (int i = 1; i < 15; i++)
			{
				item = player.getInventory().getPaperdollItem(i);
				if (item == null)
					continue;
				
				if (item.getItem().getCrystalType() == CrystalType.S && !Config.LISTID_RESTRICT_OLY.contains(item.getItemId()))
				{
					player.sendMessage("[Olympiad]: Olympiad (Grade-A) no custom!");
					return false;
				}
			}
		}
		if (Config.BLOCK_REGISTER_ITEMS_OLYMPIAD_ENCHANT)
		{
			ItemInstance item;
			for (int i = 1; i < 15; i++)
			{
				item = player.getInventory().getPaperdollItem(i);
				if (item == null)
					continue;
				
			     if (item.getEnchantLevel() > Config.ALT_OLY_ENCHANT_LIMIT)
			      {
			         player.sendMessage("You can not register olympiad when item enchant level is above +" + Config.ALT_OLY_ENCHANT_LIMIT + ".");
			         return false;
			      }
			}
		}
		if (Olympiad.getInstance().getMillisToCompEnd() < 600000)
		{
			player.sendPacket(SystemMessageId.GAME_REQUEST_CANNOT_BE_MADE);
			return false;
		}
		
		switch (type)
		{
			case CLASSED:
			{
				if (!checkNoble(player))
					return false;
				
				List<Integer> classed = _classBasedRegisters.get(player.getBaseClass());
				if (classed != null)
					classed.add(player.getObjectId());
				else
				{
					classed = new CopyOnWriteArrayList<>();
					classed.add(player.getObjectId());
					_classBasedRegisters.put(player.getBaseClass(), classed);
				}
				
				player.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_CLASSIFIED_GAMES);
				break;
			}
			
			case NON_CLASSED:
			{
				if (!checkNoble(player))
					return false;
				
				_nonClassBasedRegisters.add(player.getObjectId());
				player.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_NO_CLASS_GAMES);
				break;
			}
		}
		return true;
	}
	
	public final boolean unRegisterNoble(Player noble)
	{
		cleanupRegisteredInvalid();
		if (!Olympiad._inCompPeriod)
		{
			noble.sendPacket(SystemMessageId.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
			return false;
		}
		
		if (!noble.isNoble())
		{
			noble.sendPacket(SystemMessageId.NOBLESSE_ONLY);
			return false;
		}
		
		if (!isRegistered(noble, false))
		{
			noble.sendPacket(SystemMessageId.YOU_HAVE_NOT_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_A_GAME);
			return false;
		}
		
		if (isInCompetition(noble, false))
			return false;
		
		Integer objId = Integer.valueOf(noble.getObjectId());
		if (_nonClassBasedRegisters.remove(objId))
		{
			noble.sendPacket(SystemMessageId.YOU_HAVE_BEEN_DELETED_FROM_THE_WAITING_LIST_OF_A_GAME);
			return true;
		}
		
		final List<Integer> classed = _classBasedRegisters.get(noble.getBaseClass());
		if (classed != null && classed.remove(objId))
		{
			_classBasedRegisters.remove(noble.getBaseClass());
			_classBasedRegisters.put(noble.getBaseClass(), classed);
			
			noble.sendPacket(SystemMessageId.YOU_HAVE_BEEN_DELETED_FROM_THE_WAITING_LIST_OF_A_GAME);
			return true;
		}
		
		return false;
	}
	
	public final void removeDisconnectedCompetitor(Player player)
	{
		if (player == null)
			return;
		final OlympiadGameTask task = OlympiadGameManager.getInstance().getOlympiadTask(player.getOlympiadGameId());
		if (task != null && task.isGameStarted())
			task.getGame().handleDisconnect(player);
		
		final Integer objId = Integer.valueOf(player.getObjectId());
		if (_nonClassBasedRegisters.remove(objId))
			return;
		
		final List<Integer> classed = _classBasedRegisters.get(player.getBaseClass());
		if (classed != null && classed.remove(objId))
			return;
	}
	
	/**
	 * @param player - messages will be sent to this L2PcInstance
	 * @return true if all requirements are met
	 */
	private final boolean checkNoble(Player player)
	{
		if (!player.isNoble())
		{
			player.sendPacket(SystemMessageId.ONLY_NOBLESS_CAN_PARTICIPATE_IN_THE_OLYMPIAD);
			return false;
		}
		
		if (player.isSubClassActive())
		{
			player.sendPacket(SystemMessageId.YOU_CANT_JOIN_THE_OLYMPIAD_WITH_A_SUB_JOB_CHARACTER);
			return false;
		}
		
		if (player.isCursedWeaponEquipped())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_JOIN_OLYMPIAD_POSSESSING_S1).addItemName(player.getCursedWeaponEquippedId()));
			return false;
		}
		
		if (player.getInventoryLimit() * 0.8 <= player.getInventory().getSize())
		{
			player.sendPacket(SystemMessageId.SINCE_80_PERCENT_OR_MORE_OF_YOUR_INVENTORY_SLOTS_ARE_FULL_YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD);
			return false;
		}
		
		if (isRegistered(player, true))
			return false;
		
		if (isInCompetition(player, true))
			return false;
		
		final int charId = player.getObjectId();
		if (player.isAio() || player.isSellBuff() || TvTEvent.isPlayerParticipant(charId) || CTFEvent.isPlayerParticipant(charId) || KTBEvent.isPlayerParticipant(charId) || LMEvent.isPlayerParticipant(charId) || DMEvent.isPlayerParticipant(charId) || FOSEvent.isPlayerParticipant(charId))
		{
			player.sendMessage("You can't join olympiad while participating on Event.");
			return false;
		}
		StatsSet statDat = Olympiad.getNobleStats(player.getObjectId());
		if (statDat == null)
		{
			statDat = new StatsSet();
			statDat.set(Olympiad.CLASS_ID, player.getBaseClass());
			statDat.set(Olympiad.CHAR_NAME, player.getName());
			statDat.set(Olympiad.POINTS, Olympiad.DEFAULT_POINTS);
			statDat.set(Olympiad.COMP_DONE, 0);
			statDat.set(Olympiad.COMP_WON, 0);
			statDat.set(Olympiad.COMP_LOST, 0);
			statDat.set(Olympiad.COMP_DRAWN, 0);
			statDat.set("to_save", true);
			
			Olympiad.addNobleStats(player.getObjectId(), statDat);
		}
		
		final int points = Olympiad.getInstance().getNoblePoints(player.getObjectId());
		if (points <= 0)
		{
			final NpcHtmlMessage message = new NpcHtmlMessage(0);
			message.setFile("data/html/olympiad/noble_nopoints1.htm");
			message.replace("%objectId%", player.getTargetId());
			player.sendPacket(message);
			return false;
		}
		
		return true;
	}
	
	private static class SingletonHolder
	{
		protected static final OlympiadManager _instance = new OlympiadManager();
	}
}