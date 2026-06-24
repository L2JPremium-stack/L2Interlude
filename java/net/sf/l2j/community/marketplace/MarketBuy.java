package net.sf.l2j.community.marketplace;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.commons.math.MathUtil;

public class MarketBuy
{
	static
	{
		new File("log/Player Log/marketBuy").mkdirs();
	}

	private static final Logger _log = Logger.getLogger(MarketBuy.class.getName());

	public static void Log(String vendedor_name, String player_name, String itemname, int enchant, int cont, String params)
	{
		final File file = new File("log/Player Log/marketBuy/" + player_name + ".txt");
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
			if (enchant > 0)
				save.write("Data ["+MathUtil.formatDate(new Date(), "dd/MM/yyyy H:mm:ss") + "] Player Vendedor: [" + vendedor_name + "] > Player Comprador: [" + player_name + "] Comprou Item: [" + itemname + "] +[" + enchant + "]\r\n");
			else
				save.write("Data ["+MathUtil.formatDate(new Date(), "dd/MM/yyyy H:mm:ss") + "] Player Vendedor: [" + vendedor_name + "] > Player Comprador: [" + player_name + "] Comprou Item: [" + cont + "] [" + itemname + "]\r\n");
		}
		catch (IOException e)
		{
			_log.log(Level.SEVERE, "marketBuy for Player " + player_name + " could not be saved: ", e);
		}
	}

	public static void Log(String vendedor_name, String player_name, String itemname, int enchant, int cont)
	{
		Log(vendedor_name, player_name, itemname, enchant, cont, "");
	}
}