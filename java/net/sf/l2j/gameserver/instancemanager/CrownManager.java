package net.sf.l2j.gameserver.instancemanager;

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.CrownTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

public class CrownManager
{
	
	protected static final Logger LOG = Logger.getLogger(CrownManager.class.getName());
	
	public CrownManager()
	{
		LOG.info("Load: Castle Crown Manager");
		
	}
	
	public void checkCrowns(final L2Clan clan)
	{
		if (clan == null)
		{
			return;
		}
		
		for (final L2ClanMember member : clan.getMembers())
		{
			if ((member != null) && member.isOnline() && (member.getPlayerInstance() != null))
			{
				checkCrowns(member.getPlayerInstance());
			}
		}
	}
	
	public void checkCrowns(final Player activeChar)
	{
		if (activeChar == null)
		{
			return;
		}
		
		boolean isLeader = false;
		int crownId = -1;
		
		L2Clan activeCharClan = activeChar.getClan();
		L2ClanMember activeCharClanLeader;
		
		if (activeCharClan != null)
		{
			activeCharClanLeader = activeChar.getClan().getLeader();
		}
		else
		{
			activeCharClanLeader = null;
		}
		
		if (activeCharClan != null)
		{
			Castle activeCharCastle = CastleManager.getInstance().getCastleByOwner(activeCharClan);
			
			if (activeCharCastle != null)
			{
				crownId = CrownTable.getCrownId(activeCharCastle.getCastleId());
			}
			
			activeCharCastle = null;
			
			if ((activeCharClanLeader != null) && (activeCharClanLeader.getObjectId() == activeChar.getObjectId()))
			{
				isLeader = true;
			}
		}
		
		activeCharClan = null;
		activeCharClanLeader = null;
		
		if (crownId > 0)
		{
			if (isLeader && (activeChar.getInventory().getItemByItemId(6841) == null))
			{
				activeChar.addItem("Crown", 6841, 1, activeChar, true);
				activeChar.getInventory().updateDatabase();
			}
			
			if (activeChar.getInventory().getItemByItemId(crownId) == null)
			{
				activeChar.addItem("Crown", crownId, 1, activeChar, true);
				activeChar.getInventory().updateDatabase();
			}
		}
		
		boolean alreadyFoundCirclet = false;
		boolean alreadyFoundCrown = false;
		
		for (final ItemInstance item : activeChar.getInventory().getItems())
		{
			if (CrownTable.getCrownList().contains(item.getItemId()))
			{
				if (crownId > 0)
				{
					if (item.getItemId() == crownId)
					{
						if (!alreadyFoundCirclet)
						{
							alreadyFoundCirclet = true;
							continue;
						}
					}
					else if ((item.getItemId() == 6841) && isLeader)
					{
						if (!alreadyFoundCrown)
						{
							alreadyFoundCrown = true;
							continue;
						}
					}
				}
				
				if (Config.REMOVE_CASTLE_CIRCLETS)
				{
					activeChar.destroyItem("Removing Crown", item, activeChar, true);
				}
				activeChar.getInventory().updateDatabase();
			}
		}
	}
	
	public static CrownManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final CrownManager INSTANCE = new CrownManager();
	}
}
