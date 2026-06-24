package net.sf.l2j.log;

import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class FileLogFormatter extends Formatter
{
	private static final String CRLF = "\r\n";
	private static final String SPACE = "\t";
	
	@Override
	public String format(LogRecord record)
	{
		//return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(record.getMillis()) + SPACE + record.getLevel().getName() + SPACE + record.getThreadID() + SPACE + record.getLoggerName() + SPACE + record.getMessage() + CRLF;
		  return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(record.getMillis()) + SPACE + record.getLevel().getName()  + SPACE + record.getLoggerName() + SPACE + record.getMessage() + CRLF;
	}
}
