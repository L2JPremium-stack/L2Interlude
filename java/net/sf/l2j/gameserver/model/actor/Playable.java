package net.sf.l2j.gameserver.model.actor;

import net.sf.l2j.Config;
import net.sf.l2j.event.bossevent.KTBEvent;
import net.sf.l2j.event.ctf.CTFEvent;
import net.sf.l2j.event.deathmath.DMEvent;
import net.sf.l2j.event.fortress.FOSEvent;
import net.sf.l2j.event.lastman.LMEvent;
import net.sf.l2j.event.tvt.TvTEvent;
import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2WorldRegion;
import net.sf.l2j.gameserver.model.actor.stat.PlayableStat;
import net.sf.l2j.gameserver.model.actor.status.PlayableStatus;
import net.sf.l2j.gameserver.model.actor.template.CharTemplate;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.model.zone.type.L2TimeFarmZone;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.Revive;
import net.sf.l2j.gameserver.scriptings.QuestState;
import net.sf.l2j.gameserver.templates.skills.L2EffectFlag;
import net.sf.l2j.gameserver.templates.skills.L2EffectType;
import net.sf.l2j.gameserver.util.Broadcast;
import net.sf.l2j.timezone.TimeFarmZoneManager;

/**
 * This class represents all Playable characters in the world.<BR>
 * <BR>
 * L2Playable :<BR>
 * <BR>
 * <li>L2PcInstance</li> <li>L2Summon</li><BR>
 * <BR>
 */
public abstract class Playable extends Creature
{
	/**
	 * Constructor of L2Playable (use L2Character constructor).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Call the L2Character constructor to create an empty _skills slot and link copy basic Calculator set to this L2Playable</li><BR>
	 * <BR>
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2CharTemplate to apply to the L2Playable
	 */
	public Playable(int objectId, CharTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void initCharStat()
	{
		setStat(new PlayableStat(this));
	}
	
	@Override
	public PlayableStat getStat()
	{
		return (PlayableStat) super.getStat();
	}
	
	@Override
	public void initCharStatus()
	{
		setStatus(new PlayableStatus(this));
	}
	
	@Override
	public PlayableStatus getStatus()
	{
		return (PlayableStatus) super.getStatus();
	}
	
	@Override
	public void onActionShift(Player player)
	{
		if (!TvTEvent.onAction(player, getObjectId()))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (!CTFEvent.onAction(player, getObjectId()))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (!FOSEvent.onAction(player, getObjectId()))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (!LMEvent.onAction(player, getObjectId()))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (!DMEvent.onAction(player, getObjectId()))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (!KTBEvent.onAction(player, getObjectId()))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (TvTEvent.isPlayerParticipant(player.getObjectId()) && TvTEvent.isStarted() && !TvTEvent.isPlayerParticipant(player.getObjectId()))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (CTFEvent.isPlayerParticipant(player.getObjectId()) && FOSEvent.isStarted() && !CTFEvent.isPlayerParticipant(player.getObjectId()))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (FOSEvent.isPlayerParticipant(player.getObjectId()) && FOSEvent.isStarted() && !FOSEvent.isPlayerParticipant(player.getObjectId()))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (KTBEvent.isPlayerParticipant(player.getObjectId()) && KTBEvent.isStarted() && !KTBEvent.isPlayerParticipant(player.getObjectId()))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (DMEvent.isPlayerParticipant(player.getObjectId()) && DMEvent.isStarted() && !DMEvent.isPlayerParticipant(player.getObjectId()))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		
		if (player.getTarget() != this)
		{
			player.setTarget(this);	
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
			player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public boolean doDie(Creature killer)
	{
	    // killing is only possible one time
	    synchronized (this)
	    {
	        if (isDead())
	            return false;

	        // now reset currentHp to zero
	        setCurrentHp(0);

	        setIsDead(true);
	    }

	    // Set target to null and cancel Attack or Cast
	    setTarget(null);

	    // Stop movement
	    stopMove(null);

	    // Stop HP/MP/CP Regeneration task
	    getStatus().stopHpMpRegeneration();

	    // Stop all active skills effects in progress
	    if (isPhoenixBlessed())
	    {
	        // remove Lucky Charm if player has SoulOfThePhoenix/Salvation buff
	        if (getCharmOfLuck())
	            stopCharmOfLuck(null);
	        if (isNoblesseBlessed())
	            stopNoblesseBlessing(null);
	    }
	    // Same thing if the Character isn't a Noblesse Blessed L2Playable
	    else if (isNoblesseBlessed())
	    {
	        stopNoblesseBlessing(null);

	        // remove Lucky Charm if player have Nobless blessing buff
	        if (getCharmOfLuck())
	            stopCharmOfLuck(null);
	    }

	    else if (this instanceof Player)
	    {
	    	 boolean loseBuffs = false;

	 	    for (L2TimeFarmZone zone : TimeFarmZoneManager.getZones())
	 	    {
	 	        if (zone.isInsideZone(this) && zone.canLoseBuff())
	 	        {
	 	            loseBuffs = true;
	 	            break;
	 	        }
	 	    }

	    	
	    	final Player player = (Player) this;
	    	
	    	if ((player.isArenaAttack() && !Config.ALLOW_TOURNAMENT_DIE_LEAVE_BUFF) || (TvTEvent.isPlayerParticipant(player.getObjectId()) && TvTEvent.isStarted() && !Config.TVT_DIE_LEAVE_BUFF) || (CTFEvent.isPlayerParticipant(player.getObjectId()) && CTFEvent.isStarted() && !Config.CTF_DIE_LEAVE_BUFF) || (DMEvent.isPlayerParticipant(player.getObjectId()) && DMEvent.isStarted() && !Config.DM_DIE_LEAVE_BUFF) || (LMEvent.isPlayerParticipant(player.getObjectId()) && LMEvent.isStarted() && !Config.LM_DIE_LEAVE_BUFF) || (FOSEvent.isPlayerParticipant(player.getObjectId()) && FOSEvent.isStarted() && !Config.FOS_DIE_LEAVE_BUFF) || (player.isInsideZone(ZoneId.PVP_CUSTOM) && !Config.PVPZONE_DIE_LEAVE_BUFF) || (player.isInsideZone(ZoneId.RAID_ZONE) && !Config.RAIDZONE_DIE_LEAVE_BUFF) || (player.isInsideZone(ZoneId.BOSS) && !Config.BOSSZONE_DIE_LEAVE_BUFF) || (KTBEvent.isPlayerParticipant(player.getObjectId()) && KTBEvent.isStarted() && !Config.KTB_DIE_LEAVE_BUFF) || (player.isInsideZone(ZoneId.FLAG) && !Config.FLAGZONE_DIE_LEAVE_BUFF) || loseBuffs)
	    	{
	    		// Todas configs aqui quando estao false em seus eventos, irao manter os buffs
	    	}
	    	else
	    	{
	    		if(Config.LEAVE_ALL_BUFFS_WORLD_DIE) //Quando esta false, nao perde buff em lugar algum
	    		{
	    			// Remove os buffs fora da arena ou dentro da arena se a configuração desativar
	    			stopAllEffectsExceptThoseThatLastThroughDeath();
	    			// Restante da lógica
	    			if (killer instanceof Player)
	    			{
	    				if (player.getClan() != null)
	    					Broadcast.PMAnnounce(player.getName() + " from clan " + player.getClan().getName() + " took SELF! ^^");
	    				else
	    					Broadcast.PMAnnounce(player.getName() + " took SELF! ^^");
	    			}	
	    		}
	    		
	    	}
	    }
	    
	    
	    // Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
	    broadcastStatusUpdate();

	    // Notify L2Character AI
	    getAI().notifyEvent(CtrlEvent.EVT_DEAD);

	    final L2WorldRegion region = getRegion();
	    if (region != null)
	        region.onDeath(this);

	    // Notify Quest of L2Playable's death
	    final Player actingPlayer = getActingPlayer();
	    for (QuestState qs : actingPlayer.getNotifyQuestOfDeath())
	        qs.getQuest().notifyDeath((killer == null ? this : killer), actingPlayer);


	    return true;
	}
	
	@Override
	public void doRevive()
	{
		if (!isDead() || isTeleporting())
			return;
		
		setIsDead(false);
		
		if (isPhoenixBlessed())
		{
			stopPhoenixBlessing(null);
			
			getStatus().setCurrentHp(getMaxHp());
			getStatus().setCurrentMp(getMaxMp());
		}
		else
			getStatus().setCurrentHp(getMaxHp() * Config.RESPAWN_RESTORE_HP);
		
		// Start broadcast status
		broadcastPacket(new Revive(this));
		
		final L2WorldRegion region = getRegion();
		if (region != null)
			region.onRevive(this);
	}
	
	public boolean checkIfPvP(Playable target)
	{
		if (target == null || target == this)
			return false;
		
		final Player player = getActingPlayer();
		if (player == null || player.getKarma() != 0)
			return false;
		
		final Player targetPlayer = target.getActingPlayer();
		if (targetPlayer == null || targetPlayer == this)
			return false;
		
		if (targetPlayer.getKarma() != 0 || targetPlayer.getPvpFlag() == 0)
			return false;
		
		return true;
	}
	
	/**
	 * Return True.
	 */
	@Override
	public boolean isAttackable()
	{
		return true;
	}
	
	/**
	 * <B><U> Overridden in </U> :</B>
	 * <ul>
	 * <li>L2Summon</li>
	 * <li>L2PcInstance</li>
	 * </ul>
	 * @param id The system message to send to player.
	 */
	public void sendPacket(SystemMessageId id)
	{
		// default implementation
	}
	
	// Support for Noblesse Blessing skill, where buffs are retained after resurrect
	public final boolean isNoblesseBlessed()
	{
		return _effects.isAffected(L2EffectFlag.NOBLESS_BLESSING);
	}
	
	public final void stopNoblesseBlessing(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2EffectType.NOBLESSE_BLESSING);
		else
			removeEffect(effect);
		updateAbnormalEffect();
	}
	
	// Support for Soul of the Phoenix and Salvation skills
	public final boolean isPhoenixBlessed()
	{
		return _effects.isAffected(L2EffectFlag.PHOENIX_BLESSING);
	}
	
	public final void stopPhoenixBlessing(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2EffectType.PHOENIX_BLESSING);
		else
			removeEffect(effect);
		
		updateAbnormalEffect();
	}
	
	/**
	 * @return True if the Silent Moving mode is active.
	 */
	public boolean isSilentMoving()
	{
		return _effects.isAffected(L2EffectFlag.SILENT_MOVE);
	}
	
	// for Newbie Protection Blessing skill, keeps you safe from an attack by a chaotic character >= 10 levels apart from you
	public final boolean getProtectionBlessing()
	{
		return _effects.isAffected(L2EffectFlag.PROTECTION_BLESSING);
	}
	
	public void stopProtectionBlessing(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2EffectType.PROTECTION_BLESSING);
		else
			removeEffect(effect);
		
		updateAbnormalEffect();
	}
	
	// Charm of Luck - During a Raid/Boss war, decreased chance for death penalty
	public final boolean getCharmOfLuck()
	{
		return _effects.isAffected(L2EffectFlag.CHARM_OF_LUCK);
	}
	
	public final void stopCharmOfLuck(L2Effect effect)
	{
		if (effect == null)
			stopEffects(L2EffectType.CHARM_OF_LUCK);
		else
			removeEffect(effect);
		
		updateAbnormalEffect();
	}
	
	@Override
	public void updateEffectIcons(boolean partyOnly)
	{
		_effects.updateEffectIcons(partyOnly);
	}
	
	/**
	 * This method allows to easily send relations. Overridden in L2Summon and L2PcInstance.
	 */
	public void broadcastRelationsChanges()
	{
	}
	
	@Override
	public boolean isInArena()
	{
		return isInsideZone(ZoneId.PVP) && !isInsideZone(ZoneId.SIEGE);
	}
	
	public abstract void doPickupItem(L2Object object);
	
	public abstract int getKarma();
	
	public abstract byte getPvpFlag();
	
	public abstract boolean useMagic(L2Skill skill, boolean forceUse, boolean dontMove);
}