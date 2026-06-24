package net.sf.l2j.gameserver.datatables.xml;

import java.io.File;
import java.text.DecimalFormat;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.datatables.ArmorSetsTable;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.instancemanager.custom.Roulette;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.xmlfactory.XMLDocumentFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class RouletteData 
{
    private final TreeMap<Integer, Roulette> _rewards = new TreeMap<>();
    private static final DecimalFormat PERCENT = new DecimalFormat("#.#");
    private static Logger _log = Logger.getLogger(ArmorSetsTable.class.getName());

    protected RouletteData()
    {
        load();
    }

    public void load()
    {
    	
    	try
		{
			File f = new File("./data/xml/custom/Roulette.xml");
			Document doc = XMLDocumentFactory.getInstance().loadDocument(f);
			
			Node n = doc.getFirstChild();
			for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
			{
				if (d.getNodeName().equalsIgnoreCase("reward"))
				{
					NamedNodeMap attrs = d.getAttributes();
					
					int id = Integer.valueOf(attrs.getNamedItem("id").getNodeValue());
					int itemId = Integer.valueOf(attrs.getNamedItem("itemId").getNodeValue());
					int enchant = Integer.valueOf(attrs.getNamedItem("enchant").getNodeValue());
					int count = Integer.valueOf(attrs.getNamedItem("count").getNodeValue());
					int chance = Integer.valueOf(attrs.getNamedItem("chance").getNodeValue());
					_rewards.put(id, new Roulette(itemId, enchant, count, chance));
				}
			}
			_log.log(Level.WARNING, "roulette: loaded "+_rewards.size()+" rewards");
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Roulette: Error loading from database: " + e.getMessage(), e);
		}
    }

    public void reload() 
    {
        _rewards.clear();
        load();
    }

    /**
     * @return the _rewards
     */
    public TreeMap<Integer, Roulette> get_rewards() 
    {
        return _rewards;
    }

    public Roulette getReward(int id)
    {
        return _rewards.get(id);
    }

    public void sendList(Player player, NpcHtmlMessage html, int currentPage, int itemsPerPage)
    {
        // Load static htm.
        html.setFile("data/html/mods/Roleta/items/ROULETTE_LIST.htm");
        final StringBuilder sb = new StringBuilder();

        int totalItems = get_rewards().size();

        // Verificar se currentPage está dentro dos limites
        if (currentPage < 1)
        {
            currentPage = 1;
        }

        int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);

        if (currentPage > totalPages)
        {
            currentPage = totalPages;
        }

        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, totalItems);
        int row = 0;

        // Generate data for the current page.
        for (int i = startIndex; i < endIndex; i++) 
        {
            Roulette roulette = get_rewards().values().toArray(new Roulette[0])[i];
            final double chance = roulette.get_chance();
            String color = (chance > 80.) ? "90EE90" : (chance > 5.) ? "BDB76B" : "F08080";
            final String percent = PERCENT.format(chance);
            final int amount = roulette.get_count();
            final Item item = ItemTable.getInstance().getTemplate(roulette.get_itemId());

            String name = item.getName();
            if (name.startsWith("Recipe: "))
            {
                name = "R: " + name.substring(8);
            }

            sb.append(((row % 2) == 0 ? "<table width=256 bgcolor=000000><tr>" : "<table width=256><tr>"));
            sb.append("<td width=44 height=41 align=center><table bgcolor=" + "FFFFFF" + " cellpadding=6 cellspacing=\"-5\"><tr><td><button width=32 height=32 back=" + IconTable.getIcon(roulette.get_itemId()) + " fore=" + IconTable.getIcon(roulette.get_itemId()) + "></td></tr></table></td>");
            sb.append("<td width=246>&nbsp;" + (roulette.get_enchant() > 0 ? "+" + roulette.get_enchant() + " " : "") + name + "<br1>");
            sb.append("<table width=240><tr><td width=80><font color=B09878>Rate:</font> <font color=" + color + ">" + percent + "%</font></td><td width=160><font color=B09878>Amount: </font>" + amount + "</td></tr></table>");
            sb.append("</td></tr></table><img src=L2UI.SquareGray width=256 height=1>");

            row++;
        }

        // Add pagination controls
        sb.append("<br1><tr><td align=\"center\">Page " + currentPage + " of " + totalPages + "</td>");
        sb.append("<td align=\"center\">");

        if (currentPage > 1) 
        {
            sb.append("<a action=\"bypass  Roleta_item_roulette list " + (currentPage - 1) + "\">Previous</a> ");
        }

        if (currentPage < totalPages)
        {
            sb.append("<a action=\"bypass  Roleta_item_roulette list " + (currentPage + 1) + "\">Next</a>");
        }

        sb.append("</td></tr>");

        html.replace("%content%", sb.toString());
        player.sendPacket(html);
    }

    public static RouletteData getInstance() 
    {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder
    {
        protected static final RouletteData INSTANCE = new RouletteData();
    }
}