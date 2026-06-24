package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ConnectionPool;
import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class VoicedPassword implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands =
	{
		"password",
		"change_password"
	};
	
	public static final Logger LOGGER = Logger.getLogger(VoicedPassword.class.getName());
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		
		if (command.startsWith("password"))
			ShowHtml(activeChar);
		if (command.startsWith("change_password"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			String curPass = null;
			String newPass = null;
			String repPass = null;
			try
			{
				if (st.hasMoreTokens())
				{
					curPass = st.nextToken();
					newPass = st.nextToken();
					repPass = st.nextToken();
				}
				else
				{
					activeChar.sendMessage("Please fill in all the blanks before requesting for a password change.");
					return false;
				}
				changePassword(curPass, newPass, repPass, activeChar);
			}
			catch (Exception e)
			{
			}
		}
		return true;
	}
	
	public static boolean changePassword(String currPass, String newPass, String repeatNewPass, Player player)
	{
		if (newPass.length() < 3 || newPass.length() > 16)
		{
			player.sendMessage("Password length must be between 3 and 16 characters.");
			return false;
		}
		
		if (!newPass.equals(repeatNewPass))
		{
			player.sendMessage("Repeated password doesn't match the new password.");
			return false;
		}
		
		try
		{
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			
			String currentEncoded = Base64.getEncoder().encodeToString(md.digest(currPass.getBytes(StandardCharsets.UTF_8)));
			
			try (Connection con = ConnectionPool.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT password FROM accounts WHERE login = ?"))
			{
				ps.setString(1, player.getAccountName());
				
				try (ResultSet rs = ps.executeQuery())
				{
					if (!rs.next())
					{
						player.sendMessage("Account not found.");
						return false;
					}
					
					if (!currentEncoded.equals(rs.getString("password")))
					{
						player.sendMessage("The current password you've inserted is incorrect!");
						return false;
					}
				}
				
				String newEncoded = Base64.getEncoder().encodeToString(md.digest(newPass.getBytes(StandardCharsets.UTF_8)));
				
				try (PreparedStatement ps2 = con.prepareStatement("UPDATE accounts SET password = ? WHERE login = ?"))
				{
					ps2.setString(1, newEncoded);
					ps2.setString(2, player.getAccountName());
					ps2.executeUpdate();
				}
			}
			
			player.sendMessage("Your password has been changed successfully! You will be disconnected for security reasons.");
			
			ThreadPool.schedule(() -> player.getClient().closeNow(), 3000);
			
			return true;
		}
		catch (Exception e)
		{
			LOGGER.warning("Failed to change password for account: " + player.getAccountName());
			LOGGER.warning(e.getMessage());
			return false;
		}
	}
	
	private static void ShowHtml(Player activeChar)
	{
		String htmFile = "data/html/mods/menu/password.htm";
		NpcHtmlMessage msg = new NpcHtmlMessage(5);
		msg.setFile(htmFile);
		activeChar.sendPacket(msg);
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
	
}
