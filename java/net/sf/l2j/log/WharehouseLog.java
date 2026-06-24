package net.sf.l2j.log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

public class WharehouseLog
{
	static
	{
		new File("log/Player Log/WharehouseLog").mkdirs();
	}

	private static final Logger _log = Logger.getLogger(WharehouseLog.class.getName());

	public static void auditGMAction(String target, String gmName,int i, ItemInstance newItem, int a, String params)
	{
		final File file = new File("log/Player Log/WharehouseLog/" + target + ".txt");
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
			save.write(MathUtil.formatDate(new Date(), "dd/MM/yyyy H:mm:ss") + " >> [" + gmName + "] >> " + newItem + " [+" + a + "] >> Obj_ID item: [" + i + "]\r\n");
		}
		catch (IOException e)
		{
			_log.log(Level.SEVERE, "WharehouseLog for Player " + gmName + " could not be saved: ", e);
		}
	}

	public static void auditGMAction(String target, String gmName, int i, ItemInstance newItem, int a)
	{
		auditGMAction(target,gmName, i, newItem, a, "");
	}
}