package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.util.Broadcast;

/**
 * @author Christian
 *
 */
public class AdminAntZerg implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_antzerg",
	};
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.startsWith("admin_antzerg"))
		{
			try
			{
				String targetName = command.substring(14);
				Player player = L2World.getInstance().getPlayer(targetName);
				if (player == null)
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
					return false;
				}
				
				final L2Party party = player.getParty();
				if (party != null)
				{
					
					for (Player member : party.getPartyMembers())
						member.teleToLocation(MapRegionTable.TeleportWhereType.TOWN);
					if (player.getParty() != null)
					{
						if (player.getClan() != null)
						Broadcast.announceToOnlinePlayers("[Party]->"+"do " + player.getName() + " do Clan: " + player.getClan().getName() + " esta Zergando " + " e foi teleportado"+ " para a Cidade" + " pelo Administrador");
						else
						Broadcast.announceToOnlinePlayers("[Party]->"+"do " + player.getName() + " esta Zergando " + " e foi teleportado"+ " para a Cidade" + " pelo Administrador");	
					}	
				}
				else
				{
					if (player.getClan() != null)
						Broadcast.announceToOnlinePlayers("[Solo]->" + player.getName() + " do Clan: " + player.getClan().getName()  + " esta Zergando " + " e foi teleportado"+ " para a Cidade" + " pelo Administrador");
					else
						Broadcast.announceToOnlinePlayers("[solo]->"+"do " + player.getName() + " esta Zergando " + " e foi teleportado"+ " para a Cidade" + " pelo Administrador");
					player.teleToLocation(MapRegionTable.TeleportWhereType.TOWN);
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
			}
		}
		return false;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
}
