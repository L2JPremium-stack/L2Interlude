package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;

public class AdminSaveLoc implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = { "admin_saveloc" };

	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.startsWith("admin_saveloc"))
		{
			int x = activeChar.getX();
			int y = activeChar.getY();
			int z = activeChar.getZ();

			String line = String.format("<spawn X=\"%d\" Y=\"%d\" Z=\"%d\" />", x, y, z);

			try (FileWriter fw = new FileWriter("data/saved_locs.txt", true);
			     PrintWriter out = new PrintWriter(fw))
			{
				out.println(line);
				activeChar.sendMessage("Local salvo: " + line);
			}
			catch (IOException e)
			{
				activeChar.sendMessage("Erro ao salvar coordenadas.");
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
