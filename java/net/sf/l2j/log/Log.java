package net.sf.l2j.log;

/**
 * @version 0.1, 2005-06-06
 * @author Balancer
 */
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Log
{
	private static final Logger _log = Logger.getLogger(Log.class.getName());
	
	public static final void add(String text, String cat)
	{
		String date = (new SimpleDateFormat("yy.MM.dd H:mm:ss")).format(new Date());
		String curr = (new SimpleDateFormat("yyyy-MM-dd-")).format(new Date());
		new File("log/game").mkdirs();
		
		final File file = new File("log/game/" + (curr != null ? curr : "") + (cat != null ? cat : "unk") + ".txt");
		try (FileWriter save = new FileWriter(file, true))
		{
			String out = "[" + date + "] " + text + "\n";
			save.write(out);
		}
		catch (IOException e)
		{
			_log.log(Level.WARNING, "Error saving logfile: ", e);
		}
	}
	public static final void addArrayNoDate(Object[] lines, String folderName, String fileName, boolean writeConsole)
	{
		try
		{
			File folder = new File("log/" + folderName);
			if (!folder.exists())
				folder.mkdirs();

			File file = new File(folder, fileName + ".txt");
			try (FileWriter fw = new FileWriter(file, true))
			{
				for (Object line : lines)
				{
					fw.write(line.toString());
					fw.write(System.lineSeparator());
				}
				fw.write(System.lineSeparator()); // espaço entre registros
			}

			if (writeConsole)
			{
				for (Object line : lines)
					System.out.println("[Log][" + folderName + "] " + line.toString());
			}
		}
		catch (IOException e)
		{
			_log.log(Level.WARNING, "Error saving logfile (addArrayNoDate): ", e);
		}
	}


}