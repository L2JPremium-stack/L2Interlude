package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;

public final class RequestPrivateStoreManageSell extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if (player == null)
			return;
		
		if (player.isOlympiadProtection() || player.isInCombat() || player.isInOlympiadMode() || player.isDead() || player.isMoving() || player.isActiveAgathion())
		{
			player.sendMessage("You can not Combat,Moviment,Activated Agathion,Olympiads etc.");
			return;
		}
		
		player.tryOpenPrivateSellStore(false);
	}
}