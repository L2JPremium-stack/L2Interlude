package net.sf.l2j.solofarm.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.ConnectionPool;
import net.sf.l2j.solofarm.holder.SoloFarmSpawn;

public class SoloFarmSpawnTable
{
	private static final String LOAD_SPAWNS = "SELECT npc_id, loc_x, loc_y, loc_z, heading, respawn_delay, is_boss FROM solo_farm_spawnlist";
	
	private final List<SoloFarmSpawn> _spawns = new ArrayList<>();
	
	public void load()
	{
		_spawns.clear();
		
		try (Connection con = ConnectionPool.getConnection(); PreparedStatement ps = con.prepareStatement(LOAD_SPAWNS); ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				_spawns.add(new SoloFarmSpawn(rs.getInt("npc_id"), rs.getInt("loc_x"), rs.getInt("loc_y"), rs.getInt("loc_z"), rs.getInt("heading"), rs.getInt("respawn_delay"), rs.getBoolean("is_boss")));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public List<SoloFarmSpawn> getSpawns()
	{
		return _spawns;
	}
	
	public SoloFarmSpawn getRandomSpawn()
	{
		if (_spawns.isEmpty())
			return null;
		
		return _spawns.get(Rnd.get(_spawns.size()));
	}
	
	public static SoloFarmSpawnTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		private static final SoloFarmSpawnTable INSTANCE = new SoloFarmSpawnTable();
	}
}
