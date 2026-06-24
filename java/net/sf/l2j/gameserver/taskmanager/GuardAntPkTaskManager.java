package net.sf.l2j.gameserver.taskmanager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.L2GuardInstance;

public final class GuardAntPkTaskManager implements Runnable
{
	private final Map<L2Npc, Boolean> _guards = new ConcurrentHashMap<>();
	
	public static GuardAntPkTaskManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private GuardAntPkTaskManager()
	{
		ThreadPool.scheduleAtFixedRate(this, 1000, 1000);
	}
	
	public void add(L2Npc npc)
	{
		_guards.put(npc, Boolean.TRUE);
	}
	
	public void remove(L2Npc npc)
	{
		_guards.remove(npc);
	}
	
	@Override
	public void run()
	{
		if (_guards.isEmpty())
			return;
		
		for (L2Npc npc : _guards.keySet())
		{
			if (npc == null || npc.isDead() || !npc.isInActiveRegion())
			{
				_guards.remove(npc);
				continue;
			}
			
			// Segurança extra
			if (!(npc instanceof L2GuardInstance))
				continue;
			
			checkForKarmaPlayer((L2GuardInstance) npc);
		}
	}
	
	public static void checkForKarmaPlayer(L2GuardInstance guard)
	{
		// Já está atacando alguém
		if (guard.getAI().getIntention() == CtrlIntention.ATTACK)
			return;
		
		for (L2Object obj : guard.getKnownList().getKnownObjects())
		{
			if (!(obj instanceof Player))
				continue;
			Player player = (Player) obj;
			
			if (player.isDead())
				continue;
			
			if (player.getKarma() <= 0)
				continue;
			
			if (!guard.isInsideRadius(player, 800, true, false))
				continue;
			
			// Ativa agressão imediata
			guard.setTarget(player);
			guard.getAI().setIntention(CtrlIntention.ATTACK, player);
			return;
		}
	}
	
	private static final class SingletonHolder
	{
		private static final GuardAntPkTaskManager _instance = new GuardAntPkTaskManager();
	}
}
