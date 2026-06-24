package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import java.util.List;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.lang.Tokenizer;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.xml.AugmentStoneData;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.handler.itemhandlers.custom.AugmentStone;
import net.sf.l2j.gameserver.instancemanager.custom.CustomAugmentManager;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.holder.AugmentStoneHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class VoicedAugmentStone implements IVoicedCommandHandler
{
	@Override
	public boolean useVoicedCommand(String command, Player player, String target)
	{
		if (!command.startsWith("augment"))
			return false;
		
		final Tokenizer tokenizer = new Tokenizer(command);
		final String param = tokenizer.getToken(1);
		
		if (param == null)
			return true;
		
		switch (param.toLowerCase())
		{
			case "passive":
			case "active":
			{
				showAugmentList(player, param.toUpperCase(), 0);
				break;
			}
			case "list":
			{
				String type = tokenizer.getToken(2);
				int page = Integer.parseInt(tokenizer.getToken(3));
				
				showAugmentList(player, type, page);
				break;
			}
			case "index":
			{
				AugmentStone reUse = new AugmentStone();
				
				reUse.useItem(player, player.getInventory().getItemByObjectId(player.getTempAugmentItem()), false);
				break;
			}
			case "info":
			{
				showInfoPage(player);
				break;
			}
			case "admin":
			{
				if (!player.isGM())
					return true;
				
				showAdminPage(player);
				break;
			}
			case "buy":
			{
				if (player.getActiveWeaponInstance() == null)
				{
					player.sendMessage("Equip a weapon first.");
					return true;
				}
				if (player.getTempAugmentItem() == 0)
				{
					player.sendMessage("No augment session active.");
					return true;
				}
				int index = Integer.parseInt(tokenizer.getToken(2));
				String type = tokenizer.getToken(3);
				
				List<AugmentStoneHolder> list = AugmentStoneData.getInstance().getByType(type);
				
				if (index < 0 || index >= list.size())
					return true;
				
				AugmentStoneHolder holder = list.get(index);
				
				// =========================
				// 1. valida item que abriu
				// =========================
				ItemInstance stone = player.getInventory().getItemByObjectId(player.getTempAugmentItem());
				
				if (stone == null)
				{
					player.sendMessage("Invalid augment stone.");
					return true;
				}
				
				// =========================
				// 2. valida preço
				// =========================
				if (!player.destroyItemByItemId("AugmentPrice", holder.getPriceItemId(), holder.getPriceCount(), player, true))
				{
					player.sendMessage("Not enough items.");
					return true;
				}
				
				// =========================
				// 3. consome a stone (IMPORTANTE)
				// =========================
				player.destroyItem("AugmentStone", stone.getObjectId(), 1, player, true);
				
				// =========================
				// 4. aplica augment
				// =========================
				CustomAugmentManager.getInstance().applyAugment(player, holder);
				
				player.sendMessage("Augment applied successfully.");
				
				break;
			}
		}
		
		return true;
	}
	
	public static void showAugmentList(Player player, String type, int page)
	{
		List<AugmentStoneHolder> list = AugmentStoneData.getInstance().getByType(type);
		
		int perPage = 5;
		int start = page * perPage;
		int end = Math.min(start + perPage, list.size());
		
		StringBuilder sb = new StringBuilder();
		sb.append("<html><body><center>");
		sb.append("<font color=\"LEVEL\">" + type + " AUGMENTS</font><br><br>");
		
		for (int i = start; i < end; i++)
		{
			AugmentStoneHolder a = list.get(i);
			
			sb.append("<table width=260><tr>");
			sb.append("<td width=32><img src=\"" + a.getIcon() + "\" width=32 height=32></td>");
			sb.append("<td>");
			sb.append(a.getTitle() + "<br1>");
			
			final String safeName = truncate(escape(ItemTable.getInstance().getTemplate(a.getPriceItemId()).getName()), 30);
			
			sb.append("Price: " + StringUtil.formatNumber(a.getPriceCount()) + " " + safeName);
			sb.append("</td>");
			sb.append("<td>");
			sb.append("<button value=\"Select\" ");
			int globalIndex = i;
			
			sb.append("action=\"bypass -h voiced_augment buy " + globalIndex + " " + type + "\" ");
			sb.append("width=95 height=25 back=\"L2UI_JDEV.AdminGreenBtn_Down\" fore=\"L2UI_JDEV.AdminGreenBtn\">");
			sb.append("</td>");
			sb.append("</tr></table><br>");
		}
		
		// PAGINAÇÃO
		sb.append("<br>");
		
		sb.append("<br><table width=300><tr>");
		
		// =========================
		// PREV (LEFT)
		// =========================
		sb.append("<td align=left>");
		
		if (page > 0)
		{
			sb.append("<button value=\"<< Prev\" ");
			sb.append("action=\"bypass voiced_augment list " + type + " " + (page - 1) + "\" ");
			sb.append("width=95 height=25 back=\"L2UI_JDEV.AdminGreenBtn_Down\" fore=\"L2UI_JDEV.AdminGreenBtn\">");
		}
		else
		{
			sb.append("&nbsp;"); // mantém alinhamento
		}
		
		sb.append("</td>");
		
		// =========================
		// INDEX (CENTER)
		// =========================
		sb.append("<td align=center>");
		
		sb.append("<button value=\"Menu\" ");
		sb.append("action=\"bypass voiced_augment index\" ");
		sb.append("width=95 height=25 back=\"L2UI_JDEV.AdminGreenBtn_Down\" fore=\"L2UI_JDEV.AdminGreenBtn\">");
		
		sb.append("</td>");
		
		// =========================
		// NEXT (RIGHT)
		// =========================
		sb.append("<td align=right>");
		
		if (end < list.size())
		{
			sb.append("<button value=\"Next >>\" ");
			sb.append("action=\"bypass voiced_augment list " + type + " " + (page + 1) + "\" ");
			sb.append("width=95 height=25 back=\"L2UI_JDEV.AdminGreenBtn_Down\" fore=\"L2UI_JDEV.AdminGreenBtn\">");
		}
		else
		{
			sb.append("&nbsp;");
		}
		
		sb.append("</td>");
		
		sb.append("</tr></table>");
		
		sb.append("</center></body></html>");
		
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setHtml(sb.toString());
		player.sendPacket(html);
	}
	
	private static String truncate(String s, int max)
	{
		if (s == null)
			return "";
		if (s.length() <= max)
			return s;
		return s.substring(0, max - 3) + "...";
	}
	
	private static String escape(String s)
	{
		if (s == null)
			return "";
		// Minimal escape for L2 HTML
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}
	
	private static final String[] VOICED_COMMANDS =
	{
		"augment"
	};
	
	private static void showInfoPage(Player player)
	{
	    StringBuilder sb = new StringBuilder();

	    sb.append("<html><body><center>");

	    // ===== HEADER =====
	    sb.append("<font color=\"LEVEL\">AUGMENT SYSTEM</font><br>");
	    sb.append("<img src=\"L2UI.SquareGray\" width=260 height=1><br1><br1>");

	    // =========================
	    // HOW IT WORKS
	    // =========================
	    sb.append("<font color=\"B09878\">How it works:</font><br1><br1>");

	    sb.append("<table width=260>");

	    addLine(sb, "Use an <font color=\"LEVEL\">Augment Stone</font>");
	    addLine(sb, "Select <font color=\"00BFFF\">Passive</font> or <font color=\"FF6666\">Active</font>");
	    addLine(sb, "Choose your desired augment");
	    addLine(sb, "Pay the required <font color=\"FFD700\">price</font>");

	    sb.append("</table><br1>");

	    sb.append("<img src=\"L2UI.SquareGray\" width=260 height=1><br1><br1>");

	    // =========================
	    // RULES
	    // =========================
	    sb.append("<font color=\"LEVEL\">Important Rules:</font><br1><br1>");

	    sb.append("<table width=260>");

	    addLine(sb, "Augment is bound to the <font color=\"LEVEL\">weapon</font>");
	    addLine(sb, "<font color=\"FF5555\">Unequip</font> = skill removed");
	    addLine(sb, "<font color=\"00FF99\">Trade</font> = augment transfers");
	    addLine(sb, "<font color=\"00FF99\">Drop</font> = new owner receives it");
	    addLine(sb, "<font color=\"FF5555\">Destroy</font> = augment is lost");

	    sb.append("</table><br1>");

	    sb.append("<img src=\"L2UI.SquareGray\" width=260 height=1><br><br>");

	    // =========================
	    // NOTES
	    // =========================
	    sb.append("<font color=\"FF9900\">Notes:</font><br1><br1>");

	    sb.append("<table width=260>");

	    addLine(sb, "Only <font color=\"LEVEL\">one augment</font> per weapon");
	    addLine(sb, "Replacing removes previous augment");

	    sb.append("</table><br1>");

	    // =========================
	    // BUTTON
	    // =========================
	    sb.append("<br><button value=\"Back\" ");
	    sb.append("action=\"bypass voiced_augment index\" ");
	    sb.append("width=95 height=25 ");
	    sb.append("back=\"L2UI_JDEV.AdminGreenBtn_Down\" ");
	    sb.append("fore=\"L2UI_JDEV.AdminGreenBtn\">");

	    sb.append("</center></body></html>");

	    NpcHtmlMessage html = new NpcHtmlMessage(0);
	    html.setHtml(sb.toString());
	    player.sendPacket(html);
	}
	
	private static void addLine(StringBuilder sb, String text)
	{
	    sb.append("<tr>");
	    
	    // DOT
	    sb.append("<td width=12 align=center>");
	    sb.append("<img src=\"L2UI_JDEV.Dot\" width=8 height=8>");
	    sb.append("</td>");
	    
	    // TEXT
	    sb.append("<td align=left>");
	    sb.append("<font color=\"CCCCCC\">");
	    sb.append(text);
	    sb.append("</font>");
	    sb.append("</td>");
	    
	    sb.append("</tr>");
	}
	
	private static void showAdminPage(Player player)
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("<html><body><center>");
		sb.append("<font color=\"FF9900\">AUGMENT ADMIN PANEL</font><br>");
		sb.append("<img src=\"L2UI.SquareGray\" width=260 height=1><br><br>");
		
		ItemInstance weapon = player.getActiveWeaponInstance();
		
		if (weapon == null)
		{
			sb.append("No weapon equipped.");
		}
		else
		{
			int[] data = CustomAugmentManager.getInstance().getAugment(weapon.getObjectId());
			
			sb.append("Weapon: " + weapon.getName() + "<br>");
			
			if (data != null)
			{
				sb.append("Skill ID: " + data[0] + "<br>");
				sb.append("Level: " + data[1] + "<br>");
			}
			else
			{
				sb.append("No augment applied.<br>");
			}
		}
		
		sb.append("<br><button value=\"Back\" ");
		sb.append("action=\"bypass voiced_augment index\" ");
		sb.append("width=95 height=25 back=\"L2UI_JDEV.AdminGreenBtn_Down\" fore=\"L2UI_JDEV.AdminGreenBtn\">");
		
		sb.append("</center></body></html>");
		
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setHtml(sb.toString());
		player.sendPacket(html);
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
