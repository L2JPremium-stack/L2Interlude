package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ConnectionPool;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.item.kind.Weapon;
import net.sf.l2j.gameserver.model.item.type.CrystalType;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class VoicedRankingCustom implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS = new String[] { "weapons", "jewels", "jboss" };
	
	int pos;
	
	private static void showRankingHtml(Player activeChar)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/mods/menu/Ranking.htm");
		activeChar.sendPacket(html);
	}
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target) 
	{
		if (command.equals("ranking"))
			showRankingHtml(activeChar); 
//		if (command.equals("weapons")) 
//		{
//		    NpcHtmlMessage htm = new NpcHtmlMessage(5);
//		    StringBuilder tb = new StringBuilder("<html><head><title>Weapons Ranking</title></head><body><center><img src=\"L2UI.SquareBlank\" width=300 height=0><br1><center><table width=315 bgcolor=000000><tr><td></td></tr><tr><td><center><font color=LEVEL>Ranking Weapons Enchants</font></center></td></tr><tr><td></td></tr></table><br1><center><br></center><br1><center><table width=95%><tr><td><center><font color=LEVEL>[ Player's Name ]</font></center></td><td><center><font color=LEVEL>[ Enchanted Weapon ]</font></center></td></tr>");
//
//		    try (Connection con = ConnectionPool.getConnection()) 
//		    {
//		        // Alterando a consulta para limitar os resultados a 15
//		        PreparedStatement statement = con.prepareStatement(
//		            "SELECT i.item_id, i.enchant_level, c.char_name " +
//		            "FROM items i " +
//		            "LEFT JOIN characters c ON c.obj_Id = i.owner_id " +
//		            "WHERE c.accesslevel = 0 " +
//		            "ORDER BY i.enchant_level DESC " +
//		            "LIMIT 15"  // Limite para 15 resultados
//		        );
//		        ResultSet result = statement.executeQuery();
//		        
//		        boolean hasWeapons = false; // Flag para verificar se encontramos armas
//
//		        while (result.next())
//		        {
//		            String item_id = result.getString("item_id");
//		            String item_enchant = result.getString("enchant_level");
//		            String char_name = result.getString("char_name");
//
//		            // Verifica se o item é uma arma
//		            Item item = ItemTable.getInstance().getTemplate(Integer.parseInt(item_id));
//		            if (item != null && item instanceof Weapon)
//		            {
//		                String name = item.getName();
//
//		                // Limita o nome do item, se for muito longo, adiciona "..."
//		                int maxNameLength = 28;  // Defina o comprimento máximo que você deseja para o nome
//		                if (name.length() > maxNameLength) {
//		                    name = name.substring(0, maxNameLength) + "...";  // Adiciona "..." ao final
//		                }
//
//		                // Verifica se a arma tem o tipo de cristal correto
//		                boolean isValid = (item.getCrystalType() == CrystalType.S || item.getCrystalType() == CrystalType.A || item.getCrystalType() == CrystalType.B);
//		                
//		                if (isValid)
//		                {
//		                    // Marca que encontramos ao menos uma arma válida
//		                    hasWeapons = true;
//
//		                    // Adiciona o item à lista
//		                    tb.append("<tr><td><center><font color=\"AAAAAA\">" + char_name + "</font></center></td>");
//		                    tb.append("<td><center>" + name + "<font color=LEVEL> +" + item_enchant + "</font></center></td></tr>");
//		                }
//		            }
//		        } 
//
//		        // Se não encontramos nenhuma arma válida, adicionar uma mensagem de erro
//		        if (!hasWeapons) {
//		            tb.append("<tr><td colspan=\"2\"><center><font color=\"FF0000\">No valid weapons found.</font></center></td></tr>");
//		        }
//
//		        statement.close();
//		        result.close();
//		    } catch (Exception exception) {
//		        exception.printStackTrace();  // Para debugar se necessário
//		    }
//
//		    tb.append("</table><br>");
//		    tb.append("<center><a action=\"bypass -h voiced_ranking\"><font color=\"LEVEL\">Back to Ranking</font></a></center>");
//		    tb.append("</body></html>");
//		    htm.setHtml(tb.toString());
//		    activeChar.sendPacket(htm);
//		}
		// Evento "weapons"
//		if (command.equals("weapons")) {
//		    NpcHtmlMessage htm = new NpcHtmlMessage(5);
//		    StringBuilder tb = new StringBuilder("<html><head><title>Weapons Ranking</title></head><body><center>");
//		    tb.append("<img src=\"L2UI.SquareBlank\" width=300 height=0><br1>");
//		    tb.append("<table width=315 bgcolor=000000><tr><td></td></tr><tr><td><center><font color=LEVEL>Ranking Weapons Enchants</font></center></td></tr><tr><td></td></tr></table><br1><center><br></center><br1><center><table width=95%><tr><td><center><font color=LEVEL>[ Player's Name ]</font></center></td><td><center><font color=LEVEL>[ Enchanted Weapon ]</font></center></td></tr>");
//
//		    try (Connection con = ConnectionPool.getConnection()) {
//		        // Consulta SQL sem limite, para pegar todas as armas encantadas com nível >= 4
//		        PreparedStatement statement = con.prepareStatement(
//		            "SELECT i.owner_id, i.item_id, i.enchant_level, c.char_name " +
//		            "FROM items i " +
//		            "LEFT JOIN characters c ON c.obj_Id = i.owner_id " +
//		            "WHERE c.accesslevel = 0 " +
//		            "AND i.enchant_level >= 4 " +  // Apenas armas com encantamento >= 4
//		            "ORDER BY i.enchant_level DESC"  // Ordena pelo nível de encantamento
//		        );
//		        ResultSet result = statement.executeQuery();
//
//		        int count = 0;  // Contador para limitar a exibição a 15 armas
//
//		        // Controla a exibição
//		        while (result.next() && count < 15) {  // Limita para exibir apenas as 15 primeiras armas
//		            String item_id = result.getString("item_id");
//		            String item_enchant = result.getString("enchant_level");
//		            String char_name = result.getString("char_name");
//
//		            // Verifica se o item é uma arma
//		            Item item = ItemTable.getInstance().getTemplate(Integer.parseInt(item_id));
//		            if (item != null && item instanceof Weapon) {
//		                // Limita o nome do item se for muito longo
//		                String name = item.getName();
//		                int maxNameLength = 28;  // Limite de 28 caracteres para o nome
//		                if (name.length() > maxNameLength) {
//		                    name = name.substring(0, maxNameLength) + "...";  // Adiciona "..." ao final
//		                }
//
//		                // Verifica se o item tem o tipo de cristal correto
//		                boolean isValid = (item.getCrystalType() == CrystalType.S || item.getCrystalType() == CrystalType.A || item.getCrystalType() == CrystalType.B);
//
//		                if (isValid) {
//		                    // Adiciona o item à lista
//		                    tb.append("<tr><td><center><font color=\"AAAAAA\">" + char_name + "</font></center></td>");
//		                    tb.append("<td><center>" + name + "<font color=LEVEL> +" + item_enchant + "</font></center></td></tr>");
//		                    count++;  // Incrementa o contador
//		                }
//		            }
//		        }
//
//		        result.close();
//		        statement.close();
//		    } catch (Exception exception) {
//		        exception.printStackTrace();  // Para debugar se necessário
//		    }
//
//		    tb.append("</table><br>");
//		    tb.append("<center><a action=\"bypass -h voiced_ranking\"><font color=\"LEVEL\">Back to Ranking</font></a></center>");
//		    tb.append("</body></html>");
//		    htm.setHtml(tb.toString());
//		    activeChar.sendPacket(htm);
//		}
		//COM TABELA 
//		if (command.equals("weapons")) {
//		    NpcHtmlMessage htm = new NpcHtmlMessage(5);
//		    StringBuilder tb = new StringBuilder();
//
//		    tb.append("<html><body><center>");
//		    tb.append("<font color=\"LEVEL\">Top 15 - Enchanted Weapons</font><br><br>");
//
//		    // Definindo bordas para a tabela e ajustando o tamanho para evitar que fique muito grande
//		    tb.append("<table width=110% border=1 cellspacing=0 cellpadding=0>");
//
//		    // Cabeçalho da tabela
//		    tb.append("<tr>");
//		    tb.append("<td width=10%><font color=\"FFFF00\"><b>#</b></font></td>");
//		    tb.append("<td width=60%><font color=\"FFFF00\"><b>Player Name</b></font></td>");  // Player agora ocupa 60% da tabela
//		    tb.append("<td width=30%><font color=\"FFFF00\"><b>Weapon Enchant</b></font></td>");  // Weapon ocupa 30% da tabela
//		    tb.append("</tr>");
//
//		    try (Connection con = ConnectionPool.getConnection()) {
//		        PreparedStatement statement = con.prepareStatement(
//		            "SELECT i.owner_id, i.item_id, i.enchant_level, c.char_name " +
//		            "FROM items i " +
//		            "LEFT JOIN characters c ON c.obj_Id = i.owner_id " +
//		            "WHERE c.accesslevel = 0 AND i.enchant_level >= 4 " +
//		            "ORDER BY i.enchant_level DESC"
//		        );
//
//		        ResultSet result = statement.executeQuery();
//		        int count = 0;
//
//		        while (result.next() && count < 15) {  // Agora mostra até 15 jogadores
//		            int itemId = result.getInt("item_id");
//		            int enchant = result.getInt("enchant_level");
//		            String player = result.getString("char_name");
//
//		            Item item = ItemTable.getInstance().getTemplate(itemId);
//		            if (item != null && item instanceof Weapon) {
//		                boolean isValid = (item.getCrystalType() == CrystalType.S || item.getCrystalType() == CrystalType.A || item.getCrystalType() == CrystalType.B);
//		                if (!isValid) continue;
//
//		                String name = item.getName();
//		                if (name.length() > 26) {
//		                    name = name.substring(0, 26) + "..."; // Truncar o nome se for muito longo
//		                }
//
//		                // Adicionando as linhas na tabela
//		                tb.append("<tr>");
//		                
//		                // Cor da posição (#): todas as posições serão verdes
//		                tb.append("<td><font color=\"00FF00\"><b>" + (count + 1) + ".</b></font></td>");
//		                
//		                // Ajustando a largura da coluna Player para garantir que 16 caracteres caibam corretamente
//		                tb.append("<td style=\"white-space: nowrap; max-width: 400px; overflow: hidden; text-overflow: ellipsis; color: lightblue;\"><font color=\"00BFFF\">" + player + "</font></td>");
//		                
//		                // Cor da arma: todas as armas terão uma cor cinza claro (neutral)
//		                tb.append("<td><font color=\"A9A9A9\">" + name + " +<font color=\"LEVEL\">" + enchant + "</font></font></td>");
//		                tb.append("</tr>");
//		                count++;
//		            }
//		        }
//
//		        result.close();
//		        statement.close();
//		    } catch (Exception e) {
//		        e.printStackTrace();
//		    }
//
//		    tb.append("</table>");
//		    tb.append("<br><a action=\"bypass -h voiced_ranking\">Back to Ranking</a>");
//		    tb.append("</center></body></html>");
//
//		    htm.setHtml(tb.toString());
//		    activeChar.sendPacket(htm);
//		}
		if (command.equals("weapons")) {
		    NpcHtmlMessage htm = new NpcHtmlMessage(5);
		    StringBuilder tb = new StringBuilder();

		    tb.append("<html><body><center>");
		    tb.append("<font color=\"LEVEL\">Top 15 - Enchanted Weapons</font><br><br>");
		    tb.append("<table width=110% border=1 cellspacing=0 cellpadding=0>");

		    tb.append("<tr>");
		    tb.append("<td width=10%><font color=\"FFFF00\"><b>#</b></font></td>");
		    tb.append("<td width=60%><font color=\"FFFF00\"><b>Player Name</b></font></td>");
		    tb.append("<td width=30%><font color=\"FFFF00\"><b>Weapon Enchant</b></font></td>");
		    tb.append("</tr>");

		    try (Connection con = ConnectionPool.getConnection();
		         PreparedStatement statement = con.prepareStatement(
		             "SELECT i.owner_id, i.item_id, i.enchant_level, c.char_name " +
		             "FROM items i " +
		             "LEFT JOIN characters c ON c.obj_Id = i.owner_id " +
		             "WHERE c.accesslevel = 0 AND i.enchant_level >= 4 " +
		             "ORDER BY i.enchant_level DESC");
		         ResultSet result = statement.executeQuery()) {

		        int count = 0;

		        while (result.next() && count < 15) {
		            int itemId = result.getInt("item_id");
		            int enchant = result.getInt("enchant_level");
		            String player = result.getString("char_name");

		            Item item = ItemTable.getInstance().getTemplate(itemId);
		            if (item != null && item instanceof Weapon) {
		                boolean isValid = (item.getCrystalType() == CrystalType.S ||
		                                   item.getCrystalType() == CrystalType.A ||
		                                   item.getCrystalType() == CrystalType.B);
		                if (!isValid)
		                    continue;

		                String name = item.getName();
		                if (name.length() > 26) {
		                    name = name.substring(0, 26) + "...";
		                }

		                tb.append("<tr>");
		                tb.append("<td><font color=\"00FF00\"><b>" + (count + 1) + ".</b></font></td>");
		                tb.append("<td style=\"white-space: nowrap; max-width: 400px; overflow: hidden; text-overflow: ellipsis; color: lightblue;\"><font color=\"00BFFF\">" + player + "</font></td>");
		                tb.append("<td><font color=\"A9A9A9\">" + name + " +<font color=\"LEVEL\">" + enchant + "</font></font></td>");
		                tb.append("</tr>");

		                count++;
		            }
		        }
		    } catch (Exception e) {
		        e.printStackTrace();
		    }

		    tb.append("</table>");
		    tb.append("<br><a action=\"bypass -h voiced_ranking\">Back to Ranking</a>");
		    tb.append("</center></body></html>");

		    htm.setHtml(tb.toString());
		    activeChar.sendPacket(htm);
		}



//		if (command.equals("jewels"))
//		{
//		    NpcHtmlMessage htm = new NpcHtmlMessage(5);
//		    StringBuilder tb = new StringBuilder("<html><head><title>Jewels No Custom A/S Ranking</title></head><center><img src=\"L2UI.SquareBlank\" width=300 height=0><br1><body><table width=315 bgcolor=000000><tr><td></td></tr><tr><td><center><font color=LEVEL>Ranking Jewels No Custom A/S Enchants</font></center></td></tr><tr><td></td></tr></table><br1><center><br></center><br1><center><table width=95%><tr><td><center><font color=LEVEL>[ Player's Name ]</font></center></td><td><center><font color=LEVEL>[ Enchanted Jewels ]</font></center></td></tr>");
//		    try (Connection con = ConnectionPool.getConnection()) 
//		    {
//		        PreparedStatement statement = con.prepareStatement(
//		            "SELECT owner_id,item_id,enchant_level,char_name FROM items " +
//		            "LEFT JOIN characters AS c ON (c.obj_Id=owner_id) " +
//		            "WHERE item_id IN (924,862,893,920,858,889) and c.accesslevel=0 " +
//		            "ORDER BY enchant_level DESC LIMIT 15"
//		        );
//		        ResultSet result = statement.executeQuery();
//		        
//		        while (result.next()) {
//		            String owner_id = result.getString("owner_id");
//		            String item_id = result.getString("item_id");
//		            String item_enchant = result.getString("enchant_level");
//		            Item item = ItemTable.getInstance().getTemplate(Integer.parseInt(item_id)); // 'item' nunca será null
//		            String name = item.getName();
//		            
//		            // Garantir que o 'PreparedStatement' e 'ResultSet' do segundo query sejam fechados
//		            try (PreparedStatement charname = con.prepareStatement("SELECT char_name FROM characters WHERE obj_Id=?")) {
//		                charname.setString(1, owner_id);  // Usar setString para evitar SQL Injection
//		                ResultSet result2 = charname.executeQuery();
//		                
//		                while (result2.next()) {
//		                    String char_name = result2.getString("char_name");
//		                    
//		                    if (item instanceof Armor && char_name.equals(activeChar.getName()) && (item.getCrystalType() == CrystalType.S || item.getCrystalType() == CrystalType.A)) {
//		                        tb.append("<tr><td><center><font color =\"AAAAAA\">" + char_name + "</center></td><td><center>" + name + "</font><font color=LEVEL> +" + item_enchant + "</font></center></td></tr>");
//		                        continue;
//		                    }
//		                    if (item instanceof Armor && (item.getCrystalType() == CrystalType.S || item.getCrystalType() == CrystalType.A)) {
//		                        tb.append("<tr><td><center><font color=AAAAAA>" + char_name + "</center></td><td><center>" + name + "</font><font color=LEVEL> +" + item_enchant + "</font></center></td></tr>");
//		                    }
//		                }
//		                result2.close();  // Fechar o ResultSet explicitamente
//		            }
//		        }
//		        statement.close();
//		        result.close();
//		    } catch (Exception exception) {
//		        exception.printStackTrace();
//		    }
//		    tb.append("</table><br>");
//		    tb.append("<center><a action=\"bypass -h voiced_ranking\"><font color=\"LEVEL\">Back to Ranking</font></a></center>");
//		    tb.append("</body></html>");
//		    htm.setHtml(tb.toString());
//		    activeChar.sendPacket(htm);
//		}
//		if (command.equals("jewels")) {
//		    NpcHtmlMessage htm = new NpcHtmlMessage(5);
//		    StringBuilder tb = new StringBuilder("<html><head><title>Jewels No Custom A/S Ranking</title></head><center><img src=\"L2UI.SquareBlank\" width=300 height=0><br1><body><table width=315 bgcolor=000000><tr><td></td></tr><tr><td><center><font color=LEVEL>Ranking Jewels No Custom A/S Enchants</font></center></td></tr><tr><td></td></tr></table><br1><center><br></center><br1><center><table width=95%><tr><td><center><font color=LEVEL>[ Player's Name ]</font></center></td><td><center><font color=LEVEL>[ Enchanted Jewels ]</font></center></td></tr>");
//		    
//		    try (Connection con = ConnectionPool.getConnection()) {
//		        // Consulta para pegar todas as joias (sem limite na busca)
//		        PreparedStatement statement = con.prepareStatement(
//		            "SELECT owner_id, item_id, enchant_level, char_name " +
//		            "FROM items " +
//		            "LEFT JOIN characters AS c ON (c.obj_Id = owner_id) " +
//		            "WHERE item_id IN (924, 862, 893, 920, 858, 889) AND c.accesslevel = 0 " +
//		            "ORDER BY enchant_level DESC"
//		        );
//		        ResultSet result = statement.executeQuery();
//		        
//		        int count = 0;  // Contador para limitar a exibição a 15 resultados
//
//		        while (result.next() && count < 15) {  // Limitar a exibição a 15 resultados
//		            String owner_id = result.getString("owner_id");
//		            String item_id = result.getString("item_id");
//		            String item_enchant = result.getString("enchant_level");
//
//		            // Se o encantamento for +0, ignora o item e vai para o próximo
//		            if (Integer.parseInt(item_enchant) == 0) {
//		                continue;  // Pula para o próximo item
//		            }
//
//		            Item item = ItemTable.getInstance().getTemplate(Integer.parseInt(item_id)); // 'item' nunca será null
//		            String name = item.getName();
//
//		            // Verifica o nome do jogador
//		            try (PreparedStatement charname = con.prepareStatement("SELECT char_name FROM characters WHERE obj_Id = ?")) {
//		                charname.setString(1, owner_id);  // Usar setString para evitar SQL Injection
//		                ResultSet result2 = charname.executeQuery();
//
//		                while (result2.next()) {
//		                    String char_name = result2.getString("char_name");
//
//		                    // Adiciona o item à tabela, somente itens com encantamento maior que 0
//		                    tb.append("<tr><td><center><font color=\"AAAAAA\">" + char_name + "</font></center></td>");
//		                    tb.append("<td><center>" + name + "<font color=LEVEL> +" + item_enchant + "</font></center></td></tr>");
//		                    count++;  // Incrementa o contador
//		                }
//		                result2.close();  // Fechar o ResultSet explicitamente
//		            }
//		        }
//		        statement.close();
//		        result.close();
//		    } catch (Exception exception) {
//		        exception.printStackTrace();  // Para debugar se necessário
//		    }
//
//		    tb.append("</table><br>");
//		    tb.append("<center><a action=\"bypass -h voiced_ranking\"><font color=\"LEVEL\">Back to Ranking</font></a></center>");
//		    tb.append("</body></html>");
//		    htm.setHtml(tb.toString());
//		    activeChar.sendPacket(htm);
//		}
//		if (command.equals("jewels")) {
//		    NpcHtmlMessage htm = new NpcHtmlMessage(5);
//		    StringBuilder tb = new StringBuilder();
//
//		    tb.append("<html><body><center>");
//		    tb.append("<font color=\"LEVEL\">Top 15 - Enchanted Jewels</font><br><br>");
//
//		    // Definindo bordas para a tabela e ajustando o tamanho para evitar que fique muito grande
//		    tb.append("<table width=110% border=1 cellspacing=0 cellpadding=0>");
//
//		    // Cabeçalho da tabela
//		    tb.append("<tr>");
//		    tb.append("<td width=10%><font color=\"FFFF00\"><b>#</b></font></td>");
//		    tb.append("<td width=60%><font color=\"FFFF00\"><b>Player Name</b></font></td>");  // Player agora ocupa 60% da tabela
//		    tb.append("<td width=30%><font color=\"FFFF00\"><b>Jewels Enchant</b></font></td>");  // Jewels ocupa 30% da tabela
//		    tb.append("</tr>");
//
//		    try (Connection con = ConnectionPool.getConnection()) {
//		        PreparedStatement statement = con.prepareStatement(
//		            "SELECT i.owner_id, i.item_id, i.enchant_level, c.char_name " +
//		            "FROM items i " +
//		            "LEFT JOIN characters c ON c.obj_Id = i.owner_id " +
//		            "WHERE c.accesslevel = 0 AND i.item_id IN (924, 862, 893, 920, 858, 889) AND i.enchant_level > 0 " +
//		            "ORDER BY i.enchant_level DESC"
//		        );
//
//		        ResultSet result = statement.executeQuery();
//		        int count = 0;
//
//		        while (result.next() && count < 15) {  // Agora mostra até 15 jogadores
//		            int itemId = result.getInt("item_id");
//		            int enchant = result.getInt("enchant_level");
//		            String player = result.getString("char_name");
//
//		            Item item = ItemTable.getInstance().getTemplate(itemId);
//		            if (item != null) {
//		                String name = item.getName();
//		                if (name.length() > 26) {
//		                    name = name.substring(0, 26) + "..."; // Truncar o nome se for muito longo
//		                }
//
//		                // Adicionando as linhas na tabela
//		                tb.append("<tr>");
//		                
//		                // Cor da posição (#): todas as posições serão verdes
//		                tb.append("<td><font color=\"00FF00\"><b>" + (count + 1) + ".</b></font></td>");
//		                
//		                // Ajustando a largura da coluna Player para garantir que 16 caracteres caibam corretamente
//		                tb.append("<td style=\"white-space: nowrap; max-width: 400px; overflow: hidden; text-overflow: ellipsis; color: lightblue;\"><font color=\"00BFFF\">" + player + "</font></td>");
//		                
//		                // Cor da joia: todas as joias terão uma cor cinza claro (neutral)
//		                tb.append("<td><font color=\"A9A9A9\">" + name + " +<font color=\"LEVEL\">" + enchant + "</font></font></td>");
//		                tb.append("</tr>");
//		                count++;
//		            }
//		        }
//
//		        result.close();
//		        statement.close();
//		    } catch (Exception e) {
//		        e.printStackTrace();
//		    }
//
//		    tb.append("</table>");
//		    tb.append("<br><a action=\"bypass -h voiced_ranking\">Back to Ranking</a>");
//		    tb.append("</center></body></html>");
//
//		    htm.setHtml(tb.toString());
//		    activeChar.sendPacket(htm);
//		}
		if (command.equals("jewels")) {
		    NpcHtmlMessage htm = new NpcHtmlMessage(5);
		    StringBuilder tb = new StringBuilder();

		    tb.append("<html><body><center>");
		    tb.append("<font color=\"LEVEL\">Top 15 - Enchanted Jewels</font><br><br>");

		    // Definindo bordas para a tabela e ajustando o tamanho para evitar que fique muito grande
		    tb.append("<table width=110% border=1 cellspacing=0 cellpadding=0>");

		    // Cabeçalho da tabela
		    tb.append("<tr>");
		    tb.append("<td width=10%><font color=\"FFFF00\"><b>#</b></font></td>");
		    tb.append("<td width=60%><font color=\"FFFF00\"><b>Player Name</b></font></td>");  // Player agora ocupa 60% da tabela
		    tb.append("<td width=30%><font color=\"FFFF00\"><b>Jewels Enchant</b></font></td>");  // Jewels ocupa 30% da tabela
		    tb.append("</tr>");

		    try (Connection con = ConnectionPool.getConnection();
		         PreparedStatement statement = con.prepareStatement(
		             "SELECT i.owner_id, i.item_id, i.enchant_level, c.char_name " +
		             "FROM items i " +
		             "LEFT JOIN characters c ON c.obj_Id = i.owner_id " +
		             "WHERE c.accesslevel = 0 AND i.item_id IN (924, 862, 893, 920, 858, 889) AND i.enchant_level > 0 " +
		             "ORDER BY i.enchant_level DESC");
		         ResultSet result = statement.executeQuery()) {

		        int count = 0;

		        while (result.next() && count < 15) {  // Agora mostra até 15 jogadores
		            int itemId = result.getInt("item_id");
		            int enchant = result.getInt("enchant_level");
		            String player = result.getString("char_name");

		            Item item = ItemTable.getInstance().getTemplate(itemId);
		            if (item != null) {
		                String name = item.getName();
		                if (name.length() > 26) {
		                    name = name.substring(0, 26) + "..."; // Truncar o nome se for muito longo
		                }

		                // Adicionando as linhas na tabela
		                tb.append("<tr>");

		                // Cor da posição (#): todas as posições serão verdes
		                tb.append("<td><font color=\"00FF00\"><b>" + (count + 1) + ".</b></font></td>");

		                // Ajustando a largura da coluna Player para garantir que 16 caracteres caibam corretamente
		                tb.append("<td style=\"white-space: nowrap; max-width: 400px; overflow: hidden; text-overflow: ellipsis; color: lightblue;\"><font color=\"00BFFF\">" + player + "</font></td>");

		                // Cor da joia: todas as joias terão uma cor cinza claro (neutral)
		                tb.append("<td><font color=\"A9A9A9\">" + name + " +<font color=\"LEVEL\">" + enchant + "</font></font></td>");
		                tb.append("</tr>");
		                count++;
		            }
		        }
		    } catch (Exception e) {
		        e.printStackTrace();
		    }

		    tb.append("</table>");
		    tb.append("<br><a action=\"bypass -h voiced_ranking\">Back to Ranking</a>");
		    tb.append("</center></body></html>");

		    htm.setHtml(tb.toString());
		    activeChar.sendPacket(htm);
		}



//		if (command.equals("jboss")) {
//		    NpcHtmlMessage htm = new NpcHtmlMessage(5);
//		    StringBuilder tb = new StringBuilder("<html><head><title>Jewels Boss Ranking</title></head><center><img src=\"L2UI.SquareBlank\" width=300 height=0><br1><body><table width=315 bgcolor=000000><tr><td></td></tr><tr><td><center><font color=LEVEL>Ranking Jewels Boss Enchants</font></center></td></tr><tr><td></td></tr></table><br1><center><br></center><br1><center><table width=95%><tr><td><center><font color=LEVEL>[ Player's Name ]</font></center></td><td><center><font color=LEVEL>[ Enchanted Jewels ]</font></center></td></tr>");
//
//		    try (Connection con = ConnectionPool.getConnection()) {
//		        // Usar PreparedStatement com parâmetros para evitar SQL Injection
//		        String query = "SELECT owner_id, item_id, enchant_level, char_name " +
//		                       "FROM items LEFT JOIN characters AS c ON (c.obj_Id = owner_id) " +
//		                       "WHERE item_id IN (?) AND c.accesslevel = 0 " +
//		                       "ORDER BY enchant_level DESC LIMIT 15";
//		        try (PreparedStatement statement = con.prepareStatement(query)) {
//		            statement.setString(1, Config.ID_LIST_JEWELES_BOSS_RANKING); // Evita SQL Injection
//
//		            try (ResultSet result = statement.executeQuery()) {
//		                while (result.next()) {
//		                    String owner_id = result.getString("owner_id");
//		                    String item_id = result.getString("item_id");
//		                    String item_enchant = result.getString("enchant_level");
//		                    Item item = ItemTable.getInstance().getTemplate(Integer.parseInt(item_id));
//		                    String name = item.getName();
//
//		                    // Limita o comprimento do nome do item
//		                    int maxItemNameLength = 20;
//		                    if (name.length() > maxItemNameLength) {
//		                        name = name.substring(0, maxItemNameLength) + "...";  // Truncando o nome do item
//		                    }
//
//		                    // Consulta para pegar o nome do personagem
//		                    try (PreparedStatement charname = con.prepareStatement("SELECT char_name FROM characters WHERE obj_Id = ?")) {
//		                        charname.setString(1, owner_id); // Usar setString para evitar SQL Injection
//
//		                        try (ResultSet result2 = charname.executeQuery()) {
//		                            while (result2.next()) {
//		                                String char_name = result2.getString("char_name");
//
//		                                if (item instanceof Armor) {
//		                                    if (char_name.equals(activeChar.getName())) {
//		                                        tb.append("<tr><td><center><font color =\"AAAAAA\">" + char_name + "</center></td><td><center>" + name + "<font color=LEVEL> +" + item_enchant + "</font></center></td></tr>");
//		                                        continue;
//		                                    }
//		                                    tb.append("<tr><td><center><font color=AAAAAA>" + char_name + "</center></td><td><center>" + name + "<font color=LEVEL> +" + item_enchant + "</font></center></td></tr>");
//		                                }
//		                            }
//		                        }
//		                    }
//		                }
//		            }
//		        }
//		    } catch (Exception exception) {
//		        exception.printStackTrace();
//		    }
//
//		    tb.append("</table><br>");
//		    tb.append("<center><a action=\"bypass -h voiced_ranking\"><font color=\"LEVEL\">Back to Ranking</font></a></center>");
//		    tb.append("</body></html>");
//		    htm.setHtml(tb.toString());
//		    activeChar.sendPacket(htm);
//		}
//		if (command.equals("jboss")) {
//		    NpcHtmlMessage htm = new NpcHtmlMessage(5);
//		    StringBuilder tb = new StringBuilder("<html><head><title>Jewels Boss Ranking</title></head><center><img src=\"L2UI.SquareBlank\" width=300 height=0><br1><body><table width=315 bgcolor=000000><tr><td></td></tr><tr><td><center><font color=LEVEL>Ranking Jewels Boss Enchants</font></center></td></tr><tr><td></td></tr></table><br1><center><br></center><br1><center><table width=95%><tr><td><center><font color=LEVEL>[ Player's Name ]</font></center></td><td><center><font color=LEVEL>[ Enchanted Jewels ]</font></center></td></tr>");
//
//		    // Acessando a lista de itens da configuração (assumindo que seja uma String com IDs separados por vírgula)
//		    String itemIdsStr = Config.ID_LIST_JEWELES_BOSS_RANKING;
//		    // Divida a string em um array de Strings e depois converta para uma lista de Inteiros
//		    List<Integer> itemIds = Arrays.stream(itemIdsStr.split(","))
//		                                  .map(String::trim)  // Remover espaços extras
//		                                  .map(Integer::parseInt)  // Converter para Inteiro
//		                                  .collect(Collectors.toList());  // Coletar em uma lista
//
//		    try (Connection con = ConnectionPool.getConnection()) {
//		        // Cria a consulta SQL com os IDs de itens da configuração
//		        String query = "SELECT owner_id, item_id, enchant_level, char_name " +
//		                       "FROM items " +
//		                       "LEFT JOIN characters AS c ON (c.obj_Id = owner_id) " +
//		                       "WHERE item_id IN (" + itemIds.stream().map(String::valueOf).collect(Collectors.joining(", ")) + ") " +
//		                       "AND c.accesslevel = 0 " +
//		                       "ORDER BY enchant_level DESC";
//
//		        try (PreparedStatement statement = con.prepareStatement(query)) {
//		            ResultSet result = statement.executeQuery();
//
//		            int count = 0;  // Contador para limitar a exibição a 15 resultados
//
//		            while (result.next() && count < 15) {  // Limitar a exibição a 15 resultados
//		                String owner_id = result.getString("owner_id");
//		                String item_id = result.getString("item_id");
//		                String item_enchant = result.getString("enchant_level");
//
//		                Item item = ItemTable.getInstance().getTemplate(Integer.parseInt(item_id)); // 'item' nunca será null
//		                String name = item.getName();
//
//		                // Limitar o comprimento do nome do item
//		                int maxItemNameLength = 20;  // Máximo de 20 caracteres no nome do item
//		                if (name.length() > maxItemNameLength) {
//		                    name = name.substring(0, maxItemNameLength) + "...";  // Truncando o nome do item
//		                }
//
//		                // Verifica se o encantamento é +0 e ignora itens +0
//		                if (Integer.parseInt(item_enchant) == 0) {
//		                    continue;  // Pula itens com encantamento +0
//		                }
//
//		                // Consulta para pegar o nome do personagem
//		                try (PreparedStatement charname = con.prepareStatement("SELECT char_name FROM characters WHERE obj_Id = ?")) {
//		                    charname.setString(1, owner_id);  // Usar setString para evitar SQL Injection
//
//		                    try (ResultSet result2 = charname.executeQuery()) {
//		                        while (result2.next()) {
//		                            String char_name = result2.getString("char_name");
//
//		                            // Adiciona o item à tabela do ranking
//		                            tb.append("<tr><td><center><font color=\"AAAAAA\">" + char_name + "</font></center></td>");
//		                            tb.append("<td><center>" + name + "<font color=LEVEL> +" + item_enchant + "</font></center></td></tr>");
//		                            count++;  // Incrementa o contador
//		                        }
//		                    }
//		                }
//		            }
//		            statement.close();
//		            result.close();
//		        }
//		    } catch (Exception exception) {
//		        exception.printStackTrace();  // Para debugar se necessário
//		    }
//
//		    tb.append("</table><br>");
//		    tb.append("<center><a action=\"bypass -h voiced_ranking\"><font color=\"LEVEL\">Back to Ranking</font></a></center>");
//		    tb.append("</body></html>");
//		    htm.setHtml(tb.toString());
//		    activeChar.sendPacket(htm);
//		}
//		if (command.equals("jboss")) {
//		    NpcHtmlMessage htm = new NpcHtmlMessage(5);
//		    StringBuilder tb = new StringBuilder();
//
//		    tb.append("<html><body><center>");
//		    tb.append("<font color=\"LEVEL\">Top 15 - Jewels Boss Enchants</font><br><br>");
//
//		    // Definindo bordas para a tabela e ajustando o tamanho para evitar que fique muito grande
//		    tb.append("<table width=110% border=1 cellspacing=0 cellpadding=0>");
//
//		    // Cabeçalho da tabela
//		    tb.append("<tr>");
//		    tb.append("<td width=10%><font color=\"FFFF00\"><b>#</b></font></td>");
//		    tb.append("<td width=60%><font color=\"FFFF00\"><b>Player Name</b></font></td>");  // Player agora ocupa 60% da tabela
//		    tb.append("<td width=30%><font color=\"FFFF00\"><b>Jewels Enchant</b></font></td>");  // Jewels ocupa 30% da tabela
//		    tb.append("</tr>");
//
//		    // Acessando a lista de itens da configuração (assumindo que seja uma String com IDs separados por vírgula)
//		    String itemIdsStr = Config.ID_LIST_JEWELES_BOSS_RANKING;
//		    List<Integer> itemIds = Arrays.stream(itemIdsStr.split(","))
//		                                  .map(String::trim)  // Remover espaços extras
//		                                  .map(Integer::parseInt)  // Converter para Inteiro
//		                                  .collect(Collectors.toList());  // Coletar em uma lista
//
//		    try (Connection con = ConnectionPool.getConnection()) {
//		        // Cria a consulta SQL com os IDs de itens da configuração
//		        String query = "SELECT i.owner_id, i.item_id, i.enchant_level, c.char_name " +
//		                       "FROM items i " +
//		                       "LEFT JOIN characters c ON c.obj_Id = i.owner_id " +
//		                       "WHERE i.item_id IN (" + itemIds.stream().map(String::valueOf).collect(Collectors.joining(", ")) + ") " +
//		                       "AND c.accesslevel = 0 " +
//		                       "ORDER BY i.enchant_level DESC";
//
//		        try (PreparedStatement statement = con.prepareStatement(query)) {
//		            ResultSet result = statement.executeQuery();
//
//		            int count = 0;  // Contador para limitar a exibição a 15 resultados
//
//		            while (result.next() && count < 15) {  // Limitar a exibição a 15 resultados
//		                String owner_id = result.getString("owner_id");
//		                String item_id = result.getString("item_id");
//		                String item_enchant = result.getString("enchant_level");
//
//		                Item item = ItemTable.getInstance().getTemplate(Integer.parseInt(item_id)); // 'item' nunca será null
//		                String name = item.getName();
//
//		                // Limitar o comprimento do nome do item
//		                int maxItemNameLength = 20;  // Máximo de 20 caracteres no nome do item
//		                if (name.length() > maxItemNameLength) {
//		                    name = name.substring(0, maxItemNameLength) + "...";  // Truncando o nome do item
//		                }
//
//		                // Verifica se o encantamento é +0 e ignora itens +0
//		                if (Integer.parseInt(item_enchant) == 0) {
//		                    continue;  // Pula itens com encantamento +0
//		                }
//
//		                // Consulta para pegar o nome do personagem
//		                try (PreparedStatement charname = con.prepareStatement("SELECT char_name FROM characters WHERE obj_Id = ?")) {
//		                    charname.setString(1, owner_id);  // Usar setString para evitar SQL Injection
//
//		                    try (ResultSet result2 = charname.executeQuery()) {
//		                        while (result2.next()) {
//		                            String char_name = result2.getString("char_name");
//
//		                            // Adiciona o item à tabela do ranking
//		                            tb.append("<tr>");
//		                            
//		                            // Cor da posição (#): todas as posições serão verdes
//		                            tb.append("<td><font color=\"00FF00\"><b>" + (count + 1) + ".</b></font></td>");
//		                            
//		                            // Ajustando a largura da coluna Player para garantir que 16 caracteres caibam corretamente
//		                            tb.append("<td style=\"white-space: nowrap; max-width: 400px; overflow: hidden; text-overflow: ellipsis; color: lightblue;\"><font color=\"00BFFF\">" + char_name + "</font></td>");
//		                            
//		                            // Cor da joia: todas as joias terão uma cor cinza claro (neutral)
//		                            tb.append("<td><font color=\"A9A9A9\">" + name + " +<font color=\"LEVEL\">" + item_enchant + "</font></font></td>");
//		                            tb.append("</tr>");
//		                            count++;  // Incrementa o contador
//		                        }
//		                    }
//		                }
//		            }
//		            statement.close();
//		            result.close();
//		        }
//		    } catch (Exception exception) {
//		        exception.printStackTrace();  // Para debugar se necessário
//		    }
//
//		    tb.append("</table>");
//		    tb.append("<br><a action=\"bypass -h voiced_ranking\">Back to Ranking</a>");
//		    tb.append("</center></body></html>");
//
//		    htm.setHtml(tb.toString());
//		    activeChar.sendPacket(htm);
//		}
		if (command.equals("jboss")) {
		    NpcHtmlMessage htm = new NpcHtmlMessage(5);
		    StringBuilder tb = new StringBuilder();

		    tb.append("<html><body><center>");
		    tb.append("<font color=\"LEVEL\">Top 15 - Jewels Boss Enchants</font><br><br>");
		    tb.append("<table width=110% border=1 cellspacing=0 cellpadding=0>");
		    tb.append("<tr>");
		    tb.append("<td width=10%><font color=\"FFFF00\"><b>#</b></font></td>");
		    tb.append("<td width=60%><font color=\"FFFF00\"><b>Player Name</b></font></td>");
		    tb.append("<td width=30%><font color=\"FFFF00\"><b>Jewels Enchant</b></font></td>");
		    tb.append("</tr>");

		    String itemIdsStr = Config.ID_LIST_JEWELES_BOSS_RANKING;
		    List<Integer> itemIds = Arrays.stream(itemIdsStr.split(","))
		                                  .map(String::trim)
		                                  .map(Integer::parseInt)
		                                  .collect(Collectors.toList());

		    String joinedIds = itemIds.stream()
		                              .map(String::valueOf)
		                              .collect(Collectors.joining(", "));

		    String query = "SELECT i.owner_id, i.item_id, i.enchant_level, c.char_name " +
		                   "FROM items i " +
		                   "LEFT JOIN characters c ON c.obj_Id = i.owner_id " +
		                   "WHERE i.item_id IN (" + joinedIds + ") " +
		                   "AND c.accesslevel = 0 " +
		                   "ORDER BY i.enchant_level DESC";

		    try (Connection con = ConnectionPool.getConnection();
		         PreparedStatement statement = con.prepareStatement(query);
		         ResultSet result = statement.executeQuery()) {

		        int count = 0;

		        while (result.next() && count < 15) {
		            int itemId = result.getInt("item_id");
		            int enchant = result.getInt("enchant_level");
		            String charName = result.getString("char_name");

		            if (enchant == 0 || charName == null) {
		                continue;  // Ignora itens +0 e jogadores nulos
		            }

		            Item item = ItemTable.getInstance().getTemplate(itemId);
		            if (item == null) continue;

		            String name = item.getName();
		            int maxItemNameLength = 20;
		            if (name.length() > maxItemNameLength) {
		                name = name.substring(0, maxItemNameLength) + "...";
		            }

		            tb.append("<tr>");
		            tb.append("<td><font color=\"00FF00\"><b>" + (count + 1) + ".</b></font></td>");
		            tb.append("<td style=\"white-space: nowrap; max-width: 400px; overflow: hidden; text-overflow: ellipsis; color: lightblue;\"><font color=\"00BFFF\">" + charName + "</font></td>");
		            tb.append("<td><font color=\"A9A9A9\">" + name + " +<font color=\"LEVEL\">" + enchant + "</font></font></td>");
		            tb.append("</tr>");

		            count++;
		        }
		    } catch (Exception e) {
		        e.printStackTrace();
		    }

		    tb.append("</table>");
		    tb.append("<br><a action=\"bypass -h voiced_ranking\">Back to Ranking</a>");
		    tb.append("</center></body></html>");

		    htm.setHtml(tb.toString());
		    activeChar.sendPacket(htm);
		}



		return true;
	}
	
	@Override
	public String[] getVoicedCommandList() 
	{
		return VOICED_COMMANDS;
	}
}
