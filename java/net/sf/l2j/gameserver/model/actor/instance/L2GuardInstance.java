package net.sf.l2j.gameserver.model.actor.instance;

import java.util.List;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.knownlist.GuardKnownList;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MoveToPawn;
import net.sf.l2j.gameserver.scriptings.EventType;
import net.sf.l2j.gameserver.scriptings.Quest;
import net.sf.l2j.gameserver.taskmanager.GuardAntPkTaskManager;

/**
 * This class manages all Guards in the world.<br>
 * It inherits all methods from L2Attackable and adds some more such as:
 * <ul>
 * <li>tracking PK</li>
 * <li>aggressive L2MonsterInstance.</li>
 * </ul>
 */
public final class L2GuardInstance extends Attackable
{
	public L2GuardInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void initKnownList()
	{
		setKnownList(new GuardKnownList(this));
	}
	
	@Override
	public final GuardKnownList getKnownList()
	{
		return (GuardKnownList) super.getKnownList();
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return attacker instanceof L2MonsterInstance;
	}
	
	@Override
	public void onSpawn()
	{
		
		super.onSpawn();
		setIsNoRndWalk(true);
		GuardAntPkTaskManager.getInstance().add(this);
	}
	
	@Override
	public void deleteMe()
	{
		
		super.deleteMe();
		GuardAntPkTaskManager.getInstance().remove(this);
	}
	
	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String filename = "";
		if (val == 0)
			filename = "" + npcId;
		else
			filename = npcId + "-" + val;
		
		return "data/html/guard/" + filename + ".htm";
	}
	
	@Override
	public void onAction(Player player)
	{
		// Set the target of the L2PcInstance player
		if (player.getTarget() != this)
		{
			player.setTarget(this);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			// Calculate the distance between the L2PcInstance and the L2Npc
			if (!canInteract(player))
			{
				// Set the L2PcInstance Intention to INTERACT
				player.getAI().setIntention(CtrlIntention.INTERACT, this);
			}
			else
			{
				// Some guards have no HTMs on retail. Bypass the chat window if such guard is met.
				switch (getNpcId())
				{
					case 31671:
					case 31672:
					case 31673:
					case 31674:
						// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
						player.sendPacket(ActionFailed.STATIC_PACKET);
						return;
				}
				
				// Rotate the player to face the instance
				player.sendPacket(new MoveToPawn(player, this, L2Npc.INTERACTION_DISTANCE));
				
				if (hasRandomAnimation())
					onRandomAnimation(Rnd.get(8));
				
				List<Quest> qlsa = getTemplate().getEventQuests(EventType.QUEST_START);
				if (qlsa != null && !qlsa.isEmpty())
					player.setLastQuestNpcObject(getObjectId());
				
				List<Quest> qlst = getTemplate().getEventQuests(EventType.ON_FIRST_TALK);
				if (qlst != null && qlst.size() == 1)
					qlst.get(0).notifyFirstTalk(this, player);
				else
					showChatWindow(player);
			}
		}
	}
	
	@Override
	public boolean isGuard()
	{
		return true;
	}
	
	@Override
	public int getDriftRange()
	{
		return 20;
	}
}