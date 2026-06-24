package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.datatables.xml.IconTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.item.DropData;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class AdminBuscarDrop implements IAdminCommandHandler
{
    @SuppressWarnings("unused")
	private static final int ITEMS_PER_PAGE = 5; // Número de itens por página
    private static final int NPCS_PER_ITEM = 5; // Número de NPCs por item
    private static final String[] ADMIN_COMMANDS = { "admin_buscar" };

    @Override
    public boolean useAdminCommand(String command, Player activeChar)
    {
        final NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("data/html/admin/Buscar.htm");

        if (command.equals("admin_buscar"))
        {
            html.replace("%list%", "<center><br><br><br>Set first a keyword</center>");
        }
        else if (command.startsWith("admin_buscar"))
        {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();

            int page = Integer.valueOf(st.nextToken());
            if (st.hasMoreTokens())
            {
                String buscar = command.substring(14 + String.valueOf(page).length());
                StringBuilder list = getList(activeChar, page, buscar);
                html.replace("%list%", list == null ? "" : list.toString());
            }
            else
            {
                html.replace("%list%", "<center><br><br><br>Set first a keyword</center>");
            }
        }
        activeChar.sendPacket(html);
        return true;
    }

    public StringBuilder getList(Player activeChar, int page, String buscar)
    {
        // Listas para armazenar NPCs e itens em diferentes categorias
        List<String> merchantEntries = new ArrayList<>();
        List<String> monsterEntries = new ArrayList<>();
        List<String> itemEntries = new ArrayList<>();

        // Obtém NPCs do tipo Merchant
        List<NpcTemplate> merchants = NpcTable.getInstance().getAllNpcs().stream()
                .filter(npc -> npc.getType().equals("Merchant") && matches(npc.getName(), buscar))
                .collect(Collectors.toList());

        // Processar Merchants
        for (NpcTemplate npc : merchants)
        {
            String colorNpcId = "<font color=FF0000>" + npc.getNpcId() + "</font>"; // Cor para o ID do NPC

            // Adicionar informações sobre os Merchants
            List<L2Spawn> npcSpawns = SpawnTable.getInstance().getSpawnTable().stream()
                    .filter(spawn -> spawn.getNpcId() == npc.getNpcId())
                    .collect(Collectors.toList());

            for (L2Spawn spawn : npcSpawns)
            {
                String location = "<font color=FFFFFF>Location: [" + spawn.getLocX() + ", " + spawn.getLocY() + ", " + spawn.getLocZ() + "]</font>";
                merchantEntries.add(
                        "<table width=280 bgcolor=000000><tr>" +
                        "<td width=40><img src=\"" + IconTable.getIcon(npc.getNpcId()) + "\" width=32 height=32></td>" + // Imagem do ícone
                        "<td>" +
                        "NPC Name: " + npc.getName() + "<br>" +
                        "NPC ID: " + colorNpcId + "<br>" +
                        location + "<br><br>" +
                        "</td>" +
                        "</tr></table>"
                );
            }
        }

        // Obtém os monstros que têm drops e que correspondem ao critério de pesquisa
        List<NpcTemplate> monsters = NpcTable.getInstance().getAllNpcs().stream()
                .filter(npc -> !npc.getType().equals("Merchant") && npc.hasDrops() && matches(npc.getName(), buscar))
                .collect(Collectors.toList());

        // Processar monstros
        for (NpcTemplate npc : monsters)
        {
            String colorNpcId = "<font color=FF0000>" + npc.getNpcId() + "</font>"; // Cor para o ID do NPC

            // Adicionar informações sobre os monstros
            List<L2Spawn> npcSpawns = SpawnTable.getInstance().getSpawnTable().stream()
                    .filter(spawn -> spawn.getNpcId() == npc.getNpcId())
                    .collect(Collectors.toList());

            for (L2Spawn spawn : npcSpawns)
            {
                String location = "<font color=FFFFFF>Location: [" + spawn.getLocX() + ", " + spawn.getLocY() + ", " + spawn.getLocZ() + "]</font>";
                monsterEntries.add(
                        "<table width=280 bgcolor=000000><tr>" +
                        "<td width=40><img src=\"" + IconTable.getIcon(npc.getNpcId()) + "\" width=32 height=32></td>" + // Imagem do ícone
                        "<td>" +
                        "NPC Name: " + npc.getName() + "<br>" +
                        "NPC ID: " + colorNpcId + "<br>" +
                        location + "<br><br>" +
                        "</td>" +
                        "</tr></table>"
                );
            }
        }

        // Lista de itens que correspondem à pesquisa
        List<Item> allItems = Arrays.asList(ItemTable.getInstance().getTemplates()).stream()
                .filter(item -> item != null && matches(item.getName(), buscar))
                .filter(this::isItemInDrops) // Apenas itens que estão nos drops de NPCs
                .collect(Collectors.toList());

        // Processar itens
        for (Item item : allItems)
        {
            String itemName = item.getName();
            if (itemName.length() >= 43)
            {
                itemName = itemName.substring(0, 40) + "...";
            }

            // Obtém os NPCs que dropam o item
            List<NpcTemplate> itemNpcs = NpcTable.getInstance().getAllNpcs().stream()
                    .filter(npc -> npc.hasDrops() && npc.getAllDropData().stream()
                            .anyMatch(drop -> drop.getItemId() == item.getItemId()))
                    .collect(Collectors.toList());

            for (NpcTemplate npc : itemNpcs)
            {
                for (DropData drop : npc.getAllDropData())
                {
                    if (drop.getItemId() == item.getItemId())
                    {
                        // Obtém o caminho do ícone
                        String icon = IconTable.getIcon(item.getItemId());

                        // Aplicar cores nas informações
                        String colorName = "<font color=00FF00>" + itemName + "</font>"; // Verde
                        String colorItemId = "<font color=FFFF00>" + item.getItemId() + "</font>"; // Amarelo
                        String colorNpcId = "<font color=FF0000>" + npc.getNpcId() + "</font>"; // Vermelho

                        // Colorir Min Drop e Max Drop
                        String colorMinDrop = "<font color=FF6347>" + drop.getMinDrop() + "</font>"; // Azul Claro
                        String colorMaxDrop = "<font color=FF6347>" + drop.getMaxDrop() + "</font>"; // Tomate

                        // Adicionar as informações do item
                        itemEntries.add(
                                "<table width=280 bgcolor=000000><tr>" +
                                "<td width=40><img src=\"" + icon + "\" width=32 height=32></td>" + // Imagem do ícone
                                "<td>" +
                                "Name: " + colorName + "<br>" +
                                "ItemId: " + colorItemId + " NPC ID: " + colorNpcId + "<br>" +
                                "Min Drop: " + colorMinDrop + " Max Drop: " + colorMaxDrop + "<br><br>" +
                                "</td>" +
                                "</tr></table>"
                        );
                    }
                }
            }
        }

        // Combine as entradas, com Merchant first, depois monstros, e itens por último
        List<String> combinedEntries = new ArrayList<>();
        combinedEntries.addAll(merchantEntries);  // Primeiro Merchants
        combinedEntries.addAll(monsterEntries);   // Depois monstros
        combinedEntries.addAll(itemEntries);      // Finalmente itens

        if (combinedEntries.isEmpty())
        {
            return new StringBuilder("<center><br><br><br>No matching NPCs or items found for: <font color=LEVEL>" + buscar + "</font></center>");
        }

        // Paginação da lista combinada de informações (NPCs e itens)
        final int totalPages = Math.max(1, (int) Math.ceil((double) combinedEntries.size() / NPCS_PER_ITEM));
        int fromIndex = (page - 1) * NPCS_PER_ITEM;
        int toIndex = Math.min(page * NPCS_PER_ITEM, combinedEntries.size());

        if (fromIndex >= combinedEntries.size())
        {
            return new StringBuilder("<center><br><br><br>No more entries to display.</center>");
        }

        // Constrói a página atual
        List<String> currentEntries = combinedEntries.subList(fromIndex, toIndex);
        final StringBuilder sb = new StringBuilder();

        for (String entry : currentEntries)
        {
            sb.append(entry);
        }

        // Adiciona navegação de páginas
        sb.append("<img height=2><img src=L2UI.SquareGray width=280 height=1><table width=280 bgcolor=000000><tr>");
        sb.append("<td align=right width=70>" + (page > 1 ? "<button value=\"< PREV\" action=\"bypass admin_buscar " + (page - 1) + " " + buscar + "\" width=65 height=19 back=L2UI_ch3.smallbutton2_over fore=L2UI_ch3.smallbutton2>" : "") + "</td>");
        sb.append("<td align=center width=100>Page " + page + " / " + totalPages + "</td>");
        sb.append("<td align=left width=70>" + (page < totalPages ? "<button value=\"NEXT >\" action=\"bypass admin_buscar " + (page + 1) + " " + buscar + "\" width=65 height=19 back=L2UI_ch3.smallbutton2_over fore=L2UI_ch3.smallbutton2>" : "") + "</td>");
        sb.append("</tr></table><img src=L2UI.SquareGray width=280 height=1>");

        return sb;
    }





    class NpcItemEntry
    {
        private final NpcTemplate npc;
        private final Item item;
        private final DropData drop;
        private final int totalDropAmount;

        public NpcItemEntry(NpcTemplate npc, Item item, DropData drop, int totalDropAmount)
        {
            this.npc = npc;
            this.item = item;
            this.drop = drop;
            this.totalDropAmount = totalDropAmount;
        }

        public int getTotalDropAmount()
        {
            return totalDropAmount;
        }

        public String toHtml()
        {
            // Obtém o caminho do ícone
            String icon = IconTable.getIcon(item.getItemId());

            // Aplicar cores nas informações
            String colorName = "<font color=00FF00>" + item.getName() + "</font>"; // Verde
            String colorItemId = "<font color=FFFF00>" + item.getItemId() + "</font>"; // Amarelo
            String colorNpcId = "<font color=FF0000>" + npc.getNpcId() + "</font>"; // Vermelho
            String colorMinDrop = "<font color=FF6347>" + drop.getMinDrop() + "</font>"; // Azul Claro
            String colorMaxDrop = "<font color=FF6347>" + drop.getMaxDrop() + "</font>"; // Tomate

            return "<table width=280 bgcolor=000000><tr>" +
                    "<td width=40><img src=\"" + icon + "\" width=32 height=32></td>" + // Imagem do ícone
                    "<td>" +
                    "Name: " + colorName + "<br>" +
                    "ItemId: " + colorItemId + " NPC ID: " + colorNpcId + "<br>" +
                    "Min Drop: " + colorMinDrop + " Max Drop: " + colorMaxDrop + "<br>" +
                    "Total Drop Amount: " + totalDropAmount + "<br><br>" +
                    "</td>" +
                    "</tr></table>";
        }
    }


    // Método para obter a localização do NPC a partir do SpawnTable
    @SuppressWarnings({
		"null",
		"unused"
	})
	private static String getNpcCoordinates(NpcTemplate npc)
    {
        // Verifica se a lista de spawns é nula ou vazia
		List<L2Spawn> spawns = new ArrayList<>(SpawnTable.getInstance().getSpawnTable());
        if (spawns == null || spawns.isEmpty())
        {
            return "No active spawns available";
        }

        // Itera através de todos os spawns no SpawnTable
        for (L2Spawn spawn : spawns)
        {
            // Verifica se o npcId do spawn corresponde ao npcId do NPC
            if (spawn.getNpcId() == npc.getNpcId())
            {
                // Obter coordenadas do spawn
                int npcX = spawn.getLocX();
                int npcY = spawn.getLocY();
                int npcZ = spawn.getLocZ();

                return "X: " + npcX + " Y: " + npcY + " Z: " + npcZ;
            }
        }

        // Caso o NPC não seja encontrado
        return "Location not found for NPC ID: " + npc.getNpcId();
    }

    private boolean isItemInDrops(Item item)
    {
        return NpcTable.getInstance().getAllNpcs().stream()
                .anyMatch(npc -> npc.hasDrops() && npc.getAllDropData().stream()
                        .anyMatch(drop -> drop.getItemId() == item.getItemId()));
    }

    public static boolean matches(String name, String buscar)
    {
        return Arrays.stream(buscar.toLowerCase().split(" "))
                .allMatch(result -> name.toLowerCase().contains(result));
    }

    @Override
    public String[] getAdminCommandList()
    {
        return ADMIN_COMMANDS;
    }
}
