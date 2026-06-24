package net.sf.l2j.log;

import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import net.sf.l2j.commons.lang.StringUtil;

public class ChatLogFormatter extends Formatter
{
	private static final String CRLF = "\r\n";
	
	@Override
	public String format(LogRecord record)
	{
		final StringBuilder sb = new StringBuilder();
		
		StringUtil.append(sb, "[", new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(record.getMillis()), "] ");
		
		for (Object p : record.getParameters())
		{
			if (p == null)
				continue;
			
			StringUtil.append(sb, p, " ");
		}
		
		StringUtil.append(sb, record.getMessage(), CRLF);
		
		return sb.toString();
	}
}