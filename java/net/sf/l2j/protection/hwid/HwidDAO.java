package net.sf.l2j.protection.hwid;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ConnectionPool;
import net.sf.l2j.gameserver.model.actor.Player;

public class HwidDAO
{
	private static final Logger LOGGER = Logger.getLogger(HwidDAO.class.getName());
	
	public static final class BanSnapshot
	{
		private final Set<Integer> _deviceIds;
		private final Set<String> _accounts;
		
		public BanSnapshot(Set<Integer> deviceIds, Set<String> accounts)
		{
			_deviceIds = deviceIds;
			_accounts = accounts;
		}
		
		public boolean containsDevice(int deviceId)
		{
			return _deviceIds.contains(deviceId);
		}
		
		public boolean containsAccount(String account)
		{
			return account != null && _accounts.contains(account.toLowerCase());
		}
	}
	
	public int getOrCreateDevice(String cpu, String hdd, String mac)
	{
		try (Connection con = ConnectionPool.getConnection())
		{
			PreparedStatement ps = con.prepareStatement("SELECT id FROM hwid_devices WHERE cpu=? AND hdd=? AND mac=?");
			
			ps.setString(1, cpu);
			ps.setString(2, hdd);
			ps.setString(3, mac);
			
			ResultSet rs = ps.executeQuery();
			if (rs.next())
				return rs.getInt("id");
			
			ps = con.prepareStatement("INSERT INTO hwid_devices (cpu, hdd, mac, first_seen, last_seen) VALUES (?, ?, ?, NOW(), NOW())", PreparedStatement.RETURN_GENERATED_KEYS);
			
			ps.setString(1, cpu);
			ps.setString(2, hdd);
			ps.setString(3, mac);
			
			ps.executeUpdate();
			
			rs = ps.getGeneratedKeys();
			if (rs.next())
				return rs.getInt(1);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return -1;
	}
	
	public boolean isBanned(int deviceId)
	{
		try (Connection con = ConnectionPool.getConnection())
		{
			PreparedStatement ps = con.prepareStatement("SELECT banned FROM hwid_devices WHERE id=?");
			ps.setInt(1, deviceId);
			
			ResultSet rs = ps.executeQuery();
			if (rs.next())
				return rs.getBoolean("banned");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean isHardwareBanned(String cpu, String hdd, String mac)
	{
		final List<String> conditions = new ArrayList<>();
		final List<String> values = new ArrayList<>();
		
		addHardwarePair(conditions, values, "cpu", cpu, "hdd", hdd);
		addHardwarePair(conditions, values, "cpu", cpu, "mac", mac);
		addHardwarePair(conditions, values, "hdd", hdd, "mac", mac);
		
		if (conditions.isEmpty())
			return false;
		
		final String sql = "SELECT 1 FROM hwid_devices WHERE banned=1 AND (" + String.join(" OR ", conditions) + ") LIMIT 1";
		try (Connection con = ConnectionPool.getConnection();
			PreparedStatement ps = con.prepareStatement(sql))
		{
			for (int i = 0; i < values.size(); i++)
				ps.setString(i + 1, values.get(i));
			
			try (ResultSet rs = ps.executeQuery())
			{
				return rs.next();
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Could not check permanent HWID ban.", e);
			return false;
		}
	}
	
	public boolean isAccountBanned(String account)
	{
		if (account == null || account.isBlank())
			return false;
		
		try (Connection con = ConnectionPool.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT 1 FROM hwid_account_bans WHERE account_name=? AND active=1 LIMIT 1"))
		{
			ps.setString(1, account.toLowerCase());
			try (ResultSet rs = ps.executeQuery())
			{
				return rs.next();
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Could not check permanent account ban for " + account + ".", e);
			return false;
		}
	}
	
	public BanSnapshot loadBanSnapshot()
	{
		final Set<Integer> deviceIds = new HashSet<>();
		final Set<String> accounts = new HashSet<>();
		
		try (Connection con = ConnectionPool.getConnection())
		{
			try (PreparedStatement ps = con.prepareStatement("SELECT id FROM hwid_devices WHERE banned=1");
				ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
					deviceIds.add(rs.getInt("id"));
			}
			
			try (PreparedStatement ps = con.prepareStatement("SELECT account_name FROM hwid_account_bans WHERE active=1");
				ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
					accounts.add(rs.getString("account_name").toLowerCase());
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Could not load HWID ban snapshot.", e);
		}
		
		return new BanSnapshot(deviceIds, accounts);
	}
	
	public void deactivateSessions(String account, int deviceId)
	{
		try (Connection con = ConnectionPool.getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE hwid_sessions SET active=0 WHERE active=1 AND (account_name=? OR device_id=?)"))
		{
			ps.setString(1, account);
			ps.setInt(2, deviceId);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Could not deactivate banned HWID session.", e);
		}
	}
	
	public void updateLastSeen(int deviceId)
	{
		try (Connection con = ConnectionPool.getConnection())
		{
			PreparedStatement ps = con.prepareStatement("UPDATE hwid_devices SET last_seen=NOW() WHERE id=?");
			ps.setInt(1, deviceId);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void linkAccount(String account, int deviceId)
	{
		try (Connection con = ConnectionPool.getConnection())
		{
			PreparedStatement ps = con.prepareStatement("INSERT INTO hwid_accounts (account_name, device_id, first_seen, last_seen) " + "VALUES (?, ?, NOW(), NOW()) " + "ON DUPLICATE KEY UPDATE last_seen=NOW()");
			
			ps.setString(1, account);
			ps.setInt(2, deviceId);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void createSession(String account, int deviceId, String ip, String token)
	{
		try (Connection con = ConnectionPool.getConnection())
		{
			PreparedStatement ps = con.prepareStatement("INSERT INTO hwid_sessions (account_name, device_id, ip_address, token, login_time, last_heartbeat) " + "VALUES (?, ?, ?, ?, NOW(), NOW())");
			
			ps.setString(1, account);
			ps.setInt(2, deviceId);
			ps.setString(3, ip);
			ps.setString(4, token);
			
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void deactivateOldSessions(String account)
	{
		try (Connection con = ConnectionPool.getConnection())
		{
			PreparedStatement ps = con.prepareStatement("UPDATE hwid_sessions SET active=0 WHERE account_name=? AND active=1");
			
			ps.setString(1, account);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	public void restartAndDisconnetion(Player player)
	{
		if (player == null)
			return;
		
		try (Connection con = ConnectionPool.getConnection())
		{
			PreparedStatement ps = con.prepareStatement("UPDATE hwid_sessions SET active=0 WHERE account_name=? AND active=1");
			
			ps.setString(1, player.getAccountName());
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public int countActiveSessionsByHWID(int deviceId)
	{
		try (Connection con = ConnectionPool.getConnection())
		{
			PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM hwid_sessions " + "WHERE device_id=? AND active=1 " + "AND last_heartbeat > NOW() - INTERVAL 60 SECOND");
			
			ps.setInt(1, deviceId);
			
			ResultSet rs = ps.executeQuery();
			if (rs.next())
				return rs.getInt(1);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		
		
		return 0;
	}
	
	private static void addHardwarePair(List<String> conditions, List<String> values, String firstColumn, String firstValue, String secondColumn, String secondValue)
	{
		if (!isUsableHardwarePart(firstValue) || !isUsableHardwarePart(secondValue))
			return;
		
		conditions.add("(" + firstColumn + "=? AND " + secondColumn + "=?)");
		values.add(firstValue);
		values.add(secondValue);
	}
	
	private static boolean isUsableHardwarePart(String value)
	{
		if (value == null || value.isBlank())
			return false;
		
		final String normalized = value.toLowerCase();
		return !normalized.contains("fail") && !normalized.equals("unknown");
	}
}
