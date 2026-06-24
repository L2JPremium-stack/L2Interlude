package net.sf.l2j.launcher.etc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class InterfaceInfo extends JPanel
{
	private static final long serialVersionUID = -1;
	
	protected static final Logger LOGGER = Logger.getLogger(InterfaceInfo.class.getName());
	
	protected static final long START_TIME = System.currentTimeMillis();
	
	public InterfaceInfo()
	{
		setBackground(new Color(35, 38, 42));
		setBorder(new LineBorder(new Color(58, 61, 66), 1, true));
		setPreferredSize(new Dimension(270, 160));
		setLayout(new BorderLayout());
		
		
		
	}
	
	static String formatMemorySize(long bytes)
	{
		if (bytes < 1024)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(1024));
		char pre = "KMGTPE".charAt(exp - 1);
		double size = bytes / Math.pow(1024, exp);
		return String.format(exp == 1 ? "%.0f %sB" : "%.2f %sB", size, pre);
	}
	
	static String getDurationBreakdown(long millis)
	{
		long remaining = millis;
		final long days = TimeUnit.MILLISECONDS.toDays(remaining);
		remaining -= TimeUnit.DAYS.toMillis(days);
		final long hours = TimeUnit.MILLISECONDS.toHours(remaining);
		remaining -= TimeUnit.HOURS.toMillis(hours);
		final long minutes = TimeUnit.MILLISECONDS.toMinutes(remaining);
		remaining -= TimeUnit.MINUTES.toMillis(minutes);
		final long seconds = TimeUnit.MILLISECONDS.toSeconds(remaining);
		return (days + "d " + hours + "h " + minutes + "m " + seconds + "s");
	}
}
