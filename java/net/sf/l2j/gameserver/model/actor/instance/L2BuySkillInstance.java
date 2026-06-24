package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.base.ClassRace;
import net.sf.l2j.gameserver.model.base.ClassType;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;

public class L2BuySkillInstance extends L2NpcInstance
{
	public L2BuySkillInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onAction(Player player)
	{
		if (this != player.getTarget())
		{
			player.setTarget(this);
			player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
			player.sendPacket(new ValidateLocation(this));
		}
		// mage
		else if (player.isMageClass() || (player.getTemplate().getRace() == ClassRace.ORC && (player.getClassId().getType() == ClassType.MYSTIC || player.getClassId().getType() == ClassType.PRIEST)) && isInsideRadius(player, 150, false, false)) // mage
		{
			SocialAction sa = new SocialAction(this, Rnd.get(8));
			broadcastPacket(sa);
			player.setCurrentFolkNPC(this);
			showMessageWindowMager(player);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		// fight
		else if (!player.isMageClass() || (player.getTemplate().getRace() == ClassRace.ORC && (player.getClassId().getType() == ClassType.MYSTIC || player.getClassId().getType() == ClassType.PRIEST)) && isInsideRadius(player, 150, false, false)) // mage
		{
			SocialAction sa = new SocialAction(this, Rnd.get(8));
			broadcastPacket(sa);
			player.setCurrentFolkNPC(this);
			showMessageWindowFighter(player);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else
		{
			player.getAI().setIntention(CtrlIntention.INTERACT, this);
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (command.startsWith("learn"))// Fight
		{
			String[] args = command.substring(6).split(" ");
			int id = Integer.parseInt(args[0]);
			int item = Config.BUY_SKILL_ITEM;
			int count = Config.BUY_SKILL_PRICE;
			if (player.getSkillBought() >= Config.BUY_SKILL_MAX_SLOTS)
			{
				player.sendMessage("You cannot learn more skills.");
				return;
			}
			if (Config.ENABLE_SKILL_MAIN_CLASS)
			{
				if (player.getClassIndex() != 0)
				{
					player.sendMessage("Skill is only available for base class.");
					return;
				}
			}
			
			if (player.getInventory().getItemByItemId(item) == null || player.getInventory().getItemByItemId(item).getCount() < count)
			{
				player.sendMessage("Incorrect item count.");
				return;
			}
			L2Skill skill = SkillTable.getInstance().getInfo(id, player.getSkillLevel(id));
			if (skill != null)
			{
				player.sendMessage("You already have this skill.");
				return;
			}
			player.destroyItemByItemId("Coin", item, count, this, true);
			player.addSkill(SkillTable.getInstance().getInfo(id, SkillTable.getInstance().getMaxLevel(id)), true);
			player.sendSkillList();
			player.setSkillBought(player.getSkillBought() + 1);
			player.store();
			if (!player.isMageClass() || (player.getTemplate().getRace() == ClassRace.ORC && (player.getClassId().getType() == ClassType.MYSTIC || player.getClassId().getType() == ClassType.PRIEST)) && isInsideRadius(player, 150, false, false))
			{
				showMessageWindowFighter(player);
			}
			if (player.isMageClass() || (player.getTemplate().getRace() == ClassRace.ORC && (player.getClassId().getType() == ClassType.MYSTIC || player.getClassId().getType() == ClassType.PRIEST)) && isInsideRadius(player, 150, false, false)) // mage)
			{
				showMessageWindowMager(player);
			}
			
		}
		if (player.getClassId() == ClassId.SHILLIEN_ELDER || player.getClassId() == ClassId.SHILLIEN_SAINT || player.getClassId() == ClassId.BISHOP || player.getClassId() == ClassId.CARDINAL || player.getClassId() == ClassId.ELVEN_ELDER || (player.getClassId() == ClassId.EVAS_SAINT && !Config.ALLOW_HEALER_NPC))
		{
			NpcHtmlMessage msg = new NpcHtmlMessage(5);
			msg.setFile("data/html/mods/skillManager/Protect/healer.htm");
			player.sendPacket(msg);
			return;
		}
		if (player.isAio())
		{
			NpcHtmlMessage msg = new NpcHtmlMessage(5);
			msg.setFile("data/html/mods/skillManager/Protect/aioxskill.htm");
			player.sendPacket(msg);
			return;
		}
		if (player.isSellBuff())
		{
			player.sendMessage("Sell buffs Character, Can't Speak To Village Masters.");
			return;
		}
		if (command.startsWith("remove"))
			if (player.getInventory().getInventoryItemCount(Config.REMOVE_SKILL_COIN_ID, 0) >= Config.REMOVE_SKILL_COIN_COUNT)
			{
				String skills = command.substring(7);
				int skillId = Integer.parseInt(skills);
				L2Skill skill = SkillTable.getInstance().getInfo(skillId, player.getSkillLevel(skillId));
				if (skill == null)
				{
					player.sendMessage("You dont have this skill.");
					return;
				}
				player.getInventory().destroyItemByItemId("Remove", Config.REMOVE_SKILL_COIN_ID, Config.REMOVE_SKILL_COIN_COUNT, player, null);
				player.getInventory().addItem("Add", Config.CHARGE_BACK_ITEM_ID, Config.CHARGE_BACK_ITEM_COUNT, player, null);
				player.getInventory().updateDatabase();
				player.sendMessage("You received " + ItemTable.getInstance().getTemplate(Config.CHARGE_BACK_ITEM_ID).getName() + " amount " + Config.CHARGE_BACK_ITEM_COUNT);
				player.sendPacket(new ItemList(player, true));
				player.removeSkill(SkillTable.getInstance().getInfo(skillId, player.getSkillLevel(skillId)));
				player.sendSkillList();
				// showMessageWindowFighter(player);
				player.setSkillBought(player.getSkillBought() - 1);
				player.store();
			}
			else
			{
				player.sendMessage("Incorrect item count.");
			}
		super.onBypassFeedback(player, command);
	}
	
	private void showMessageWindowFighter(Player player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile("data/html/mods/skillManager/MainFight.htm");
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
	
	private void showMessageWindowMager(Player player)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile("data/html/mods/skillManager/MainMage.htm");
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
}
