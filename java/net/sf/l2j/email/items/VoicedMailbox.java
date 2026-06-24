package net.sf.l2j.email.items;

import java.util.List;

import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class VoicedMailbox implements IVoicedCommandHandler
{
    private static final String[] _voicedCommands = { "mailbox" };

    @Override
    public boolean useVoicedCommand(String command, Player activeChar, String target)
    {
        showMailbox(activeChar, 1);
        return true;
    }

    public static void showMailbox(Player player, int page)
    {
        final int ITEMS_PER_PAGE = 13;
        final int MAX_ITEM_NAME_LENGTH = 22;

        List<MailData> mails = MailManager.getInstance().getMailsForPlayer(player.getObjectId());
        NpcHtmlMessage html = new NpcHtmlMessage(player.getObjectId());
        StringBuilder sb = new StringBuilder();

        sb.append("<html><body><center><font color=LEVEL size=5>Mailbox</font><br><br>");

        int totalItems = 0;
        for (MailData mail : mails)
        {
            if (!mail.isClaimed())
                totalItems++;
        }

        int maxPage = (int) Math.ceil(totalItems / (double) ITEMS_PER_PAGE);
        if (page < 1) page = 1;
        if (page > maxPage) page = maxPage;

        int startIndex = (page - 1) * ITEMS_PER_PAGE;
        int endIndex = page * ITEMS_PER_PAGE;

        int currentIndex = 0;

        sb.append("<table width=310 cellspacing=1 cellpadding=0 border=1>");

        // Cabeçalho simples
        sb.append("<tr>");
        sb.append("<td width=220><font color=FFFF00><b>Item</b></font></td>");
        sb.append("<td width=80 align=center><font color=FFFF00><b>Action</b></font></td>");
        sb.append("</tr>");

        for (MailData mail : mails)
        {
            if (mail.isClaimed())
                continue;

            if (currentIndex < startIndex)
            {
                currentIndex++;
                continue;
            }
            if (currentIndex >= endIndex)
                break;

            Item itemTemplate = ItemTable.getInstance().getTemplate(mail.getItemId());
            String itemName = (itemTemplate != null) ? itemTemplate.getName() : "Unknown Item";

            if (itemName.length() > MAX_ITEM_NAME_LENGTH)
                itemName = itemName.substring(0, MAX_ITEM_NAME_LENGTH - 2) + "..";

            sb.append("<tr>");
            sb.append("<td>");
            sb.append("Item: <font color=00FFCC><u>").append(itemName).append("</u></font>");
            if (mail.getEnchantLevel() > 0)
                sb.append(" <font color=LEVEL>+").append(mail.getEnchantLevel()).append("</font>");
            sb.append(" <font color=FFD700>x").append(mail.getItemCount()).append("</font>");
            if (mail.isReturned())
                sb.append(" <font color=FF6666>(Returned)</font>");
            sb.append("</td>");

            sb.append("<td align=center>");
            sb.append("<a action=\"bypass mailbox_receive ").append(mail.getId()).append("\">");
            sb.append("<font color=00FF00><u>Receive</u></font>");
            sb.append("</a>");
            sb.append("</td>");
            sb.append("</tr>");

            currentIndex++;
        }

        sb.append("</table>");

        // Paginação
        sb.append("<br><table width=300 cellspacing=0 cellpadding=3><tr>");

        if (page > 1) {
            sb.append("<td align=right width=140>");
            sb.append("<a action=\"bypass mailbox_page ").append(page - 1).append("\">");
            sb.append("<font color=FFFF00><u>Previous</u></font></a></td>");
        } else {
            sb.append("<td align=right width=140><font color=AAAAAA>Previous</font></td>");
        }

        sb.append("<td align=center width=20> | </td>");

        if (page < maxPage) {
            sb.append("<td align=left width=140>");
            sb.append("<a action=\"bypass mailbox_page ").append(page + 1).append("\">");
            sb.append("<font color=FFFF00><u>Next</u></font></a></td>");
        } else {
            sb.append("<td align=left width=140><font color=AAAAAA>Next</font></td>");
        }

        sb.append("</tr><tr><td colspan=3 align=center>Page ").append(page).append(" of ").append(maxPage).append("</td></tr></table>");

        sb.append("<br><br><center>");
        sb.append("<table bgcolor=000000 width=90 height=21 cellspacing=0 cellpadding=0><tr><td align=center>");
        sb.append("<a action=\"bypass -h mailbox_receive_all\"><font color=00FF00><b>Receive All</b></font></a>");
        sb.append("</td></tr></table>");
        sb.append("</center>");



        sb.append("</center></body></html>");
        html.setHtml(sb.toString());
        player.sendPacket(html);
    }






    @Override
    public String[] getVoicedCommandList()
    {
        return _voicedCommands;
    }
}
