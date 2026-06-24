package net.sf.l2j.gameserver.balance;

import net.sf.l2j.gameserver.balance.classbalance.ClassProfileHolder;
import net.sf.l2j.gameserver.balance.classbalance.ClassProfileReader;
import net.sf.l2j.gameserver.balance.matchup.MatchupHolder;
import net.sf.l2j.gameserver.balance.matchup.MatchupReader;
import net.sf.l2j.gameserver.balance.skills.SkillBalanceHolder;
import net.sf.l2j.gameserver.balance.skills.SkillBalanceReader;

public final class BalanceManager
{
	public static void load()
	{
		ClassProfileHolder.clear();
		MatchupHolder.clear();
		SkillBalanceHolder.clear();
		
		new ClassProfileReader().load();
		new MatchupReader().load();
		new SkillBalanceReader().load();
	}
	
	
}
