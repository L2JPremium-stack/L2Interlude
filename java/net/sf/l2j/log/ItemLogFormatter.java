package net.sf.l2j.log;

import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

/**
 * @author Advi
 */
public class ItemLogFormatter extends Formatter
{
	private static final String CRLF = "\r\n";
	
	@Override
	public String format(LogRecord record)
	{
		final StringBuilder sb = new StringBuilder();
		
		StringUtil.append(sb, "[", new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(record.getMillis()), "] ", record.getMessage());
		
		for (Object p : record.getParameters())
		{
			if (p == null)
				continue;
			
			sb.append(", ");
			if (p instanceof ItemInstance)
			{
				final ItemInstance item = (ItemInstance) p;
				
				StringUtil.append(sb, "item ", item.getObjectId(), ":");
				if (item.getEnchantLevel() > 0)
					StringUtil.append(sb, "+", item.getEnchantLevel(), " ");
				
				StringUtil.append(sb, item.getItem().getName(), "(", item.getCount(), ")");
			}
			else
				sb.append(p.toString());
		}
		sb.append(CRLF);
		
		return sb.toString();
	}
}