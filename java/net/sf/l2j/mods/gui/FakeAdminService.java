package net.sf.l2j.mods.gui;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.ConnectionPool;
import net.sf.l2j.gameserver.datatables.CharNameTable;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.mods.actor.FakePlayer;
import net.sf.l2j.mods.engine.FakePlayerRestoreEngine;
import net.sf.l2j.mods.enums.FakeTeleportPoint;
import net.sf.l2j.mods.factory.FakePlayerFactory;
import net.sf.l2j.mods.manager.FakePlayerManager;

 
public final class FakeAdminService
{
	private static final Logger LOGGER = Logger.getLogger(FakeAdminService.class.getName());
	
	private static final String SELECT_FAKE_CHARACTERS = "SELECT c.obj_id, c.char_name, c.classid, c.level, c.x, c.y, c.z FROM characters c INNER JOIN accounts a ON a.login = c.account_name WHERE a.access_level = -1 AND a.login LIKE 'AutoPilot_%'";
	
	private FakeAdminService()
	{
	}
	
	public static class FakeRow
	{
		public final int objectId;
		public final String name;
		public final ClassId classId;
		public final int level;
		public final int x, y, z;
		public final boolean online;
		public final String state;
		
		public FakeRow(int objectId, String name, ClassId classId, int level, int x, int y, int z, boolean online, String state)
		{
			this.objectId = objectId;
			this.name = name;
			this.classId = classId;
			this.level = level;
			this.x = x;
			this.y = y;
			this.z = z;
			this.online = online;
			this.state = state;
		}
	}
	
	public static List<FakeRow> loadAllRows()
	{
		Map<Integer, FakeRow> rows = new LinkedHashMap<>();
		
		try (Connection con = ConnectionPool.getConnection(); PreparedStatement ps = con.prepareStatement(SELECT_FAKE_CHARACTERS); ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				int objectId = rs.getInt("obj_id");
				String name = rs.getString("char_name");
				int classid = rs.getInt("classid");
				int level = rs.getInt("level");
				int x = rs.getInt("x");
				int y = rs.getInt("y");
				int z = rs.getInt("z");
				ClassId cid = (classid >= 0 && classid < ClassId.VALUES.length) ? ClassId.VALUES[classid] : null;
				FakePlayer fp = FakePlayerManager.getInstance().getPlayer(objectId);
				rows.put(objectId, buildRow(objectId, name, cid, level, x, y, z, fp));
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("Failed loadAllRows: " + getErrorMessage(e));
		}
		
		// Edge case: fake online que não está no DB query (raro)
		for (FakePlayer fp : FakePlayerManager.getInstance().getFakePlayers())
		{
			addRuntimeOnlyRow(rows, fp);
		}
		
		return new ArrayList<>(rows.values());
	}
	
	private static FakeRow buildRow(int objectId, String name, ClassId classId, int level, int x, int y, int z, FakePlayer fp)
	{
		boolean online = false;
		String state = "OFFLINE";
		
		if (isRuntimeOnline(fp))
		{
			try
			{
				x = fp.getX();
				y = fp.getY();
				z = fp.getZ();
				state = getRuntimeState(fp);
				online = true;
			}
			catch (Exception e)
			{
				LOGGER.fine("Skipped runtime state for fake " + objectId + ": " + getErrorMessage(e));
			}
		}
		
		return new FakeRow(objectId, name, classId, level, x, y, z, online, state);
	}
	
	private static void addRuntimeOnlyRow(Map<Integer, FakeRow> rows, FakePlayer fp)
	{
		if (!isRuntimeOnline(fp))
			return;
		
		try
		{
			int objectId = fp.getObjectId();
			if (rows.containsKey(objectId))
				return;
			
			rows.put(objectId, new FakeRow(objectId, fp.getName(), fp.getClassId(), fp.getStat().getLevel(), fp.getX(), fp.getY(), fp.getZ(), true, getRuntimeState(fp)));
		}
		catch (Exception e)
		{
			LOGGER.fine("Skipped runtime fake row: " + getErrorMessage(e));
		}
	}
	
	private static boolean isRuntimeOnline(FakePlayer fp)
	{
		try
		{
			return fp != null && !fp.isDestroyed() && fp.isOnline();
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	private static String getRuntimeState(FakePlayer fp)
	{
		String state = fp.getCurrentAction();
		return (state != null && !state.isBlank()) ? state : "ONLINE";
	}
	
	// ========================= CREATE (new) =========================
	public static List<Integer> createAtPoint(FakeTeleportPoint point, ClassId classId, int count, int radius)
	{
		if (point == null)
			throw new IllegalArgumentException("Select a Point.");
		if (classId == null)
			throw new IllegalArgumentException("Select a ClassId.");
		if (count <= 0)
			return List.of();
		
		if (!FakePlayer.getAllAIs().containsKey(classId))
			throw new IllegalStateException("No FakePlayer AI registered for: " + classId);
		
		Location base = point.getLocation();
		List<Integer> createdIds = new ArrayList<>(count);
		
		for (int i = 0; i < count; i++)
		{
			int x = base.getX();
			int y = base.getY();
			int z = base.getZ();
			
			if (radius > 0)
			{
				x += Rnd.get(-radius, radius);
				y += Rnd.get(-radius, radius);
			}
			
			FakePlayer fp = FakePlayerFactory.create(classId.getId(), x, y, z);
			createdIds.add(fp.getObjectId());
		}
		
		return createdIds;
	}
	
	// ========================= SPAWN Selected (Pinned) =========================
	public static int spawnPinnedOfflineToPoint(Set<Integer> pinnedIds, FakeTeleportPoint point, int radius)
	{
		if (pinnedIds == null || pinnedIds.isEmpty())
			throw new IllegalStateException("No pinned targets.");
		if (point == null)
			throw new IllegalArgumentException("Select a Point.");
		
		Location base = point.getLocation();
		int spawned = 0;
		
		for (int objId : pinnedIds)
		{
			// já online? não faz nada
			FakePlayer online = FakePlayerManager.getInstance().getPlayer(objId);
			if (isRuntimeOnline(online))
				continue;
			
			FakePlayer fp = restoreSingle(objId);
			if (fp == null)
				continue;
			
			int x = base.getX();
			int y = base.getY();
			int z = base.getZ();
			
			if (radius > 0)
			{
				x += Rnd.get(-radius, radius);
				y += Rnd.get(-radius, radius);
			}
			
			fp.teleToLocation(x, y, z, 0);
			spawned++;
		}
		
		return spawned;
	}
	
	private static FakePlayer restoreSingle(int objectId)
	{
		FakePlayer current = FakePlayerManager.getInstance().getPlayer(objectId);
		if (current != null && !isRuntimeOnline(current))
			removeStaleRuntime(current, objectId);
		
		return FakePlayerRestoreEngine.getInstance().restoreSingle(objectId);
	}
	
	private static void removeStaleRuntime(FakePlayer fp, int objectId)
	{
		if (fp == null)
			return;
		
		try
		{
			if (fp.isDestroyed())
				FakePlayerManager.getInstance().unregister(fp);
			else
				fp.deleteMe();
		}
		catch (Exception e)
		{
			FakePlayerManager.getInstance().unregister(fp);
			LOGGER.fine("Removed stale fake runtime objId=" + objectId + ": " + getErrorMessage(e));
		}
	}
	
	// ========================= DESPAWN Selected (Pinned) =========================
	public static int despawnPinnedOnline(Set<Integer> pinnedIds)
	{
		if (pinnedIds == null || pinnedIds.isEmpty())
			throw new IllegalStateException("No pinned targets.");
		
		int despawned = 0;
		
		for (int objId : pinnedIds)
		{
			FakePlayer fp = FakePlayerManager.getInstance().getPlayer(objId);
			if (despawnFake(fp, objId))
				despawned++;
		}
		
		return despawned;
	}
	
	private static boolean despawnFake(FakePlayer fp, int objectId)
	{
		if (fp == null)
			return false;
		if (fp.isDestroyed())
		{
			FakePlayerManager.getInstance().unregister(fp);
			return false;
		}
		
		try
		{
			fp.store();
		}
		catch (Exception e)
		{
			LOGGER.warning("Failed storing fake before despawn objId=" + objectId + ": " + getErrorMessage(e));
		}
		
		try
		{
			fp.abortAttack();
		}
		catch (Exception e)
		{
			LOGGER.fine("Failed abortAttack before despawn objId=" + objectId + ": " + getErrorMessage(e));
		}
		
		try
		{
			fp.abortCast();
		}
		catch (Exception e)
		{
			LOGGER.fine("Failed abortCast before despawn objId=" + objectId + ": " + getErrorMessage(e));
		}
		
		try
		{
			fp.deleteMe();
			return true;
		}
		catch (Exception e)
		{
			LOGGER.warning("Failed despawn fake objId=" + objectId + ": " + getErrorMessage(e));
			return false;
		}
	}
	
	// ========================= DELETE DB Selected (Pinned) =========================
	public static int deletePinnedDb(Set<Integer> pinnedIds)
	{
		if (pinnedIds == null || pinnedIds.isEmpty())
			throw new IllegalStateException("No pinned targets.");
		
		int deleted = 0;
		
		for (int objId : pinnedIds)
		{
			FakePlayer fp = FakePlayerManager.getInstance().getPlayer(objId);
			if (fp != null)
				despawnFake(fp, objId);
			
			if (deleteCharAndMaybeAccount(objId))
				deleted++;
			else
				LOGGER.warning("Delete DB failed objId=" + objId);
		}
		
		return deleted;
	}
	
	public static boolean deleteCharAndMaybeAccount(int objectId)
	{
		if (objectId <= 0)
			return false;
		
		CharNameTable.getInstance().unregister(objectId);
		
		try (Connection con = ConnectionPool.getConnection())
		{
			con.setAutoCommit(false);
			
			// 1) account
			String accountName = getAccountNameByObjId(con, objectId);
			if (accountName == null || accountName.isEmpty())
			{
				con.rollback();
				return false;
			}
			
			// 2) delete char data (SQL do teu core)
			deleteCharacterDataUsingCoreSql(con, objectId);
			
			// 3) se não sobrou char na conta, remove accounts
			int remaining = countCharsByAccount(con, accountName);
			if (remaining == 0)
			{
				deleteAccount(con, accountName);
			}
			
			con.commit();
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	private static void deleteCharacterDataUsingCoreSql(Connection con, int objectId) throws SQLException
	{
		try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_friends WHERE char_id=? OR friend_id=?"))
		{
			ps.setInt(1, objectId);
			ps.setInt(2, objectId);
			ps.execute();
		}
		
		try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=?"))
		{
			ps.setInt(1, objectId);
			ps.execute();
		}
		
		try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_macroses WHERE char_obj_id=?"))
		{
			ps.setInt(1, objectId);
			ps.execute();
		}
		
		try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_memo WHERE charId=?"))
		{
			ps.setInt(1, objectId);
			ps.execute();
		}
		
		try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_memo_alt WHERE obj_id=?"))
		{
			ps.setInt(1, objectId);
			ps.execute();
		}
		
		try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_quests WHERE charId=?"))
		{
			ps.setInt(1, objectId);
			ps.execute();
		}
		
		try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_recipebook WHERE char_id=?"))
		{
			ps.setInt(1, objectId);
			ps.execute();
		}
		
		try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=?"))
		{
			ps.setInt(1, objectId);
			ps.execute();
		}
		
		try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=?"))
		{
			ps.setInt(1, objectId);
			ps.execute();
		}
		
		try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=?"))
		{
			ps.setInt(1, objectId);
			ps.execute();
		}
		
		try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_subclasses WHERE char_obj_id=?"))
		{
			ps.setInt(1, objectId);
			ps.execute();
		}
		
		try (PreparedStatement ps = con.prepareStatement("DELETE FROM heroes WHERE char_id=?"))
		{
			ps.setInt(1, objectId);
			ps.execute();
		}
		
		try (PreparedStatement ps = con.prepareStatement("DELETE FROM olympiad_nobles WHERE char_id=?"))
		{
			ps.setInt(1, objectId);
			ps.execute();
		}
		
		try (PreparedStatement ps = con.prepareStatement("DELETE FROM seven_signs WHERE char_obj_id=?"))
		{
			ps.setInt(1, objectId);
			ps.execute();
		}
		
		try (PreparedStatement ps = con.prepareStatement("DELETE FROM pets WHERE item_obj_id IN (SELECT object_id FROM items WHERE items.owner_id=?)"))
		{
			ps.setInt(1, objectId);
			ps.execute();
		}
		
		try (PreparedStatement ps = con.prepareStatement("DELETE FROM augmentations WHERE item_id IN (SELECT object_id FROM items WHERE items.owner_id=?)"))
		{
			ps.setInt(1, objectId);
			ps.execute();
		}
		
		try (PreparedStatement ps = con.prepareStatement("DELETE FROM items WHERE owner_id=?"))
		{
			ps.setInt(1, objectId);
			ps.execute();
		}
		
		try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_raid_points WHERE char_id=?"))
		{
			ps.setInt(1, objectId);
			ps.execute();
		}
		
		// FINALMENTE: remove o char da tabela principal
		try (PreparedStatement ps = con.prepareStatement("DELETE FROM characters WHERE obj_id=?"))
		{
			ps.setInt(1, objectId);
			ps.execute();
		}
	}
	
	private static int deleteAccount(Connection con, String accountName) throws SQLException
	{
		try (PreparedStatement ps = con.prepareStatement("DELETE FROM accounts WHERE login=?"))
		{
			ps.setString(1, accountName);
			return ps.executeUpdate();
		}
	}
	
	private static int countCharsByAccount(Connection con, String accountName) throws SQLException
	{
		try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM characters WHERE account_name=?"))
		{
			ps.setString(1, accountName);
			try (ResultSet rs = ps.executeQuery())
			{
				rs.next();
				return rs.getInt(1);
			}
		}
	}
	
	private static String getAccountNameByObjId(Connection con, int objectId) throws SQLException
	{
		try (PreparedStatement ps = con.prepareStatement("SELECT account_name FROM characters WHERE obj_id=?"))
		{
			ps.setInt(1, objectId);
			try (ResultSet rs = ps.executeQuery())
			{
				return rs.next() ? rs.getString(1) : null;
			}
		}
	}
	
	public static List<FakeRow> filter(List<FakeRow> src, String text)
	{
		if (src == null || src.isEmpty())
			return List.of();
		if (text == null || text.isBlank())
		{
			List<FakeRow> out = new ArrayList<>(src.size());
			for (FakeRow r : src)
			{
				if (r != null)
					out.add(r);
			}
			return out;
		}
		
		String q = text.trim().toLowerCase();
		List<FakeRow> out = new ArrayList<>();
		
		for (FakeRow r : src)
		{
			if (r == null)
				continue;
			
			String cls = (r.classId != null) ? r.classId.name().toLowerCase() : "";
			String name = (r.name != null) ? r.name.toLowerCase() : "";
			
			if (name.contains(q) || cls.contains(q))
				out.add(r);
		}
		return out;
	}
	
	private static String getErrorMessage(Exception e)
	{
		if (e == null)
			return "unknown";
		
		String message = e.getMessage();
		return (message != null && !message.isBlank()) ? message : e.getClass().getSimpleName();
	}
}
