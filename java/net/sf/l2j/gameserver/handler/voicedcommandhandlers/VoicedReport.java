package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.StringTokenizer;

import net.sf.l2j.gameserver.datatables.GmListTable;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.clientpackets.Say2;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class VoicedReport implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"report",
		"send_report"
	};
	private static String _type;
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (command.startsWith("report"))
		{
			mainHtml(activeChar);
		}
		else if (command.startsWith("send_report"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			String msg = "";
			String type = null;
			type = st.nextToken();
			
			try
			{
				while (st.hasMoreTokens())
				{
					msg = msg + " " + st.nextToken();
				}
				
				if (msg.equals(""))
				{
					activeChar.sendMessage("Message box cannot be empty.");
					return false;
				}
				
				sendReport(activeChar, type, msg);
				
			}
			catch (StringIndexOutOfBoundsException e){}
		}
		mainHtml(activeChar);
		
		return true;
		
	}
	
	private static void sendReport(Player player, String command, String msg)
	{
		String type = command;
		L2GameClient info = player.getClient().getConnection().getClient();
		
		if (type.equals("General"))
			_type = "General";
		if (type.equals("Fatal"))
			_type = "Fatal";
		if (type.equals("Misuse"))
			_type = "Misuse";
		if (type.equals("Balance"))
			_type = "Balance";
		if (type.equals("Other"))
			_type = "Other";
		
		try
		{
			String fname = "log/BugReports/" + player.getName() + ".txt";
			File file = new File(fname);
			
			boolean exist = file.createNewFile();
			if (!exist)
			{
				player.sendMessage("You have already sent a bug report, GMs must check it first.");
				player.sendPacket(new ExShowScreenMessage("You have already sent a bug report!", 4000, ExShowScreenMessage.SMPOS.MIDDLE_RIGHT, false));
				
				return;
			}
			FileWriter fstream = new FileWriter(fname);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("Character Info: " + info + "\r\nBug Type: " + _type + "\r\nMessage: " + msg);
			player.sendMessage("Report sent. GMs will check it soon. Thanks...");
			player.sendPacket(new ExShowScreenMessage("Report sent successfully!", 4000, ExShowScreenMessage.SMPOS.MIDDLE_RIGHT, false));
			
			for (Player allgms : GmListTable.getInstance().getAllGms(true))
				allgms.sendPacket(new CreatureSay(0, Say2.SHOUT, "Bug Report Manager", player.getName() + " sent a bug report."));
			
			System.out.println("Character: " + player.getName() + " sent a bug report.");
			out.close();
		}
		catch (Exception e)
		{
			player.sendMessage("Something went wrong try again.");
		}
	}
	
	static
	{
		new File("log/BugReports/").mkdirs();
	}
	
	public static void mainHtml(Player activeChar)
	{	
		String htmFile = "data/html/mods/menu/report.htm";		
		NpcHtmlMessage msg = new NpcHtmlMessage(5);
		msg.setFile(htmFile);
		msg.replace("%player%", activeChar.getName());
		activeChar.sendPacket(msg);
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
	
}