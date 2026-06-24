package net.sf.l2j.log;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import net.sf.l2j.commons.lang.StringUtil;

public class ConsoleLogFormatter extends Formatter
{
	private static final String CRLF = "\r\n";
	
	@Override
	public String format(LogRecord record)
	{
		final StringBuilder sb = new StringBuilder(500);
		
		StringUtil.append(sb, record.getMessage(), CRLF);
		
		if (record.getThrown() != null)
		{
			try
			{
				StringUtil.append(sb, record.getThrown().getMessage(), CRLF);
			}
			catch (Exception ex)
			{
			}
		}
		return sb.toString();
	}
}