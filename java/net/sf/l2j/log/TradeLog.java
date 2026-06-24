package net.sf.l2j.log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.commons.math.MathUtil;

public class TradeLog
{
	static
	{
		new File("log/Player Log/TradeLog").mkdirs();
	}

	private static final Logger _log = Logger.getLogger(TradeLog.class.getName());

	public static void auditGMAction(String gmName, String action, String string,int i,int j, String params)
	{
		final File file = new File("log/Player Log/TradeLog/" + gmName + ".txt");
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
			save.write(MathUtil.formatDate(new Date(), "dd/MM/yyyy H:mm:ss") + " >> Trade com  [" + action + "] >> Item: [" + string + "] +"+i+" >> Obj_id: [" + j + "]\r\n");
		}
		catch (IOException e)
		{
			_log.log(Level.SEVERE, "TradeLog for Player " + gmName + " could not be saved: ", e);
		}
	}

	public static void auditGMAction(String gmName, String action, String string ,int i,int j)
	{
		auditGMAction(gmName, action, string,i,j, "");
	}
}