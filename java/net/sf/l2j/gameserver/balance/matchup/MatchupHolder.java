package net.sf.l2j.gameserver.balance.matchup;

import java.util.HashMap;
import java.util.Map;

public class MatchupHolder
{
	private static final Map<String, MatchupProfile> MATCHUPS = new HashMap<>();

	public static void add(MatchupProfile profile)
	{
		MATCHUPS.put(profile.getKey(), profile);
	}

	public static MatchupProfile get(int attackerClassId, int targetClassId)
	{
		return MATCHUPS.get(attackerClassId + ":" + targetClassId);
	}

	public static void clear()
	{
		MATCHUPS.clear();
	}
}
