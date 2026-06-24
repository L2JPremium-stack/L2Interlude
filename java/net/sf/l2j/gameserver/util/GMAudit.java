package net.sf.l2j.gameserver.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GMAudit
{
	static
	{
		new File("log/GMAudit").mkdirs();
	}
	
	private static final Logger _log = Logger.getLogger(GMAudit.class.getName());
	
	public static void auditGMAction(String gmName, String action, String target, String params)
	{
		final File file = new File("log/GMAudit/" + gmName + ".txt");
		if (!file.exists())
			try
			{
				file.createNewFile();
			}
			catch (IOException e)
			{
			}
		
		try (FileWriter save = new FileWriter(file, true))
		{
			save.write(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()) + ">" + gmName + ">" + action + ">" + target + ">" + params + "\r\n");
		}
		catch (IOException e)
		{
			_log.log(Level.SEVERE, "GMAudit for GM " + gmName + " could not be saved: ", e);
		}
	}
	
	public static void auditGMAction(String gmName, String action, String target)
	{
		auditGMAction(gmName, action, target, "");
	}
}