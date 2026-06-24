package net.sf.l2j.gameserver.balance.classbalance;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;

public class ClassProfileHolder
{
	private static final Map<String, ClassProfile> PROFILES = new HashMap<>();
	
	public static void add(ClassProfile profile)
	{
		PROFILES.put(profile.getClassId(), profile);
	}
	
	public static ClassProfile get(String classId)
	{
		return PROFILES.get(classId);
	}
	
	public static void clear()
	{
		PROFILES.clear();
	}
	
	public static ClassProfile getProfile(Creature cha)
	{
	    if (!(cha instanceof Player))
	        return null;

	    Player pc = (Player) cha;
	    return get(String.valueOf(pc.getClassId().getId()));
	}

}
