package net.sf.l2j.gameserver.model.actor.instance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

import net.sf.l2j.fusionitems.FusionItemData;
import net.sf.l2j.fusionitems.FusionItemData.Category;
import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.xml.IconTable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * NPC de fusão de itens (links simples; paginação, busca e barra de progresso).
 */
public class L2FusionItemsInstance extends L2NpcInstance
{
	// HTMLs
	private static final String MAIN_HTML = "data/html/mods/fusionNpc/ItemFusion.htm";
	private static final String PROGRESS_HTML = "data/html/mods/fusionNpc/Progress.htm"; // opcional (tem fallback inline)
	
	// UI / lista
	private static final int PAGE_SIZE = 6;
	
	// Progresso da fusão
	private static final int FUSION_TIME_MS = 5000; // 5s
	private static final int PROGRESS_STEPS = 20; // 20 ticks
	private static final int BASE_INTERACT_DISTANCE = 150;
	private static final int MAX_DISTANCE_DURING_FUSION = 200;
	
	private static final class ProgressHandle
	{
		public final List<ScheduledFuture<?>> tasks = new CopyOnWriteArrayList<>();
		public volatile boolean cancelled = false;
	}
	
	private static final Map<Integer, ProgressHandle> IN_PROGRESS = new ConcurrentHashMap<>();
	
	public L2FusionItemsInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String event)
	{
		if (event == null || event.isEmpty())
			return;
		
		try
		{
			// Listagens por categoria com paginação/busca: "list_<CAT> <page> <query...>"
			if (event.startsWith("list_WEAPONS"))
			{
				int[] p = parsePageAndQuery(event);
				sendCategory(player, Category.WEAPONS, p[0], _lastQuery);
				return;
			}
			if (event.startsWith("list_ARMOR"))
			{
				int[] p = parsePageAndQuery(event);
				sendCategory(player, Category.ARMOR, p[0], _lastQuery);
				return;
			}
			if (event.startsWith("list_JEWELS"))
			{
				int[] p = parsePageAndQuery(event);
				sendCategory(player, Category.JEWELS, p[0], _lastQuery);
				return;
			}
			if (event.startsWith("list_ACCESSORIES"))
			{
				int[] p = parsePageAndQuery(event);
				sendCategory(player, Category.ACCESSORIES, p[0], _lastQuery);
				return;
			}
			
			// Iniciar fusão com progress bar
			if (event.startsWith("fuse_"))
			{
				int recipeIndex = Integer.parseInt(event.substring(5));
				startFusionWithProgress(player, recipeIndex);
				return;
			}
			else if (event.startsWith("view_"))
			{
				int index = Integer.parseInt(event.substring(5));
				FusionItemData.FusionRecipe recipe = FusionItemData.getInstance().getRecipe(index);
				if (recipe == null)
				{
					player.sendMessage("Recipe not found or not loaded.");
					return;
				}
				
				StringBuilder htmlText = new StringBuilder();
				htmlText.append("<html><body><center>");
				htmlText.append("<font color=\"LEVEL\">Fusion Requirements</font><br><br>");
				htmlText.append("<table width=285>");
				
				for (FusionItemData.FusionIngredient ing : recipe.ingredients)
				{
					int equippedCount = 0;
					for (ItemInstance item : player.getInventory().getPaperdollItems())
						if (item != null && item.getItemId() == ing.itemId)
							equippedCount += item.getCount();
						
					long totalCount = player.getInventory().getInventoryItemCount(ing.itemId, -1);
					long availableCount = Math.max(0, totalCount - equippedCount);
					
					String color = availableCount >= ing.count ? "00FF00" : "FF0000";
					String iconName = IconTable.getIcon(ing.itemId);
					if (iconName == null || iconName.isEmpty())
						iconName = "icon.etc_question_mark_i00";
					
					htmlText.append("<tr>");
					htmlText.append("<td width=36 align=center><img src=\"").append(iconName).append("\" width=32 height=32></td>");
					htmlText.append("<td align=left>• <font color=\"").append(color).append("\">").append(ing.count).append(" × ").append(getItemName(ing.itemId)).append("</font> (You have: ").append(availableCount).append(")</td>");
					htmlText.append("</tr>");
				}
				
				htmlText.append("</table><br>");
				htmlText.append("Chance: <font color=LEVEL>").append(recipe.chance).append("%</font><br><br>");
				
				// Ações lado a lado
				htmlText.append("<table width=200><tr>");
				htmlText.append("<td width=80 align=center>").append("<a action=\"bypass -h npc_").append(getObjectId()).append("_fuse_").append(index).append("\">Fusion Now</a>").append("</td>");
				htmlText.append("<td width=80 align=center>").append("<a action=\"bypass -h npc_").append(getObjectId()).append("_back\">Return</a>").append("</td>");
				htmlText.append("</tr></table>");
				
				htmlText.append("</center></body></html>");
				
				NpcHtmlMessage msg = new NpcHtmlMessage(getObjectId());
				msg.setHtml(htmlText.toString());
				player.sendPacket(msg);
				return;
			}
			else if (event.equalsIgnoreCase("back"))
			{
				showChatWindow(player);
				return;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			player.sendMessage("Error: " + e.getMessage());
		}
		
		super.onBypassFeedback(player, event);
	}
	
	// ================== PROGRESSO DA FUSÃO ==================
	
	private void startFusionWithProgress(final Player player, final int recipeIndex)
	{
		if (player == null)
			return;
		
		final int objId = player.getObjectId();
		if (IN_PROGRESS.containsKey(objId))
		{
			player.sendMessage("Aguarde a fusão atual terminar.");
			return;
		}
		
		// snapshot de posição para detectar movimento
		final int startX = player.getX(), startY = player.getY(), startZ = player.getZ();
		
		// travas básicas
		player.abortAttack();
		player.abortCast();
		player.disableAllSkills();
		player.setIsImmobilized(true);
		
		player.sendMessage("Iniciando fusão...");
		
		final ProgressHandle ph = new ProgressHandle();
		IN_PROGRESS.put(objId, ph);
		
		final int period = Math.max(1, FUSION_TIME_MS / PROGRESS_STEPS);
		final int[] current =
		{
			0
		};
		
		// render inicial (0%)
		sendProgressHtml(player, 0, PROGRESS_STEPS);
		
		ScheduledFuture<?> ticker = ThreadPool.scheduleAtFixedRate(() -> {
			try
			{
				if (ph.cancelled)
					return;
				
				// safety checks por tick
				if (!isPlayerSessionActive(player))
				{
					cancelProgress(player, ph, "Fusão cancelada: desconectado.");
					return;
				}
				if (player.isDead() || player.isAlikeDead())
				{
					cancelProgress(player, ph, "Fusão cancelada: você morreu.");
					return;
				}
				if (!player.isInsideRadius(this, BASE_INTERACT_DISTANCE + MAX_DISTANCE_DURING_FUSION, true, false))
				{
					cancelProgress(player, ph, "Fusão cancelada: você se afastou do NPC.");
					return;
				}
				if (player.getDistanceSq(startX, startY, startZ) > (MAX_DISTANCE_DURING_FUSION * MAX_DISTANCE_DURING_FUSION))
				{
					cancelProgress(player, ph, "Fusão cancelada: você se moveu.");
					return;
				}
				
				// avança barra
				sendProgressHtml(player, current[0], PROGRESS_STEPS);
				current[0]++;
				
				// terminou?
				if (current[0] > PROGRESS_STEPS)
				{
					ph.cancelled = true;
					
					// libera player
					player.setIsImmobilized(false);
					player.enableAllSkills();
					
					// executa a fusão de fato
					boolean ok = false;
					try
					{
						ok = FusionItemData.getInstance().tryFusion(player, this, recipeIndex);
					}
					catch (Exception ignored)
					{
					}
					
					// mostra resultado na tela principal
					NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile(MAIN_HTML);
					html.replace("%objectId%", String.valueOf(getObjectId()));
					html.replace("%content%", "");
					
					final String resultText = ok ? "<font color=\"LEVEL\">Fusion processed!</font>" : "<font color=\"FF0000\">Fusion failed. You do not have the required items.</font>";
					html.replace("%result%", resultText);
					player.sendPacket(html);
					
					// cleanup
					IN_PROGRESS.remove(objId);
					ScheduledFuture<?> f = ph.tasks.isEmpty() ? null : ph.tasks.get(0);
					if (f != null)
						f.cancel(false);
				}
			}
			catch (Throwable t)
			{
				cancelProgress(player, ph, "Fusão cancelada por erro interno.");
			}
		}, period, period);
		
		ph.tasks.add(ticker);
	}
	
	private static boolean isPlayerSessionActive(Player p)
	{
		if (p == null)
			return false;
		try
		{
			return p.getClient() != null && !p.getClient().isDetached();
		}
		catch (Throwable t)
		{
			return true;
		}
	}
	
	private void sendProgressHtml(Player player, int current, int max)
	{
		String bar = generateBar(125, 15, current, max);
		int percent = (int) Math.round((current / (double) Math.max(1, max)) * 100.0);
		
		String html = HtmCache.getInstance().getHtm(PROGRESS_HTML);
		if (html == null)
		{
			html = "<html><body><center>" + "<font color=\"LEVEL\">Processing fusion...</font><br1>" + "%progress_bar%<br1>" + "<font color=808080>%percent%%</font><br><br>" + "<a action=\"bypass -h npc_%objectId%_back\">Return</a>" + "</center></body></html>";
		}
		
		html = html.replace("%progress_bar%", bar).replace("%percent%", String.valueOf(percent)).replace("%objectId%", String.valueOf(getObjectId()));
		
		NpcHtmlMessage htm = new NpcHtmlMessage(getObjectId());
		htm.setHtml(html);
		player.sendPacket(htm);
	}
	
	private void cancelProgress(Player player, ProgressHandle ph, String reason)
	{
		ph.cancelled = true;
		for (ScheduledFuture<?> t : ph.tasks)
		{
			try
			{
				if (t != null)
					t.cancel(false);
			}
			catch (Exception ignored)
			{
			}
		}
		ph.tasks.clear();
		
		if (player != null)
		{
			IN_PROGRESS.remove(player.getObjectId());
			try
			{
				player.setIsImmobilized(false);
				player.enableAllSkills();
			}
			catch (Exception ignored)
			{
			}
			try
			{
				player.sendMessage(reason);
			}
			catch (Exception ignored)
			{
			}
			try
			{
				showChatWindow(player);
			}
			catch (Exception ignored)
			{
			}
		}
	}
	
	// ================== Helpers de paginação/busca ==================
	
	private String _lastQuery = "";
	
	private int[] parsePageAndQuery(String event)
	{
		int page = 0;
		String query = "";
		try
		{
			StringTokenizer st = new StringTokenizer(event, " ");
			st.nextToken(); // "list_XXX"
			if (st.hasMoreTokens())
			{
				String p = st.nextToken();
				page = Math.max(0, Integer.parseInt(p));
				
				List<String> parts = new ArrayList<>();
				while (st.hasMoreTokens())
					parts.add(st.nextToken());
				query = String.join(" ", parts).trim();
			}
		}
		catch (Exception ignored)
		{
		}
		_lastQuery = query;
		return new int[]
		{
			page
		};
	}
	
	private static String escapeHtml(String s)
	{
		if (s == null)
			return "";
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}
	
	private static boolean matchQuery(String itemName, int itemId, String query)
	{
		if (query == null || query.isEmpty())
			return true;
		final String nm = (itemName == null ? "" : itemName).toLowerCase();
		final String q = query.toLowerCase();
		String[] terms = q.split("\\s+");
		for (String t : terms)
		{
			if (t.isEmpty())
				continue;
			boolean hit = nm.contains(t) || String.valueOf(itemId).contains(t);
			if (!hit)
				return false;
		}
		return true;
	}
	
	private String buildPager(Category cat, int pageIndex, int totalPages, String query)
	{
		StringBuilder sb = new StringBuilder(256);
		sb.append("<img src=\"L2UI.SquareGray\" width=290 height=1>");
		sb.append("<table width=290><tr>");
		
		// Prev
		if (pageIndex > 0)
			sb.append("<td align=left width=80><a action=\"bypass -h npc_").append(getObjectId()).append("_list_").append(cat.name()).append(" ").append(pageIndex - 1).append(query == null || query.isEmpty() ? "" : " " + query).append("\">&lt; Prev</a></td>");
		else
			sb.append("<td align=left width=80><font color=808080>&lt; Prev</font></td>");
		
		// Indicador
		sb.append("<td align=center><font color=LEVEL>Page ").append(pageIndex + 1).append(" / ").append(totalPages).append("</font></td>");
		
		// Next
		if (pageIndex < totalPages - 1)
			sb.append("<td align=right width=80><a action=\"bypass -h npc_").append(getObjectId()).append("_list_").append(cat.name()).append(" ").append(pageIndex + 1).append(query == null || query.isEmpty() ? "" : " " + query).append("\">Next &gt;</a></td>");
		else
			sb.append("<td align=right width=80><font color=808080>Next &gt;</font></td>");
		
		sb.append("</tr></table>");
		sb.append("<img src=\"L2UI.SquareGray\" width=290 height=1>");
		return sb.toString();
	}
	
	// ================== Listagem paginada + busca ==================
	
	private void sendCategory(Player player, Category cat, int pageIndex, String query)
	{
		List<Integer> all = FusionItemData.getInstance().getIndexesByCategory(cat);
		
		// filtro por query
		List<Integer> filtered = new ArrayList<>(all.size());
		for (int idx : all)
		{
			FusionItemData.FusionRecipe r = FusionItemData.getInstance().getRecipe(idx);
			if (r == null)
				continue;
			String name = getItemName(r.resultId);
			if (matchQuery(name, r.resultId, query))
				filtered.add(idx);
		}
		
		final int total = filtered.size();
		final int totalPages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
		if (pageIndex >= totalPages)
			pageIndex = totalPages - 1;
		if (pageIndex < 0)
			pageIndex = 0;
		
		final int start = pageIndex * PAGE_SIZE;
		final int end = Math.min(start + PAGE_SIZE, total);
		
		StringBuilder body = new StringBuilder(5000);
		
		body.append("<table width=290>");
		
		if (total == 0)
		{
			body.append("<tr><td align=center><font color=FF6666>No recipes in ").append(cat.display()).append(query == null || query.isEmpty() ? "" : " (filtered)").append(".</font></td></tr>");
		}
		else
		{
			for (int i = start; i < end; i++)
			{
				int idx = filtered.get(i);
				FusionItemData.FusionRecipe r = FusionItemData.getInstance().getRecipe(idx);
				if (r == null)
					continue;
				
				String icon = IconTable.getIcon(r.resultId);
				if (icon == null || icon.isEmpty())
					icon = "icon.etc_question_mark_i00";
				
				// Nome truncado
				String itemName = getItemName(r.resultId);
				itemName = (itemName == null) ? "Unknown Item" : itemName;
				if (itemName.length() > 26)
					itemName = itemName.substring(0, 23) + "...";
				itemName = escapeHtml(itemName);
				
				body.append("<tr>");
				body.append("<td width=36 align=center><img src=\"").append(icon).append("\" width=32 height=32></td>");
				body.append("<td align=left>");
				body.append("<font color=LEVEL>").append(itemName).append("</font><br1>");
				body.append("<font color=808080>Chance: ").append(r.chance).append("%</font>");
				body.append("</td>");
				
				body.append("<td width=20 align=center>").append("<a action=\"bypass -h npc_").append(getObjectId()).append("_view_").append(idx).append("\">Details</a>").append("</td>");
				body.append("<td width=20 align=center>").append("<a action=\"bypass -h npc_").append(getObjectId()).append("_fuse_").append(idx).append("\">Fuse</a>").append("</td>");
				
				body.append("</tr>");
				
			}
		}
		body.append("</table>");
		
		body.append("<br>").append(buildPager(cat, pageIndex, totalPages, (query == null ? "" : query)));
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(MAIN_HTML);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%result%", "");
		html.replace("%content%", body.toString());
		player.sendPacket(html);
	}
	
	// compat: chamadas antigas sem página nem busca
	@SuppressWarnings("unused")
	private void sendCategory(Player player, Category cat)
	{
		sendCategory(player, cat, 0, "");
	}
	
	public static String getItemName(int itemId)
	{
		try
		{
			return ItemTable.getInstance().getTemplate(itemId).getName();
		}
		catch (Exception e)
		{
			return "Unknown Item";
		}
	}
	
	@Override
	public void showChatWindow(Player player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(MAIN_HTML);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%result%", "");
		html.replace("%content%", "");
		player.sendPacket(html);
	}
	
	// ---------- barra de progresso (mesmo estilo do seu código) ----------
	private static String generateBar(int width, int height, int current, int max)
	{
		final StringBuilder sb = new StringBuilder();
		
		current = Math.min(Math.max(current, 0), Math.max(1, max));
		int percentage = (int) Math.round((current / (double) Math.max(1, max)) * 100.0);
		int barHP = Math.max((width * percentage / 100), 0);
		int barBG = Math.max(width - barHP, 0);
		
		sb.append("<table width=").append(width).append(" height=").append(height).append(" cellspacing=0 cellpadding=0>");
		sb.append("<tr>");
		
		sb.append("<td width=").append(barHP).append(" height=").append(height).append(" align=left>");
		if (barHP > 0)
			sb.append("<img src=\"L2UI_CH3.PlayerStatusWnd.ps_foodbar\" width=").append(barHP).append(" height=").append(height).append("/>");
		sb.append("</td>");
		
		sb.append("<td width=").append(barBG).append(" height=").append(height).append(" align=left>");
		if (barBG > 0)
			sb.append("<img src=\"L2UI_CH3.PlayerStatusWnd.ps_cpbar_back\" width=").append(barBG).append(" height=").append(height).append("/>");
		sb.append("</td>");
		
		sb.append("</tr></table>");
		
		return sb.toString();
	}
}
