package net.sf.l2j.sellbuff;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ConnectionPool;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.templates.skills.L2SkillType;

public class SellBuffMsg
{
	
	private int _lastPage = 0;
	private int _subPage = 0;
	
	public void setLastPage(int page)
	{
		_lastPage = page;
	}
	
	public int getLastPage()
	{
		return _lastPage;
	}
	
	public void setSubPage(int subPage)
	{
		_subPage = subPage;
	}
	
	public int getSubPage()
	{
		return _subPage;
	}
	
	/**
	 * Construct response to buff buyer.
	 * @param seller
	 * @param buyer
	 * @return
	 */
	@SuppressWarnings("static-access")
	private StringBuilder buyerResponse(Player seller, Player buyer)
	{
		StringBuilder tb = new StringBuilder();
		int buffsPerPage = 7; // Máximo de 8 buffs por página
		int buffsCount = 0; // Contagem de todos os buffs do jogador
		int sellBuffsCount = 0; // Contagem de buffs na página atual
		@SuppressWarnings("unused")
		String buffFor = " player";
		
		if (_lastPage == 1 || _lastPage == 2)
		{
			if (_lastPage == 2)
			{
				buffFor = " pet";
			}
			tb.append("<html><title>" + seller.getName() + "(" + seller.getLevel() + ") buff price: " + seller.getBuffPrice() + " " + ItemTable.getInstance().getTemplate(Config.ITEM_BUFF_SHOP).getName() + " </title><body><br>");
			
			// Obtendo todas as skills do vendedor
			ArrayList<L2Skill> ba = this.getBuffList(seller);
			
			// Iterando por todas as skills do vendedor
			for (L2Skill p : ba)
			{
				// Verificando se a skill está na lista de bloqueio
				if (Config.BLOCK_SKILL_LIST.contains(p.getId()))
				{
					continue; // Se a skill estiver bloqueada, pula ela
				}
				
				buffsCount++; // Contabilizando as habilidades
				
				if (buffsCount > buffsPerPage * _subPage && buffsCount <= buffsPerPage * (_subPage + 1))
				{ // Mostrar subpágina com buffs
					// Verificando o tipo de target da skill e se o comprador pode utilizá-la
					if (!Config.ALLOW_PARTY_BUFFS && p.getTargetType() == L2Skill.SkillTargetType.TARGET_PARTY)
					{
						if (buyer.getParty() != null && seller.getParty() != null && buyer.getParty().equals(seller.getParty()))
						{
							sellBuffsCount++;
							createHtmlSkillField(tb, _lastPage, p.getName() + " - Level (" + p.getLevel() + ")", p.getId(), buffsCount);
						}
					}
					else if (!Config.ALLOW_CLAN_BUFFS && (p.getTargetType() == L2Skill.SkillTargetType.TARGET_CLAN || p.getTargetType() == L2Skill.SkillTargetType.TARGET_ALLY))
					{
						if ((buyer.getClan() != null && seller.getClan() != null && buyer.getClanId() == seller.getClanId()) || (buyer.getClan() != null && seller.getClan() != null && buyer.getClan().getAllyName() != null && seller.getClan().getAllyName() != null && buyer.getClan().getAllyName().equals(seller.getClan().getAllyName())))
						{
							sellBuffsCount++;
							createHtmlSkillField(tb, _lastPage, p.getName() + " - Level (" + p.getLevel() + ")", p.getId(), buffsCount);
						}
					}
					else
					{
						sellBuffsCount++;
						createHtmlSkillField(tb, _lastPage, p.getName() + " - Level (" + p.getLevel() + ")", p.getId(), buffsCount);
					}
				}
			}
			
			// Preencher espaços vazios com buffs extras
			if (sellBuffsCount > 0)
			{
				for (int x = (buffsCount - (_subPage * buffsPerPage)); x < buffsPerPage; x++)
				{
					this.createHtmlEmptySkillField(tb);
				}
			}
			else
			{
				tb.append("<center><table><tr><td width=270 align=center height=" + 36 * buffsPerPage + "><font color=FF0000>I am sorry. I have no buffs for you now.</font></td></tr></table></center>");
			}
			
			tb.append("<br>");
			tb.append("<center><table><tr>");
			
			// Calcular o número total de páginas
			int totalPages = (int) Math.ceil((double) buffsCount / buffsPerPage);
			
			// Exibir botões de página dinamicamente
			for (int i = 0; i < totalPages; i++)
			{
				subPageButton(tb, i); // Exibe os botões de navegação de páginas
			}
			
			tb.append("</tr></table></center>");
			
			// Agora o botão "Back" será exibido ao final
			tb.append("<br>");
			tb.append("<table width=\"100%\"><tr>"); // A tabela vai ocupar 100% da largura
			tb.append("<td>"); // A célula não tem alinhamento, usando o comportamento padrão
			tb.append("<button value=\"Back\" action=\"bypass sellbuffpage0\" width=100 height=18 back=\"L2UI_ch3.bigbutton_down\" fore=\"L2UI_ch3.bigbutton\">");
			tb.append("</td></tr></table>");
			
			tb.append("</body></html>");
		}
		
		else
		{
			tb.append("<html><body><center>");
			// tb.append("<img src=\"l2font-e.replay_logo-e\" width=256 height=60><br>");
			tb.append("<img src=\"" + Config.SERVER_LOGO_SELLBUFF + "\" width=256 height=60><br>");
			tb.append("<font color=\"FF3500\"><h3>" + seller.getName() + " (" + seller.getLevel() + ") - Price for : </h3></font> " + seller.getBuffPrice() + " " + ItemTable.getInstance().getTemplate(Config.ITEM_BUFF_SHOP).getName() + "");
			
			tb.append("<br><font color=\"00FF00\">Choose buffs for:</font><br>");
			tb.append("<table width=\"300\" cellpadding=\"5\"><tr>");
			tb.append("<td><center><button value=\"Player\" action=\"bypass sellbuffpage1\" width=100 height=23 back=\"L2UI_ch3.bigbutton_down\" fore=\"L2UI_ch3.bigbutton\"></center></td>");
			tb.append("<td><center><button value=\"Pet\" action=\"bypass sellbuffpage2\" width=100 height=23 back=\"L2UI_ch3.bigbutton_down\" fore=\"L2UI_ch3.bigbutton\"></center></td>");
			tb.append("</tr></table>");
			
			tb.append("<br>");
			
			if (seller.getBuffPrice() >= Config.SELL_BUFF_PUNISHED_PRICE)
			{
				tb.append("<br><font color=\"FF0000\"><b>Warning!</b> Buff price is very high!</font>");
			}
			
			tb.append("</center></body></html>");
			
		}
		
		return tb;
		
	}
	
	/**
	 * Send html response to buyer.
	 * @param seller
	 * @param buyer
	 * @param error - if set true, return first page and send msg to buyer with error type.
	 */
	public void sendBuyerResponse(Player seller, Player buyer, boolean error)
	{
		
		if (seller != null)
		{
			if (seller.isSellBuff() && seller != buyer)
			{
				StringBuilder tb = null;
				if (!error)
				{
					tb = buyer.getSellBuffMsg().buyerResponse(seller, buyer); // buyer == this from l2pcinstance
				}
				else
				{
					this.setLastPage(0);
					this.setSubPage(0);
					tb = buyer.getSellBuffMsg().buyerResponse(seller, buyer);
				}
				NpcHtmlMessage n = new NpcHtmlMessage(0);
				n.setHtml(tb.toString());
				buyer.sendPacket(n);
			}
		}
	}
	
	public void sendSellerResponse(Player seller)
	{
		StringBuilder tb = new StringBuilder(0);
		
		// Criar venda de buffs
		if (!seller.isSellBuff())
		{
			tb.append("<html><body><center>");
			tb.append("<font color=\"FF3500\"><h3>Welcome!</h3></font>"); // Laranja
			tb.append("<br><font color=\"00FFFF\">Set your price to start selling buffs.</font>"); // Aqua
			tb.append("<br><font color=\"FFFF00\">Players will target you for buff services.</font>"); // Amarelo
			tb.append("<br><font color=\"00FF00\">You will earn " + ItemTable.getInstance().getTemplate(Config.ITEM_BUFF_SHOP).getName() + " for each buff.</font>"); // Verde
			tb.append("<br><font color=\"FFFFFF\"><center>Enter your price:</center></font><br>"); // Branco
			tb.append("<center><edit var=\"pri\" width=120 height=15></center><br><br>");
			tb.append("<center><button value=\"Confirm\" action=\"bypass -h actr $pri\" width=100 height=23 back=\"L2UI_ch3.bigbutton_down\" fore=\"L2UI_ch3.bigbutton\"></center>");
			tb.append("</center></body></html>");
			// Abortar venda
		}
		else
		{
			tb.append("<html><body><center>");
			tb.append("<font color=\"FFFF00\">Current buff price: " + seller.getBuffPrice() + " " + ItemTable.getInstance().getTemplate(Config.ITEM_BUFF_SHOP).getName() + "</font>"); // Amarelo
			tb.append("<br><font color=\"FF0000\">Do you want to cancel selling buffs?</font>"); // Vermelho
			tb.append("<br><center><button value=\"Abort\" action=\"bypass -h pctra\" width=100 height=23 back=\"L2UI_ch3.bigbutton_down\" fore=\"L2UI_ch3.bigbutton\"></center>");
			tb.append("</center></body></html>");
			seller.broadcastUserInfo();
		}
		
		NpcHtmlMessage n = new NpcHtmlMessage(0);
		n.setHtml(tb.toString());
		seller.sendPacket(n);
	}
	
	@SuppressWarnings("static-access")
	private void createHtmlSkillField(StringBuilder tb, int buffPage, String skillName, int skillId, int buffRowNumber)
	{
		// buffPage means buff for player = 1, or buff for pet = 2.
		String buffType = "";
		if (buffPage == 2)
		{
			buffType = "pet";
		}
		
		// Alternando entre cores de fundo
		String col1 = "";
		String col2 = "LEVEL"; // Título da coluna
		if (buffRowNumber % 2 == 0)
		{
			col1 = " bgcolor=000000"; // Cor de fundo
			col2 = "FFFFFF"; // Cor do texto
		}
		
		// Ajustes para buffs de summoners (habilidades específicas)
		int desc_skillId = skillId;
		if (skillId == 4699 || skillId == 4700)
		{
			skillId = 1331;
		}
		if (skillId == 4702 || skillId == 4703)
		{
			skillId = 1332;
		}
		
		// Preparando o ID da skill
		String tmp_skillId = "" + skillId;
		if (skillId < 1000)
		{
			tmp_skillId = "0" + skillId;
		}
		
		// Construindo o código HTML
		tb.append("<table" + col1 + ">");
		tb.append("<tr>");
		tb.append("<td width=40>");
		
		// Botão para aplicar o buff
		tb.append("<button action=\"bypass  " + buffType + "buff" + desc_skillId + "\" width=32 height=32 back=\"icon.skill" + tmp_skillId + "\" fore=\"icon.skill" + tmp_skillId + "\">");
		
		tb.append("</td>");
		tb.append("<td width=240>");
		tb.append("<table>");
		tb.append("<tr><td width=240 align=left><font color=\"" + col2 + "\">" + skillName + "</font></td></tr>");
		tb.append("<tr><td width=240 align=left><font color=\"ae9977\">" + this.getSkillDescription(desc_skillId) + "</font></td></tr>");
		tb.append("</table>");
		tb.append("</td>");
		tb.append("</tr>");
		tb.append("</table>");
	}
	
	private void subPageButton(StringBuilder tb, int page)
	{
		int btnValue = page + 1;
		tb.append("<td width=34>");
		if (_subPage == page)
		{
			tb.append("<button value=\"" + btnValue + "\" action=\"bypass  sellbuffpage" + _lastPage + "sub" + page + "\" width=32 height=23 back=\"L2UI_ch3.bigbutton_dow\" fore=\"L2UI_ch3.bigbutto\">"); // wrong button bg for black bg.
		}
		else
		{
			tb.append("<button value=\"" + btnValue + "\" action=\"bypass  sellbuffpage" + _lastPage + "sub" + page + "\" width=32 height=23 back=\"\" fore=\"\">");
		}
		tb.append("</td>");
	}
	
	private static void createHtmlEmptySkillField(StringBuilder tb)
	{
		tb.append("<table>");
		tb.append("<tr>");
		tb.append("<td height=36></td>");
		tb.append("</tr>");
		tb.append("</table>");
	}
	
	private static String getSkillDescription(int skillId)
	{
		String desc = "&nbsp;";
		
		try (Connection con = ConnectionPool.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT buffId, description FROM sellbuff_describe WHERE buffId = ?"))
		{
			
			statement.setInt(1, skillId);
			
			try (ResultSet rs = statement.executeQuery())
			{
				if (rs.next())
				{
					desc = rs.getString("description");
				}
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return desc;
	}
	
	public ArrayList<L2Skill> getBuffList(Player seller)
	{
		Map<Integer, L2Skill> skills = seller.getSkills();
		ArrayList<L2Skill> ba = new ArrayList<>();
		
		int classId = seller.getClassId().getId();
		
		for (L2Skill s : skills.values())
		{
			if (s == null)
				continue;
			
			boolean isBuffSkill = s.getSkillType() == L2SkillType.BUFF || s.getSkillType() == L2SkillType.HEAL_PERCENT || s.getSkillType() == L2SkillType.COMBATPOINTHEAL;
			
			boolean isExcludedClass = (classId == 96 || classId == 14 || classId == 104 || classId == 28);
			
			if (!isExcludedClass && s.isActive() && isBuffSkill)
			{
				if (Config.SELL_BUFF_FILTER_ENABLED)
				{
					// Dominator e Overlord
					if (classId == 115 || classId == 51)
					{
						if (s.getId() != 1002 && s.getId() != 1006 && s.getId() != 1007 && s.getId() != 1009)
						{
							ba.add(s);
						}
					}
					// Doomcryer e Warcryer
					else if (classId == 116 || classId == 52)
					{
						if (s.getId() != 1003 && s.getId() != 1005)
						{
							ba.add(s);
						}
					}
					// Cardinal e Bishop
					else if (classId == 97 || classId == 16)
					{
						if (s.getId() == 1353 || s.getId() == 1307 || s.getId() == 1311)
						{
							ba.add(s);
						}
					}
					else
					{
						ba.add(s);
					}
				}
				else
				{
					ba.add(s);
				}
			}
			else
			{
				// Tratamento especial para skills 1331 e 1332
				if (s.getId() == 1332)
				{
					L2Skill skill1 = SkillTable.getInstance().getInfo(4702, getSkillLevel(seller, 1332));
					L2Skill skill2 = SkillTable.getInstance().getInfo(4703, getSkillLevel(seller, 1332));
					ba.add(skill1);
					ba.add(skill2);
				}
				else if (s.getId() == 1331)
				{
					L2Skill skill1 = SkillTable.getInstance().getInfo(4699, getSkillLevel(seller, 1331));
					L2Skill skill2 = SkillTable.getInstance().getInfo(4700, getSkillLevel(seller, 1331));
					ba.add(skill1);
					ba.add(skill2);
				}
			}
		}
		return ba;
	}
	
	public ArrayList<L2Skill> getBuffsetList(Player seller, ArrayList<String> buffset)
	{
		Map<Integer, L2Skill> skills = seller.getSkills();
		ArrayList<L2Skill> ba = new ArrayList<>();
		
		int classId = seller.getClassId().getId();
		
		for (L2Skill s : skills.values())
		{
			if (s == null)
				continue;
			
			String skillIdStr = Integer.toString(s.getId());
			if (!buffset.contains(skillIdStr))
				continue;
			
			boolean isBuffSkill = s.getSkillType() == L2SkillType.BUFF || s.getSkillType() == L2SkillType.HEAL_PERCENT || s.getSkillType() == L2SkillType.COMBATPOINTHEAL;
			
			boolean isExcludedClass = classId == 96 || classId == 14 || classId == 104 || classId == 28;
			
			if (!isExcludedClass && s.isActive() && isBuffSkill)
			{
				if (Config.SELL_BUFF_FILTER_ENABLED)
				{
					if (classId == 115 || classId == 51)
					{ // Dominator e Overlord
						if (s.getId() != 1002 && s.getId() != 1006 && s.getId() != 1007 && s.getId() != 1009)
						{
							ba.add(s);
						}
					}
					else if (classId == 116 || classId == 52)
					{ // Doomcryer e Warcryer
						if (s.getId() != 1003 && s.getId() != 1005)
						{
							ba.add(s);
						}
					}
					else if (classId == 97 || classId == 16)
					{ // Cardinal e Bishop
						if (s.getId() == 1353 || s.getId() == 1307 || s.getId() == 1311)
						{
							ba.add(s);
						}
					}
					else
					{
						ba.add(s);
					}
				}
				else
				{
					ba.add(s);
				}
			}
			else
			{
				// Verifica ids específicos para adicionar direto (4699,4700,4702,4703)
				if (s.getId() == 4699 || s.getId() == 4700 || s.getId() == 4702 || s.getId() == 4703)
				{
					ba.add(s);
				}
			}
		}
		
		return ba;
	}
	
	public void setPage(Player seller, Player buyer, int page, int subPage)
	{
		if (page == 1)
		{
			buyer.getSellBuffMsg().setLastPage(1);
			buyer.getSellBuffMsg().setSubPage(subPage);
		}
		else if (page == 2)
		{
			buyer.getSellBuffMsg().setLastPage(2);
			buyer.getSellBuffMsg().setSubPage(subPage);
		}
		else
		{
			buyer.getSellBuffMsg().setLastPage(0);
			buyer.getSellBuffMsg().setSubPage(0);
		}
		buyer.getSellBuffMsg().sendBuyerResponse(seller, buyer, false);
	}
	
	@SuppressWarnings("static-access")
	public void buffBuyer(Player seller, Player buyer, int buffId)
	{
		if (buyer.getDistanceSq(seller) > 5000)
		{
			buyer.sendMessage("You must be closer the buffer!");
			// buyer.getSellBuffMsg().sendBuyerResponse(seller, buyer, true);
			buyer.setTarget(null);
		}
		else if (!seller.isSellBuff())
		{
			buyer.sendMessage("You cant buy buffs now!");
			return;
		}
		else if (!this.enoughSpiritOreForBuff(seller, buffId))
		{
			if (!seller.isOffShop())
			{
				seller.sendMessage("You can't sell buff, you haven't Spirit Ore for use skill!");
			}
			buyer.sendMessage("You can't buff now. Buff seller has no item for use skill!");
			buyer.getSellBuffMsg().sendBuyerResponse(seller, buyer, false);
		}
		else
		{
			int buffprice = seller.getBuffPrice();
			
			if (buyer.getInventory().getItemByItemId(Config.ITEM_BUFF_SHOP) == null || buyer.getInventory().getItemByItemId(Config.ITEM_BUFF_SHOP).getCount() < buffprice)
			{
				buyer.sendMessage("Not enought " + ItemTable.getInstance().getTemplate(Config.ITEM_BUFF_SHOP).getName() + " !");
			}
			else
			{
				try
				{
					// get buff data
					L2Skill skill = SkillTable.getInstance().getInfo(buffId, getSkillLevel(seller, buffId));
					
					// single skill mp consume
					if (Config.SELL_BUFF_SKILL_MP_ENABLED)
					{
						if (seller.getCurrentMp() * Config.SELL_BUFF_SKILL_MP_MULTIPLIER >= (skill.getMpConsume() + 1))
						{
							seller.setCurrentMp(Math.round(seller.getCurrentMp() * Config.SELL_BUFF_SKILL_MP_MULTIPLIER - skill.getMpConsume()) / Config.SELL_BUFF_SKILL_MP_MULTIPLIER);
						}
						else
						{
							buyer.sendMessage("Buffer has no Mana Points (" + Math.round(seller.getCurrentMp() * Config.SELL_BUFF_SKILL_MP_MULTIPLIER) + " / " + Math.round(seller.getMaxMp() * Config.SELL_BUFF_SKILL_MP_MULTIPLIER) + ")");
							buyer.getSellBuffMsg().sendBuyerResponse(seller, buyer, false);
							return;
						}
					}
					// add adena for seller
					seller.addItem("buff sell", Config.ITEM_BUFF_SHOP, buffprice, seller, true);
					
					// remove adena from buyer
					buyer.destroyItemByItemId("buff sell", Config.ITEM_BUFF_SHOP, buffprice, buyer, false);
					
					// destroy Spirit Ore:
					destroySpiritOre(seller, buffId);
					
					// give buffs for buyer
					skill.getEffects(buyer, buyer);
					if (Config.ENABLE_EFFECT_ANIMATION_SELLBUFFS)
					{
						buyer.broadcastPacket(new MagicSkillUse(buyer, buyer, buffId, 1, 500, 0));
					}
					
					buyer.sendMessage("You buyed: " + skill.getName() + " for " + seller.getBuffPrice() + " " + ItemTable.getInstance().getTemplate(Config.ITEM_BUFF_SHOP).getName() + ".");
					buyer.getSellBuffMsg().sendBuyerResponse(seller, buyer, false);
					
				}
				catch (Exception e)
				{
					e.printStackTrace();
					buyer.getSellBuffMsg().sendBuyerResponse(seller, buyer, true);
				}
			}
		}
	}
	
	@SuppressWarnings("static-access")
	public void buffBuyerPet(Player seller, Player buyer, int buffId)
	{
		// checking pet/summon if error return to first page;
		if (buyer.getPet() == null || buyer.getPet().isDead())
		{
			// buyer.getSellBuffMsg().sendBuyerResponse(seller, buyer, true);
			buyer.sendMessage("You must summon your pet first!");
			buyer.setTarget(null);
		}
		else if (buyer.getDistanceSq(seller) > 10000)
		{
			// buyer.getSellBuffMsg().sendBuyerResponse(seller, buyer, true);
			buyer.sendMessage("Your pet must be closer the buffer!");
			buyer.setTarget(null);
		}
		else if (!seller.isSellBuff())
		{
			buyer.sendMessage("You cant buy buffs now!");
			return;
		}
		else if (!this.enoughSpiritOreForBuff(seller, buffId))
		{
			if (!seller.isOffShop())
			{
				seller.sendMessage("You can't sell buff, you haven't Spirit Ore for use skill!");
			}
			buyer.sendMessage("You can't buff now. Buff seller has no item for use skill!");
			buyer.getSellBuffMsg().sendBuyerResponse(seller, buyer, false);
		}
		else
		{
			int buffprice = seller.getBuffPrice();
			
			if (buyer.getInventory().getItemByItemId(Config.ITEM_BUFF_SHOP) == null || buyer.getInventory().getItemByItemId(Config.ITEM_BUFF_SHOP).getCount() < buffprice)
			{
				buyer.sendMessage("Not enought " + ItemTable.getInstance().getTemplate(Config.ITEM_BUFF_SHOP).getName() + " !");
			}
			else
			{
				
				try
				{
					// get buff data
					L2Skill skill = SkillTable.getInstance().getInfo(buffId, getSkillLevel(seller, buffId));
					
					// single skill mp consume
					if (Config.SELL_BUFF_SKILL_MP_ENABLED)
					{
						if (seller.getCurrentMp() * Config.SELL_BUFF_SKILL_MP_MULTIPLIER >= (skill.getMpConsume() + 1))
						{
							seller.setCurrentMp(Math.round(seller.getCurrentMp() * Config.SELL_BUFF_SKILL_MP_MULTIPLIER - skill.getMpConsume()) / Config.SELL_BUFF_SKILL_MP_MULTIPLIER);
						}
						else
						{
							buyer.sendMessage("Buffer has no Mana Points (" + Math.round(seller.getCurrentMp() * Config.SELL_BUFF_SKILL_MP_MULTIPLIER) + " / " + Math.round(seller.getMaxMp() * Config.SELL_BUFF_SKILL_MP_MULTIPLIER) + ")");
							buyer.getSellBuffMsg().sendBuyerResponse(seller, buyer, true);
							return;
						}
					}
					
					// buff seller
					seller.addItem("buff sell", Config.ITEM_BUFF_SHOP, buffprice, seller, true);
					
					// buff buyer
					buyer.destroyItemByItemId("buff sell", Config.ITEM_BUFF_SHOP, buffprice, buyer, false);
					
					// destroy Spirit Ore:
					destroySpiritOre(seller, buffId);
					
					// give buffs for pet
					skill.getEffects(buyer.getPet(), buyer.getPet());
					
					buyer.sendMessage("You buyed: " + skill.getName() + " for " + seller.getBuffPrice() + " " + ItemTable.getInstance().getTemplate(Config.ITEM_BUFF_SHOP).getName() + " , your pet.");
					buyer.getSellBuffMsg().sendBuyerResponse(seller, buyer, false);
					
				}
				catch (Exception e)
				{
					e.printStackTrace();
					buyer.getSellBuffMsg().sendBuyerResponse(seller, buyer, true);
				}
			}
		}
	}
	
	public void buffsetBuyer(Player seller, Player buyer, int setId)
	{
		String buffSetName = " ";
		List<L2Skill> ba = new ArrayList<>();
		
		// ----------------------------------------------------------------------------------------------
		/*
		 * if(setId == 1) { ba = seller.getSellBuffMsg().getBuffsetList(seller, Config.SELL_BUFFSET_WARRIOR); buffSetName = " Warrior "; }else if(setId == 2){ ba = seller.getSellBuffMsg().getBuffsetList(seller, Config.SELL_BUFFSET_MAGE); buffSetName = " Mage "; }else if(setId == 3){ ba =
		 * seller.getSellBuffMsg().getBuffsetList(seller, Config.SELL_BUFFSET_RECHARGER); buffSetName = " Recharger "; }else if(setId == 4){ ba = seller.getSellBuffMsg().getBuffsetList(seller, Config.SELL_BUFFSET_TANKER); buffSetName = " Tanker "; }
		 */
		
		int buffsetSize = ba.size();
		
		if (buyer.getDistanceSq(seller) > 5000)
		{
			buyer.sendMessage("You must be closer the buffer!");
			// buyer.getSellBuffMsg().sendBuyerResponse(seller, buyer, true);
			buyer.setTarget(null);
		}
		else if (!seller.isSellBuff())
		{
			buyer.sendMessage("You cant buy buffs now!");
			return;
		}
		else
		{
			int buffprice = seller.getBuffPrice();
			if (seller.getBuffPrice() == 0)
			{
				buyer.sendMessage("This buffset is empty!");
				buyer.getSellBuffMsg().sendBuyerResponse(seller, buyer, true);
			}
			else if (buyer.getInventory().getItemByItemId(Config.ITEM_BUFF_SHOP) == null || buyer.getInventory().getItemByItemId(Config.ITEM_BUFF_SHOP).getCount() < buffprice * buffsetSize)
			{
				buyer.sendMessage("Not enought adena!");
			}
			else
			{
				try
				{
					boolean mpOK = true;
					boolean soOK = true;
					int mpConsumeSum = 0;
					int soConsumeSum = 0; // Spirit Ore sum.
					
					if (Config.SELL_BUFF_SKILL_MP_ENABLED || Config.SELL_BUFF_SKILL_ITEM_CONSUME_ENABLED)
					{
						// calculate all skills set mana use:
						for (L2Skill p : ba)
						{
							L2Skill skill = SkillTable.getInstance().getInfo(p.getId(), getSkillLevel(seller, p.getId()));
							if (Config.SELL_BUFF_SKILL_MP_ENABLED)
							{
								mpConsumeSum += skill.getMpConsume();
							}
							if (Config.SELL_BUFF_SKILL_ITEM_CONSUME_ENABLED)
							{
								soConsumeSum += getSkillConsumeSOCount(p.getId(), p.getLevel());
							}
						}
						// set ok == false if buffset mp cost greater than seller current mp.
						if (mpConsumeSum > 0 && (mpConsumeSum + 1) > seller.getCurrentMp() * Config.SELL_BUFF_SKILL_MP_MULTIPLIER)
						{
							mpOK = false;
						}
						// set ok == false if buffset require more SO count than count SO in inventory.
						if (soConsumeSum > 0 && (seller.getInventory().getItemByItemId(3031) == null || soConsumeSum > seller.getInventory().getItemByItemId(3031).getCount()))
						{
							soOK = false;
						}
					}
					
					if (mpOK && soOK)
					{
						for (L2Skill p : ba)
						{
							L2Skill skill = SkillTable.getInstance().getInfo(p.getId(), getSkillLevel(seller, p.getId()));
							
							// give buffs
							skill.getEffects(buyer, buyer);
							
							// destroy Spirit Ore:
							destroySpiritOre(seller, skill.getId());
							
							buyer.sendMessage("You buyed: " + skill.getName());
							buyer.getSellBuffMsg().sendBuyerResponse(seller, buyer, false);
							if (Config.SELL_BUFF_SKILL_MP_ENABLED)
							{
								seller.setCurrentMp(Math.round(seller.getCurrentMp() * Config.SELL_BUFF_SKILL_MP_MULTIPLIER - skill.getMpConsume()) / Config.SELL_BUFF_SKILL_MP_MULTIPLIER);
							}
						}
						
						// buff seller
						seller.addItem("buff sell", Config.ITEM_BUFF_SHOP, buffprice * buffsetSize, seller, true);
						
						// buff buyer
						buyer.destroyItemByItemId("buff sell", Config.ITEM_BUFF_SHOP, buffprice * buffsetSize, buyer, false);
						
						buyer.sendMessage("You bought" + buffSetName + "buffs for: " + buffprice * buffsetSize + " adena.");
					}
					else
					{
						if (!mpOK)
						{
							buyer.sendMessage("Buffer has no Mana Points for this set (" + Math.round(seller.getCurrentMp() * Config.SELL_BUFF_SKILL_MP_MULTIPLIER) + " / " + Math.round(seller.getMaxMp() * Config.SELL_BUFF_SKILL_MP_MULTIPLIER) + ")");
						}
						if (!soOK)
						{
							buyer.sendMessage("You can't buff now. Buff seller has no item for use skill!");
							seller.sendMessage("You can't sell buff, you haven't Spirit Ore for use skill!");
						}
						buyer.getSellBuffMsg().sendBuyerResponse(seller, buyer, true);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					buyer.getSellBuffMsg().sendBuyerResponse(seller, buyer, true);
				}
			}
		}
	}
	
	public void buffsetBuyerPet(Player seller, Player buyer, int setId)
	{
		String buffSetName = " ";
		List<L2Skill> ba = new ArrayList<>();
		
		// ----------------------------------------------------------------------------------------------
		/*
		 * if(setId == 1) { ba = seller.getSellBuffMsg().getBuffsetList(seller, Config.SELL_BUFFSET_WARRIOR); buffSetName = " Warrior "; } else if(setId == 2){ ba = seller.getSellBuffMsg().getBuffsetList(seller, Config.SELL_BUFFSET_MAGE); buffSetName = " Mage "; } else if(setId == 3) { ba =
		 * seller.getSellBuffMsg().getBuffsetList(seller, Config.SELL_BUFFSET_RECHARGER); buffSetName = " Recharger "; } else if(setId == 4) { ba = seller.getSellBuffMsg().getBuffsetList(seller, Config.SELL_BUFFSET_TANKER); buffSetName = " Tanker "; }
		 */
		
		int buffsetSize = ba.size();
		
		// checking pet/summon if error return to first page;
		if (buyer.getPet() == null || buyer.getPet().isDead())
		{
			buyer.getSellBuffMsg().sendBuyerResponse(seller, buyer, true);
			buyer.sendMessage("You must summon your pet first!");
		}
		else if (buyer.getDistanceSq(seller) > 10000)
		{
			// buyer.getSellBuffMsg().sendBuyerResponse(seller, buyer, true);
			buyer.sendMessage("Your pet must be closer the buffer!");
			buyer.setTarget(null);
		}
		else if (!seller.isSellBuff())
		{
			buyer.sendMessage("You cant buy buffs now!");
			return;
		}
		else
		{
			int buffprice = seller.getBuffPrice();
			if (seller.getBuffPrice() == 0)
			{
				buyer.sendMessage("This buffset is empty!");
				buyer.getSellBuffMsg().sendBuyerResponse(seller, buyer, true);
			}
			else if (buyer.getInventory().getItemByItemId(Config.ITEM_BUFF_SHOP) == null || buyer.getInventory().getItemByItemId(Config.ITEM_BUFF_SHOP).getCount() < buffprice)
			{
				buyer.sendMessage("Not enought adena!");
			}
			else
			{
				
				try
				{
					boolean mpOK = true;
					boolean soOK = true;
					int mpConsumeSum = 0;
					int soConsumeSum = 0; // Spirit Ore sum.
					
					if (Config.SELL_BUFF_SKILL_MP_ENABLED || Config.SELL_BUFF_SKILL_ITEM_CONSUME_ENABLED)
					{
						// calculate all skills set mana use:
						for (L2Skill p : ba)
						{
							L2Skill skill = SkillTable.getInstance().getInfo(p.getId(), getSkillLevel(seller, p.getId()));
							if (Config.SELL_BUFF_SKILL_MP_ENABLED)
							{
								mpConsumeSum += skill.getMpConsume();
							}
							if (Config.SELL_BUFF_SKILL_ITEM_CONSUME_ENABLED)
							{
								soConsumeSum += getSkillConsumeSOCount(p.getId(), p.getLevel());
							}
						}
						// set ok == false if buffset mp cost greater than seller current mp.
						if (mpConsumeSum > 0 && (mpConsumeSum + 1) > seller.getCurrentMp() * Config.SELL_BUFF_SKILL_MP_MULTIPLIER)
						{
							mpOK = false;
						}
						// set ok == false if buffset require more SO count than count SO in inventory.
						if (soConsumeSum > 0 && (seller.getInventory().getItemByItemId(3031) == null || soConsumeSum > seller.getInventory().getItemByItemId(3031).getCount()))
						{
							soOK = false;
						}
					}
					
					if (mpOK && soOK)
					{
						for (L2Skill p : ba)
						{
							L2Skill skill = SkillTable.getInstance().getInfo(p.getId(), getSkillLevel(seller, p.getId()));
							
							// give buffs
							skill.getEffects(buyer.getPet(), buyer.getPet());
							
							// destroy Spirit Ore:
							destroySpiritOre(seller, skill.getId());
							
							buyer.sendMessage("You buyed: " + skill.getName());
							buyer.getSellBuffMsg().sendBuyerResponse(seller, buyer, false);
							if (Config.SELL_BUFF_SKILL_MP_ENABLED)
							{
								seller.setCurrentMp(Math.round(seller.getCurrentMp() * Config.SELL_BUFF_SKILL_MP_MULTIPLIER - skill.getMpConsume()) / Config.SELL_BUFF_SKILL_MP_MULTIPLIER);
							}
						}
						
						// buff seller
						seller.addItem("buff sell", Config.ITEM_BUFF_SHOP, buffprice * buffsetSize, seller, true);
						
						// buff buyer
						buyer.destroyItemByItemId("buff sell", Config.ITEM_BUFF_SHOP, buffprice * buffsetSize, buyer, false);
						
						buyer.sendMessage("You bought" + buffSetName + "buffs for: " + buffprice * buffsetSize + " adena.");
					}
					else
					{
						if (!mpOK)
						{
							buyer.sendMessage("Buffer has no Mana Points for this set (" + Math.round(seller.getCurrentMp() * Config.SELL_BUFF_SKILL_MP_MULTIPLIER) + " / " + Math.round(seller.getMaxMp() * Config.SELL_BUFF_SKILL_MP_MULTIPLIER) + ")");
						}
						if (!soOK)
						{
							buyer.sendMessage("You can't buff now. Buff seller has no item for use skill!");
							seller.sendMessage("You can't sell buff, you haven't Spirit Ore for use skill!");
						}
						buyer.getSellBuffMsg().sendBuyerResponse(seller, buyer, true);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
					buyer.getSellBuffMsg().sendBuyerResponse(seller, buyer, true);
				}
			}
		}
	}
	
	public void startBuffStore(Player seller, int price, boolean saveSellerData)
	{
		if (price <= 0)
		{
			seller.sendMessage("Too low price. Min is 1 adena.");
			return;
		}
		
		if (price > 2000000000)
		{
			seller.sendMessage("Too big price. Max is 2 000 000 000 adena.");
			return;
		}
		
		if (saveSellerData)
		{
			saveSellerData(seller);
		}
		
		seller.setBuffPrice(price);
		seller.sitDown();
		seller.setSellBuff(true);
		
		// if(seller.getClassId().getId() < 90){ //if 2nd profession or lower.
		// seller.getAppearance().setTitleColor(0xFFDD); //dark green (yellow now)
		// }else{
		// seller.getAppearance().setTitleColor(0xFF00); //light green
		// }
		// seller.setTitle(getClassName(seller.getClassId().getId())); //set title like class name
		// seller.getAppearance().setNameColor(0x1111);
		seller.broadcastUserInfo();
		// seller.broadcastTitleInfo();
		
	}
	
	public void stopBuffStore(Player seller)
	{
		
		restoreSellerData(seller);
		
		seller.broadcastUserInfo();
		// seller.broadcastTitleInfo();
		seller.setSellBuff(false);
		seller.standUp();
		
	}
	
	private static void saveSellerData(Player seller)
	{
		try (Connection con = ConnectionPool.getConnection(); PreparedStatement statement = con.prepareStatement("CALL sellbuff_saveSellerData(?,?,?,?)"))
		{
			
			statement.setInt(1, seller.getObjectId());
			statement.setString(2, seller.getTitle());
			statement.setInt(3, seller.getAppearance().getTitleColor());
			statement.setInt(4, seller.getAppearance().getNameColor());
			statement.execute();
			
		}
		catch (Exception e)
		{
			if (Config.DEBUG)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void restoreSellerData(Player seller)
	{
		try (Connection con = ConnectionPool.getConnection(); PreparedStatement statement = con.prepareStatement("CALL sellbuff_restoreSellerData(?)"))
		{
			
			statement.setInt(1, seller.getObjectId());
			
			try (ResultSet res = statement.executeQuery())
			{
				while (res.next())
				{
					seller.setTitle(res.getString("lastTitle"));
					seller.getAppearance().setTitleColor(res.getInt("lastTitleColor"));
					seller.getAppearance().setNameColor(res.getInt("lastNameColor"));
				}
			}
		}
		catch (Exception e)
		{
			if (Config.DEBUG)
				e.printStackTrace();
		}
	}
	
	/**
	 * Returns skill consume count (Spirit Ore count).
	 * @param skillId
	 * @param skillLvl
	 * @return
	 */
	private static int getSkillConsumeSOCount(int skillId, int skillLvl)
	{
		// Buffs what consume items like Spirit Ore
		// {skillId, skillLvl, itemConsumeId, itemConsumeCount}
		int[][] skillsData =
		{
			{
				1388,
				1,
				3031,
				1
			},
			{
				1388,
				2,
				3031,
				2
			},
			{
				1388,
				3,
				3031,
				3
			},
			{
				1389,
				1,
				3031,
				1
			},
			{
				1389,
				2,
				3031,
				2
			},
			{
				1389,
				3,
				3031,
				3
			},
			{
				1356,
				1,
				3031,
				10
			},
			{
				1397,
				1,
				3031,
				1
			},
			{
				1397,
				2,
				3031,
				2
			},
			{
				1397,
				3,
				3031,
				3
			},
			{
				1355,
				1,
				3031,
				10
			},
			{
				1357,
				1,
				3031,
				10
			},
			{
				1416,
				1,
				3031,
				20
			},
			{
				1414,
				1,
				3031,
				40
			},
			{
				1391,
				1,
				3031,
				4
			},
			{
				1391,
				2,
				3031,
				8
			},
			{
				1391,
				3,
				3031,
				12
			},
			{
				1390,
				1,
				3031,
				4
			},
			{
				1390,
				2,
				3031,
				8
			},
			{
				1390,
				3,
				3031,
				12
			},
			{
				1363,
				1,
				3031,
				40
			},
			{
				1413,
				1,
				3031,
				40
			},
			{
				1323,
				1,
				3031,
				5
			}
		};
		
		for (int i = 0; i < skillsData.length; i++)
		{
			if (skillsData[i][0] == skillId && skillsData[i][1] == skillLvl)
			{
				return skillsData[i][3];
			}
		}
		return 0;
	}
	
	private static boolean enoughSpiritOreForBuff(Player seller, int skillId)
	{
		if (Config.SELL_BUFF_SKILL_ITEM_CONSUME_ENABLED)
		{
			if (getSkillConsumeSOCount(skillId, seller.getSkillLevel(skillId)) > 0)
			{
				// 3031 == Spirit Ore ID
				if (seller.getInventory().getItemByItemId(3031) == null || seller.getInventory().getItemByItemId(3031).getCount() < getSkillConsumeSOCount(skillId, seller.getSkillLevel(skillId)))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	private static void destroySpiritOre(Player seller, int skillId)
	{
		if (Config.SELL_BUFF_SKILL_ITEM_CONSUME_ENABLED)
		{
			if (seller.getInventory().getItemByItemId(3031) != null && seller.getInventory().getItemByItemId(3031).getCount() >= getSkillConsumeSOCount(skillId, seller.getSkillLevel(skillId)))
			{
				seller.destroyItemByItemId("buff sell", 3031, getSkillConsumeSOCount(skillId, seller.getSkillLevel(skillId)), seller, false);
			}
		}
	}
	
	@SuppressWarnings("unused")
	private static String getClassName(int classId)
	{
		switch (classId)
		{
			case 14:
				return "Warlock";
			case 96:
				return "ArcanaLord";
			case 16:
				return "Bishop";
			case 97:
				return "Cardinal";
			case 17:
				return "Prophet";
			case 98:
				return "Hierophant";
			case 21:
				return "SwordSinger";
			case 100:
				return "SwordMuse";
			case 28:
				return "Elem.Summoner";
			case 104:
				return "Elem.Master";
			case 29:
				return "Oracle";
			case 30:
				return "Elder";
			case 105:
				return "EvaSaint";
			case 34:
				return "BladeDancer";
			case 107:
				return "SpectralDancer";
			case 42:
				return "ShilenOracle";
			case 43:
				return "ShilenElder";
			case 112:
				return "ShilenSaint";
			case 50:
				return "Shaman";
			case 51:
				return "Overlord";
			case 115:
				return "Dominator";
			case 52:
				return "Warcryer";
			case 116:
				return "Doomcryer";
			
			default:
				return "Buff Sell";
		}
	}
	
	/**
	 * Special summoners skill level or normal skill level.
	 * @param seller
	 * @param skillId
	 * @return
	 */
	private static int getSkillLevel(Player seller, int skillId)
	{
		
		if (seller.getClassId().getId() != 96 && seller.getClassId().getId() != 14 && seller.getClassId().getId() != 104 && seller.getClassId().getId() != 28)
		{
			return seller.getSkillLevel(skillId);
		}
		
		// summon lvl != summon buff lvl, example: feline queen skill lvl != feline queen buff lvl.
		// skill levels by current db: min: 5, max: 8.
		if (seller.getLevel() >= 56 && seller.getLevel() <= 57)
		{
			return 5;
		}
		if (seller.getLevel() >= 58 && seller.getLevel() <= 67)
		{
			return 6;
		}
		if (seller.getLevel() >= 68 && seller.getLevel() <= 73)
		{
			return 7;
		}
		if (seller.getLevel() >= 74)
		{
			return 8;
		}
		return 1;
	}
	
}