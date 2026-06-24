package net.sf.l2j.mods.manager;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import net.sf.l2j.mods.actor.FakePlayer;

public final class FakePlayerManager
{
	private final List<FakePlayer> _fakePlayers = new CopyOnWriteArrayList<>();
	private final Map<Integer, FakePlayer> _byObjectId = new ConcurrentHashMap<>();
	
	public void register(FakePlayer player)
	{
		registerIfAbsent(player);
	}
	
	public void unregister(FakePlayer player)
	{
		if (player == null)
			return;
		if (_byObjectId.remove(player.getObjectId(), player))
			_fakePlayers.remove(player);
	}
	
	public boolean registerIfAbsent(FakePlayer player)
	{
		if (player == null)
			return false;
		if (_byObjectId.putIfAbsent(player.getObjectId(), player) != null)
			return false;
		_fakePlayers.add(player);
		return true;
	}
	
	public FakePlayer getPlayer(int objectId)
	{
		return _byObjectId.get(objectId);
	}
	
	public boolean isOnline(int objectId)
	{
		FakePlayer player = _byObjectId.get(objectId);
		return player != null && player.isOnline() && !player.isDestroyed();
	}
	
	public List<FakePlayer> getFakePlayers()
	{
		return List.copyOf(_fakePlayers);
	}
	
	public Collection<FakePlayer> values()
	{
		return _byObjectId.values();
	}
	
	public int getFakePlayersCount()
	{
		return _fakePlayers.size();
	}
	
	public void clear()
	{
		_byObjectId.clear();
		_fakePlayers.clear();
	}
	public static FakePlayerManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		private static final FakePlayerManager INSTANCE = new FakePlayerManager();
	}
}
