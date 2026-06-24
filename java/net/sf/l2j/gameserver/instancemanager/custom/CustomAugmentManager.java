package net.sf.l2j.gameserver.instancemanager.custom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2j.gameserver.ConnectionPool;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.holder.AugmentStoneHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

public class CustomAugmentManager
{
	private final Map<Integer, int[]> augments = new ConcurrentHashMap<>();
	
	public static CustomAugmentManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final CustomAugmentManager INSTANCE = new CustomAugmentManager();
	}
	
	// =========================
	// APPLY
	// =========================
	public void applyAugment(Player player, AugmentStoneHolder holder)
	{
		ItemInstance weapon = player.getActiveWeaponInstance();
		
		if (weapon == null || holder == null)
			return;
		
		int objId = weapon.getObjectId();
		
		removeWeaponAugment(player, weapon);
		
		augments.put(objId, new int[]
		{
			holder.getSkillId(),
			holder.getSkillLevel()
		});
		
		saveAugment(objId, holder.getSkillId(), holder.getSkillLevel());
		
		player.disarmWeapons();
		
		// =========================
		// 5. limpa estado
		// =========================
		player.clearTempAugmentItem();
		player.clearTempAugmentData();
		
	}
	
	// =========================
	// REMOVE
	// =========================
	public void removeWeaponAugment(Player player, ItemInstance weapon)
	{
		
		int[] data = augments.get(weapon.getObjectId());
		
		if (data != null)
		{
			int skillId = data[0];

			
			if (skillId > 0)
			{
				 
				player.removeSkill(player.getSkill(skillId), false);
				
				player.sendSkillList();
				
			}
		}
		
	}
	
	public void deleteWeaponAugment(Player player, ItemInstance weapon)
	{
		
		int[] data = augments.get(weapon.getObjectId());
		
		if (data != null)
		{
			int skillId = data[0];
			
			if (skillId > 0)
			{
				
				player.removeSkill(player.getSkill(skillId), false);
				
				player.sendSkillList();
				deleteAugment(weapon.getObjectId());
			}
		}
		
	}
	
	// =========================
	// EQUIP
	// =========================
	public void onWeaponEquip(Player player, ItemInstance weapon)
	{
		int[] data = augments.get(weapon.getObjectId());
		
		if (data != null)
		{
			int skillId = data[0];
			int level = data[1];
			
			if (skillId > 0)
			{
				L2Skill skill = SkillTable.getInstance().getInfo(skillId, level);
				player.addSkill(skill, false);
				player.sendSkillList();
 
			}
		}
	}
	
	// =========================
	// SAVE
	// =========================
	private static void saveAugment(int objectId, int skillId, int level)
	{
		try (Connection con = ConnectionPool.getConnection(); PreparedStatement ps = con.prepareStatement("REPLACE INTO weapon_augments (weapon_obj_id, skill_id, skill_level) VALUES (?, ?, ?)"))
		{
			ps.setInt(1, objectId);
			ps.setInt(2, skillId);
			ps.setInt(3, level);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	// =========================
	// DELETE
	// =========================
	private static void deleteAugment(int objectId)
	{
		try (Connection con = ConnectionPool.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM weapon_augments WHERE weapon_obj_id = ?"))
		{
			ps.setInt(1, objectId);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	// =========================
	// LOAD (STARTUP)
	// =========================
	public void load()
	{
		try (Connection con = ConnectionPool.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT weapon_obj_id, skill_id, skill_level FROM weapon_augments"); ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				int objId = rs.getInt("weapon_obj_id");
				int skillId = rs.getInt("skill_id");
				int level = rs.getInt("skill_level");
				
				augments.put(objId, new int[]
				{
					skillId,
					level
				});
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		System.out.println("CustomAugmentManager: Loaded " + augments.size() + " augments.");
	}
	
	public void reload()
	{
		augments.clear();
		load();
	}
	
	public int[] getAugment(int objectId)
	{
	    return augments.get(objectId);
	}
	
	public boolean hasAugment(int objectId)
	{
	    return augments.containsKey(objectId);
	}
}