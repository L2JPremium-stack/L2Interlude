package net.sf.l2j.dropdiary;

import java.io.File;
import java.io.FileOutputStream;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.l2j.gameserver.util.Broadcast;
import org.w3c.dom.*;

public class DropDiaryManager
{
	private static final Map<Integer, Integer> _limits = new HashMap<>();
	private static final Map<Integer, Integer> _currentCounts = new HashMap<>();
	private static final List<LocalTime> _resetTimes = new ArrayList<>();
	private static final String FILE_PATH = "data/xml/custom/DropsDiarys.xml";

	private static ScheduledExecutorService _scheduler;

	public static void load()
	{
		_limits.clear();
		_currentCounts.clear();
		_resetTimes.clear();

		try
		{
			File file = new File(FILE_PATH);
			if (!file.exists())
			{
				System.out.println("DropDiaryManager: XML file not found: " + FILE_PATH);
				return;
			}

			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
			doc.getDocumentElement().normalize();

			Element root = doc.getDocumentElement();
			if (root.hasAttribute("resetTime"))
			{
				String[] times = root.getAttribute("resetTime").split(";");
				for (String t : times)
				{
					try
					{
						_resetTimes.add(LocalTime.parse(t.trim()));
					}
					catch (DateTimeParseException e)
					{
						System.out.println("DropDiaryManager: Invalid time format in XML: " + t);
					}
				}
			}

			NodeList list = root.getElementsByTagName("item");
			for (int i = 0; i < list.getLength(); i++)
			{
				Node node = list.item(i);
				if (node.getNodeType() != Node.ELEMENT_NODE)
					continue;

				Element itemElement = (Element) node;
				int itemId = Integer.parseInt(itemElement.getAttribute("itemId"));
				int limit = Integer.parseInt(itemElement.getAttribute("LimiteDiary"));

				int current = 0;
				if (itemElement.hasAttribute("currentCount"))
					current = Integer.parseInt(itemElement.getAttribute("currentCount"));

				_limits.put(itemId, limit);
				_currentCounts.put(itemId, current);
			}

			System.out.println("DropDiaryManager: Loaded " + _limits.size() + " item limits.");
			System.out.println("DropDiaryManager: Reset times: " + _resetTimes);

			scheduleResets();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void scheduleResets()
	{
		if (_scheduler != null && !_scheduler.isShutdown())
			_scheduler.shutdownNow();

		_scheduler = Executors.newScheduledThreadPool(_resetTimes.size());

		LocalDateTime now = LocalDateTime.now();
		LocalTime closestTime = null;
		long shortestDelay = Long.MAX_VALUE;

		for (LocalTime resetTime : _resetTimes)
		{
			LocalDateTime nextReset = now.withHour(resetTime.getHour()).withMinute(resetTime.getMinute()).withSecond(0).withNano(0);
			if (now.compareTo(nextReset) >= 0)
				nextReset = nextReset.plusDays(1);

			long delay = Duration.between(now, nextReset).toMillis();

			_scheduler.scheduleAtFixedRate(() -> resetDiary(), delay, TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS);

			if (delay < shortestDelay)
			{
				shortestDelay = delay;
				closestTime = resetTime;
			}
		}

		if (closestTime != null)
			System.out.println("[DropDiaryManager] Next drop reset scheduled at " + closestTime);
	}


	private static synchronized void resetDiary()
	{
		_currentCounts.clear();
		Broadcast.gameAnnounceToOnlinePlayers("All Drop Diary limits have been reset.");
		saveProgress();
	}

	public static synchronized boolean canDrop(int itemId)
	{
		if (!_limits.containsKey(itemId))
			return true;

		int current = _currentCounts.getOrDefault(itemId, 0);
		int limit = _limits.get(itemId);
		return current < limit;
	}

	public static synchronized void recordDrop(int itemId, int count)
	{
		if (!_limits.containsKey(itemId))
			return;

		int current = _currentCounts.getOrDefault(itemId, 0);
		_currentCounts.put(itemId, current + count);
		saveProgress();
	}

	public static synchronized int getCount(int itemId)
	{
		return _currentCounts.getOrDefault(itemId, 0);
	}

	public static synchronized int getLimit(int itemId)
	{
		return _limits.getOrDefault(itemId, 0);
	}

	public static synchronized String getResetTimesAsString()
	{
		return _resetTimes.stream().map(LocalTime::toString).collect(Collectors.joining(";"));
	}

	public static synchronized void saveProgress()
	{
		try
		{
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

			Element root = doc.createElement("dropDiaryProgress");
			root.setAttribute("resetTime", getResetTimesAsString());
			doc.appendChild(root);

			for (Map.Entry<Integer, Integer> entry : _limits.entrySet())
			{
				int itemId = entry.getKey();
				int limit = entry.getValue();
				int current = _currentCounts.getOrDefault(itemId, 0);

				Element item = doc.createElement("item");
				item.setAttribute("itemId", String.valueOf(itemId));
				item.setAttribute("LimiteDiary", String.valueOf(limit));
				item.setAttribute("currentCount", String.valueOf(current));
				root.appendChild(item);
			}

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

			DOMSource source = new DOMSource(doc);

			// try-with-resources garante fechamento automático
			try (FileOutputStream fos = new FileOutputStream(FILE_PATH))
			{
				StreamResult result = new StreamResult(fos);
				transformer.transform(source, result);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}


	public static synchronized void shutdown()
	{
		if (_scheduler != null && !_scheduler.isShutdown())
		{
			_scheduler.shutdownNow();
		}
		saveProgress();
	}
}
