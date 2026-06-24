/**
 *
 * @author FBIagent
 *
 */
package net.sf.l2j.gameserver.handler.itemhandlers;

import java.util.logging.Level;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.event.bossevent.KTBEvent;
import net.sf.l2j.event.ctf.CTFEvent;
import net.sf.l2j.event.deathmath.DMEvent;
import net.sf.l2j.event.fortress.FOSEvent;
import net.sf.l2j.event.lastman.LMEvent;
import net.sf.l2j.event.tvt.TvTEvent;
import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SummonItemsData;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.instance.L2AgathionInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2XmassTreeInstance;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.item.SummonItem;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillLaunched;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.PetItemList;
import net.sf.l2j.gameserver.network.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Broadcast;

public class SummonItems implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;
		
		final Player activeChar = (Player) playable;
		
		if (activeChar.isSitting())
		{
			activeChar.sendPacket(SystemMessageId.CANT_MOVE_SITTING);
			return;
		}
		if (!TvTEvent.onItemSummon(playable.getObjectId())  || !CTFEvent.onItemSummon(playable.getObjectId()) || !LMEvent.onItemSummon(playable.getObjectId()) || !DMEvent.onItemSummon(playable.getObjectId()) || !FOSEvent.onItemSummon(playable.getObjectId()))
			return;
		if (activeChar.inObserverMode())
			return;
		
		if (activeChar.isAllSkillsDisabled() || activeChar.isCastingNow())
			return;
		
		final SummonItem sitem = SummonItemsData.getInstance().getSummonItem(item.getItemId());
		
		if ((activeChar.getPet() != null || activeChar.isMounted()) && sitem.isPetSummon())
		{
			activeChar.sendPacket(SystemMessageId.SUMMON_ONLY_ONE);
			return;
		}
		if (!KTBEvent.onItemSummon(playable.getObjectId()))
		{
			activeChar.sendMessage("You can not do this in KTB Event");
			return;
		}
		if (!DMEvent.onItemSummon(playable.getObjectId()))
		{
			activeChar.sendMessage("You can not do this in DM Event");
			return;
		}
		if (!LMEvent.onItemSummon(playable.getObjectId()))
		{
			activeChar.sendMessage("You can not do this in LM Event");
			return;
		}
		if (!FOSEvent.onItemSummon(playable.getObjectId()))
		{
			activeChar.sendMessage("You can not do this in FOS Event");
			return;
		}
		if (activeChar.isArenaProtection())
		{
			activeChar.sendMessage("You can not do this in Tournament Event");
			return;
		}
		if (activeChar.isAttackingNow())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT);
			return;
		}
		if (activeChar.isInCombat())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT);
			return;
		}
		final int npcId = sitem.getNpcId();
		if (npcId == 0)
			return;
		
		final NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcId);
		if (npcTemplate == null)
			return;
		
		activeChar.stopMove(null);
		
		switch (sitem.getType())
		{
			case 0: // static summons (like Christmas tree)
				try
				{
					for (L2XmassTreeInstance ch : activeChar.getKnownList().getKnownTypeInRadius(L2XmassTreeInstance.class, 1200))
					{
						if (npcTemplate.getNpcId() == L2XmassTreeInstance.SPECIAL_TREE_ID)
						{
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANNOT_SUMMON_S1_AGAIN).addCharName(ch));
							return;
						}
					}
					if (sitem.getItemId() == 5560 || sitem.getItemId() == 5561)
					{
						if (activeChar.destroyItem("Summon", item.getObjectId(), 1, null, false))
						{
							final L2Spawn spawn = new L2Spawn(npcTemplate);
							spawn.setLoc(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getHeading());
							spawn.setRespawnState(false);
							
							final L2Npc npc = spawn.doSpawn(true);
							npc.setTitle(activeChar.getName());
							npc.setIsRunning(false); // broadcast info
						}
					}
					else
					{
						
						if ((activeChar.getAgathionId() > 0))
						{
						    activeChar.unSummonAgathion();
						    activeChar.sendMessage("Agathion is Disabled.");
						    activeChar.lostAgathionSkills();
						    activeChar.setActiveAgathion(false);
						}
						else
						{
						    final L2Spawn spawn = new L2Spawn(npcTemplate);
						    
						    // Calculando a posição lateral aleatória
						    final double angle = Math.toRadians(Rnd.get(60, 120)) * (Rnd.nextBoolean() ? 1 : -1); // Movendo para a esquerda ou direita
						    final int offsetX = (int) (70 * Math.cos(angle)); // Distância horizontal
						    final int offsetY = (int) (70 * Math.sin(angle)); // Distância vertical

						    // Ajustando a posição final do Agathion, levando em conta os cálculos de offset
						    spawn.setLoc(activeChar.getX() + offsetX, activeChar.getY() + offsetY, activeChar.getZ() + 20, -1);

						    final L2Npc npc = spawn.doSpawn(true);
						    if (npc instanceof L2AgathionInstance)
						    {
						        ((L2AgathionInstance) npc).broadcastNpcInfo(2);
						        npc.setShowSummonAnimation(true);
						        ((L2AgathionInstance) npc).setOwner(activeChar);
						        npc.setIsInvul(true);
						        npc.setRunning();
						        activeChar.setAgathion(((L2AgathionInstance) npc));
						        activeChar.setAgathionId(npc.getNpcId());
						        ((L2AgathionInstance) npc).startAgathionTask();
						        ((L2AgathionInstance) npc).setTargetable(false);
						        ((L2AgathionInstance) npc).setFollowStatus(true);
						        activeChar.setActiveAgathion(true);
						        activeChar.rewardAgathionSkills();
						        activeChar.sendMessage("Agathion is Enabled.");
						    }
						}
					}
					
				}
				catch (Exception e)
				{
					activeChar.sendPacket(SystemMessageId.TARGET_CANT_FOUND);
				}
				break;
			case 1: // pet summons
				final L2Object oldTarget = activeChar.getTarget();
				activeChar.setTarget(activeChar);
				Broadcast.toSelfAndKnownPlayers(activeChar, new MagicSkillUse(activeChar, 2046, 1, 5000, 0));
				activeChar.setTarget(oldTarget);
				activeChar.sendPacket(new SetupGauge(0, 5000));
				activeChar.sendPacket(SystemMessageId.SUMMON_A_PET);
				activeChar.setIsCastingNow(true);
				
				ThreadPool.schedule(new PetSummonFinalizer(activeChar, npcTemplate, item), 5000);
				break;
			case 2: // wyvern
				activeChar.mount(sitem.getNpcId(), item.getObjectId(), true);
				break;
		}
	}
	
	// TODO: this should be inside skill handler
	static class PetSummonFinalizer implements Runnable
	{
		private final Player _activeChar;
		private final ItemInstance _item;
		private final NpcTemplate _npcTemplate;
		
		PetSummonFinalizer(Player activeChar, NpcTemplate npcTemplate, ItemInstance item)
		{
			_activeChar = activeChar;
			_npcTemplate = npcTemplate;
			_item = item;
		}
		
		@Override
		public void run()
		{
			try
			{
				_activeChar.sendPacket(new MagicSkillLaunched(_activeChar, 2046, 1));
				_activeChar.setIsCastingNow(false);
				
				// check for summon item validity
				if (_item == null || _item.getOwnerId() != _activeChar.getObjectId() || _item.getLocation() != ItemInstance.ItemLocation.INVENTORY)
					return;
				
				final L2PetInstance pet = L2PetInstance.spawnPet(_npcTemplate, _activeChar, _item);
				if (pet == null)
					return;
				
				pet.setShowSummonAnimation(true);
				
				if (!pet.isRespawned())
				{
					pet.setCurrentHp(pet.getMaxHp());
					pet.setCurrentMp(pet.getMaxMp());
					pet.getStat().setExp(pet.getExpForThisLevel());
					pet.setCurrentFed(pet.getPetData().getMaxMeal());
				}
				
				pet.setRunning();
				
				if (!pet.isRespawned())
					pet.store();
				
				_activeChar.setPet(pet);
				
				pet.spawnMe(_activeChar.getX() + 50, _activeChar.getY() + 100, _activeChar.getZ());
				pet.startFeed();
				_item.setEnchantLevel(pet.getLevel());
				
				pet.setFollowStatus(true);
				
				pet.getOwner().sendPacket(new PetItemList(pet));
				pet.broadcastStatusUpdate();
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}
}
