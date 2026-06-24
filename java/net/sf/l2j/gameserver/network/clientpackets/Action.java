package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.ZoneId;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;

public final class Action extends L2GameClientPacket
{
	private int _objectId;
	@SuppressWarnings("unused")
	private int _originX, _originY, _originZ;
	private int _actionId;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_originX = readD();
		_originY = readD();
		_originZ = readD();
		_actionId = readC();
	}
	
	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		
		if (activeChar.inObserverMode())
		{
			activeChar.sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (activeChar.getActiveRequester() != null || activeChar.isOutOfControl())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2Object obj = (activeChar.getTargetId() == _objectId) ? activeChar.getTarget() : L2World.getInstance().getObject(_objectId);
		
		if(Config.ZONEPVPEVENT_ALLOW_INTERFERENCE)
		{
			if (obj instanceof Player && !activeChar.isInsideZone(ZoneId.PVP_CUSTOM) && ((Player) obj).isInsideZone(ZoneId.PVP_CUSTOM) || obj instanceof Player && activeChar.isInsideZone(ZoneId.PVP_CUSTOM) && !((Player) obj).isInsideZone(ZoneId.PVP_CUSTOM))
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
				
			}
		}
		if (Config.SELL_BUFF_ENABLED)
		{
		    // Verifica se o novo alvo é um jogador (L2PcInstance) que está vendendo buffs
		    if (obj instanceof Player && ((Player) obj).isSellBuff())
		    {
		        Player seller = (Player) obj;

		        // Aqui, forçamos a atualização da página sempre, mesmo que o alvo seja o mesmo
		        // Forçamos a página de buffs a voltar para a primeira página, independente se o alvo mudou ou não
		        activeChar.getSellBuffMsg().setLastPage(0); // Volta para a primeira página ao clicar no NPC vendedor de buffs

		        // Envia a resposta do comprador (abre o HTM de buffs)
		        activeChar.getSellBuffMsg().sendBuyerResponse(seller, activeChar, false);
		    }
		}

		if (obj == null)
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (!obj.isTargetable())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		switch (_actionId)
		{
			case 0:
				obj.onAction(activeChar);
				break;
			
			case 1:
				obj.onActionShift(activeChar);
				break;
			
			default:
				// Invalid action detected (probably client cheating), log this
				_log.warning(activeChar.getName() + " requested invalid action: " + _actionId);
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				break;
		}
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}