package net.sf.l2j.gameserver.instancemanager.custom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2j.gameserver.ConnectionPool;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

public final class AdminItemLockManager
{
	private static final long CACHE_TIME = 3000L;

	private final Map<Integer, LockCache> _cache = new ConcurrentHashMap<>();

	protected AdminItemLockManager()
	{
	}

	public static AdminItemLockManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	public boolean isLocked(ItemInstance item)
	{
		if (item == null)
			return false;

		return isLocked(item.getObjectId());
	}

	public boolean isLocked(int objectId)
	{
		if (objectId <= 0)
			return false;

		final long now = System.currentTimeMillis();
		final LockCache cached = _cache.get(objectId);

		if (cached != null && cached.expireAt > now)
			return cached.locked;

		boolean locked = false;

		try (Connection con = ConnectionPool.getConnection();
			PreparedStatement ps = con.prepareStatement(
				"SELECT active FROM site_admin_item_locks WHERE object_id=? AND active=1 LIMIT 1"))
		{
			ps.setInt(1, objectId);

			try (ResultSet rs = ps.executeQuery())
			{
				locked = rs.next();
			}
		}
		catch (Exception e)
		{
			// Em erro de DB, por segurança bloqueia o item.
			locked = true;
		}

		_cache.put(objectId, new LockCache(locked, now + CACHE_TIME));
		return locked;
	}

	public boolean check(Player player, ItemInstance item)
	{
		if (item == null)
			return false;

		if (!isLocked(item))
			return true;

		if (player != null)
			player.sendMessage("Este item esta travado pela administracao e nao pode ser movido.");

		return false;
	}

	public void clearCache(int objectId)
	{
		_cache.remove(objectId);
	}

	private static final class LockCache
	{
		private final boolean locked;
		private final long expireAt;

		private LockCache(boolean locked, long expireAt)
		{
			this.locked = locked;
			this.expireAt = expireAt;
		}
	}

	private static final class SingletonHolder
	{
		protected static final AdminItemLockManager INSTANCE = new AdminItemLockManager();
	}
}