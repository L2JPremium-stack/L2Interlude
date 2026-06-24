package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MoveToPawn;
import net.sf.l2j.solofarm.instancemanager.SoloFarmManager;

/**
 * @author BAN-L2JDEV
 */
public class L2SoloFarmInstance extends L2NpcInstance
{
	public L2SoloFarmInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
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
			// Check if the player is attackable (without a forced attack) and isn't dead
			if (isAutoAttackable(player))
			{
				player.getAI().setIntention(CtrlIntention.ATTACK, this);
			}
			else
			{
				// Calculate the distance between the L2PcInstance and the L2Npc
				if (!canInteract(player))
				{
					// Notify the L2PcInstance AI with INTERACT
					player.getAI().setIntention(CtrlIntention.INTERACT, this);
				}
				else
				{
					// Rotate the player to face the instance
					player.sendPacket(new MoveToPawn(player, this, L2Npc.INTERACTION_DISTANCE));
					
					// Send ActionFailed to the player in order to avoid he stucks
					player.sendPacket(ActionFailed.STATIC_PACKET);
					
					if (hasRandomAnimation())
						onRandomAnimation(Rnd.get(8));
					
					SoloFarmManager.getInstance().showMainPage(player);
				}
			}
		}
	}
}