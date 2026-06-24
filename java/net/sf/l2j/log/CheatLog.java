package net.sf.l2j.log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.commons.math.MathUtil;

public class CheatLog
{
	static
	{
		new File("log/Player Log/HwidLog").mkdirs();
	}

	private static final Logger _log = Logger.getLogger(CheatLog.class.getName());

	public static void auditGMAction(String gmName, String params)
	{
		final File file = new File("log/Player Log/CheatLog/" + gmName + ".txt");
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
			save.write(MathUtil.formatDate(new Date(), "dd/MM/yyyy H:mm:ss") + " >>> Player: [" + gmName + "] Tentou usar comando de GM!\r\n");
		}
		catch (IOException e)
		{
			_log.log(Level.SEVERE, "CheatLog for Player " + gmName + " could not be saved: ", e);
		}
	}

	public static void auditGMAction(String gmName)
	{
		auditGMAction(gmName, "");
	}
}