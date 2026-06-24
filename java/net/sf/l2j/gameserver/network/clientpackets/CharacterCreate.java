package net.sf.l2j.gameserver.network.clientpackets;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Calendar;

import net.sf.l2j.Config;
import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.gameserver.ConnectionPool;
import net.sf.l2j.gameserver.datatables.CharNameTable;
import net.sf.l2j.gameserver.datatables.CharTemplateTable;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SkillTreeTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2ShortCut;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2SkillLearn;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.PcTemplate;
import net.sf.l2j.gameserver.model.base.ClassRace;
import net.sf.l2j.gameserver.model.base.ClassType;
import net.sf.l2j.gameserver.model.base.Experience;
import net.sf.l2j.gameserver.model.base.Sex;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CharCreateFail;
import net.sf.l2j.gameserver.network.serverpackets.CharCreateOk;
import net.sf.l2j.gameserver.network.serverpackets.CharSelectInfo;
import net.sf.l2j.gameserver.network.serverpackets.EtcStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.scriptings.Quest;
import net.sf.l2j.gameserver.scriptings.ScriptManager;

@SuppressWarnings("unused")
public final class CharacterCreate extends L2GameClientPacket
{
	// cSdddddddddddd
	private String _name;
	private int _race;
	private byte _sex;
	private int _classId;
	private int _int;
	private int _str;
	private int _con;
	private int _men;
	private int _dex;
	private int _wit;
	private byte _hairStyle;
	private byte _hairColor;
	private byte _face;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
		_race = readD();
		_sex = (byte) readD();
		_classId = readD();
		_int = readD();
		_str = readD();
		_con = readD();
		_men = readD();
		_dex = readD();
		_wit = readD();
		_hairStyle = (byte) readD();
		_hairColor = (byte) readD();
		_face = (byte) readD();
	}
	
	@Override
	protected void runImpl()
	{
		if (!StringUtil.isValidPlayerName(_name))
		{
			sendPacket(new CharCreateFail((_name.length() > 16) ? CharCreateFail.REASON_16_ENG_CHARS : CharCreateFail.REASON_INCORRECT_NAME));
			return;
		}
		
		if (_face > 2 || _face < 0)
		{
			sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
			return;
		}
		if (Config.FORBIDDEN_NAMES.length > 1)
		{
			for (String st : Config.FORBIDDEN_NAMES)
			{
				if (_name.toLowerCase().contains(st.toLowerCase()))
				{
					sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
					return;
				}
			}
		}
		if (_hairStyle < 0 || (_sex == 0 && _hairStyle > 4) || (_sex != 0 && _hairStyle > 6))
		{
			sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
			return;
		}
		
		if (_hairColor > 3 || _hairColor < 0)
		{
			sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
			return;
		}
		
		Player newChar = null;
		PcTemplate template = null;
		
		/*
		 * DrHouse: Since checks for duplicate names are done using SQL, lock must be held until data is written to DB as well.
		 */
		synchronized (CharNameTable.getInstance())
		{
			if (CharNameTable.accountCharNumber(getClient().getAccountName()) >= 7)
			{
				sendPacket(new CharCreateFail(CharCreateFail.REASON_TOO_MANY_CHARACTERS));
				return;
			}
			
			if (CharNameTable.getInstance().getIdByName(_name) > 0)
			{
				sendPacket(new CharCreateFail(CharCreateFail.REASON_NAME_ALREADY_EXISTS));
				return;
			}
			
			template = CharTemplateTable.getInstance().getTemplate(_classId);
			if (template == null || template.getClassBaseLevel() > 1)
			{
				sendPacket(new CharCreateFail(CharCreateFail.REASON_CREATION_FAILED));
				return;
			}
			
			int objectId = IdFactory.getInstance().getNextId();
			newChar = Player.create(objectId, template, getClient().getAccountName(), _name, _hairStyle, _hairColor, _face, Sex.values()[_sex]);
		}
		
		newChar.setCurrentCp(0);
		newChar.setCurrentHp(newChar.getMaxHp());
		newChar.setCurrentMp(newChar.getMaxMp());
		
		// send acknowledgement
		sendPacket(CharCreateOk.STATIC_PACKET);
		if (Config.STARTING_BUFFS)
		{
			if (!newChar.isMageClass())
			{
				for (int[] buff : Config.STARTING_BUFFS_F) // Custom buffs for fighters
				{
					L2Skill skill = SkillTable.getInstance().getInfo(buff[0], buff[1]);
					if (skill != null)
					{
						skill.getEffects(newChar, newChar);
						newChar.sendPacket(new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(buff[0]));
					}
				}
			}
			else
			{
				for (int[] buff : Config.STARTING_BUFFS_M) // Custom buffs for mystics
				{
					L2Skill skill = SkillTable.getInstance().getInfo(buff[0], buff[1]);
					if (skill != null)
					{
						skill.getEffects(newChar, newChar);
						newChar.sendPacket(new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(buff[0]));
					}
				}
			}
		}
		L2World.getInstance().addObject(newChar);
		
		newChar.addAdena("Init", Config.STARTING_ADENA, null, false);
		// newChar.setXYZInvisible(template.getSpawnX(), template.getSpawnY(), template.getSpawnZ());
		if (Config.ALLOW_CUSTOM_SPAWN_LOCATION)
		{
			newChar.setXYZInvisible(Config.CUSTOM_SPAWN_LOCATION[0], Config.CUSTOM_SPAWN_LOCATION[1], Config.CUSTOM_SPAWN_LOCATION[2]);
		
		}
		else
		{
			newChar.setXYZInvisible(template.getSpawnX(), template.getSpawnY(), template.getSpawnZ());
		}
		
		
		
		// newChar.setTitle("");
		newChar.setTitle(Config.ALLOW_NEW_CHAR_TITLE ? Config.NEW_CHAR_TITLE : "");
		
		newChar.registerShortCut(new L2ShortCut(0, 0, 3, 2, -1, 1)); // attack shortcut
		newChar.registerShortCut(new L2ShortCut(3, 0, 3, 5, -1, 1)); // take shortcut
		newChar.registerShortCut(new L2ShortCut(10, 0, 3, 0, -1, 1)); // sit shortcut
		
		if (Config.AUTO_MACRO_MENU)
		{
			InsertNewMacro(newChar, 1003, 3, "Menu", "", "MENU", "3,0,0,.menu;");
			newChar.registerShortCut(new L2ShortCut(5, 0, 4, 1003, -1, 0));
		}
		if (Config.AUTO_MACRO_AUTOFARM)
		{
			InsertNewMacro(newChar, 1004, 0, "farm", "", "FARM", "3,0,0,.autofarm;");
			newChar.registerShortCut(new L2ShortCut(6, 0, 4, 1004, -1, 0));
		}
		
		for (Item ia : template.getItems())
		{
			ItemInstance item = newChar.getInventory().addItem("Init", ia.getItemId(), 1, newChar, null);
			// if (item.getItemId() == 5588) // tutorial book shortcut
			if (item.getItemId() == Config.BOOK_ITEM_CUSTOM)
				newChar.registerShortCut(new L2ShortCut(11, 0, 1, item.getObjectId(), -1, 1));
			
			if (item.isEquipable())
			{
				if (newChar.getActiveWeaponItem() == null || !(item.getItem().getType2() != Item.TYPE2_WEAPON))
					newChar.getInventory().equipItemAndRecord(item);
			}
		}
		
		for (L2SkillLearn skill : SkillTreeTable.getInstance().getAvailableSkills(newChar, newChar.getClassId()))
		{
			newChar.addSkill(SkillTable.getInstance().getInfo(skill.getId(), skill.getLevel()), true);
			if (skill.getId() == 1001 || skill.getId() == 1177)
				newChar.registerShortCut(new L2ShortCut(1, 0, 2, skill.getId(), 1, 1));
			
			if (skill.getId() == 1216)
				newChar.registerShortCut(new L2ShortCut(9, 0, 2, skill.getId(), 1, 1));
		}
		if (Config.STARTING_LEVEL != 0)
			newChar.getStat().addExp(Experience.LEVEL[Config.STARTING_LEVEL]);
		if (Config.CUSTOM_STARTER_ITEMS_ENABLED)
		{
			// if (newChar.isMageClass())
			if (newChar.isMageClass() || (newChar.getTemplate().getRace() == ClassRace.ORC && (newChar.getClassId().getType() == ClassType.MYSTIC || newChar.getClassId().getType() == ClassType.PRIEST)))
			{
				for (final int[] reward : Config.STARTING_CUSTOM_ITEMS_M)
				{
					if (ItemTable.getInstance().createDummyItem(reward[0]).isStackable())
						newChar.getInventory().addItem("Starter Items Mage", reward[0], reward[1], newChar, null);
					else
						for (int i = 0; i < reward[1]; ++i)
							newChar.getInventory().addItem("Starter Items Mage", reward[0], 1, newChar, null);
				}
			}
			else
			{
				for (final int[] reward : Config.STARTING_CUSTOM_ITEMS_F)
				{
					if (ItemTable.getInstance().createDummyItem(reward[0]).isStackable())
						newChar.getInventory().addItem("Starter Items Fighter", reward[0], reward[1], newChar, null);
					else
						for (int i = 0; i < reward[1]; ++i)
							newChar.getInventory().addItem("Starter Items Fighter", reward[0], 1, newChar, null);
				}
			}
		}
		if (Config.ENABLE_VIP_FREE)
		{
			CharacterstartedVip(newChar, 1);
		}
		if (Config.ALLOW_CUSTOM_CHAR_NOBLE)
		{
			newChar.setNoble(true, true);
			newChar.getInventory().addItem("Tiara", 7694, 1, newChar, null);
			newChar.sendSkillList();
		}
		if (!Config.DISABLE_TUTORIAL)
		{
			if (newChar.getQuestState("Tutorial") == null)
			{
				Quest q = ScriptManager.getInstance().getQuest("Tutorial");
				if (q != null)
					q.newQuestState(newChar).setState(Quest.STATE_STARTED);
			}
		}
		newChar.setOnlineStatus(true, false);
		newChar.deleteMe();
		
		final CharSelectInfo cl = new CharSelectInfo(getClient().getAccountName(), getClient().getSessionId().playOkID1);
		getClient().getConnection().sendPacket(cl);
		getClient().setCharSelection(cl.getCharInfo());
	}
	
	/**
	 * @param activeChar
	 * @param days
	 */
	private static void CharacterstartedVip(Player activeChar, int days)
	{
		if (activeChar.isVip())
		{
			long daysleft = (activeChar.getVipEndTime() - Calendar.getInstance().getTimeInMillis()) / 86400000L;
			activeChar.setEndTime("vip", (int) (daysleft + Config.VIP_DAYS_FREE));
			activeChar.sendMessage("[PT] Parabens! aproveite seu " + Config.VIP_DAYS_FREE + " dias de VIP.");
			activeChar.sendMessage("[EN] Congratulations! enjoy your " + Config.VIP_DAYS_FREE + " VIP days.");
		}
		else
		{
			activeChar.setVip(true);
			activeChar.setEndTime("vip", Config.VIP_DAYS_FREE);
			activeChar.sendMessage("[PT] Parabens! aproveite seu " + Config.VIP_DAYS_FREE + " dias de VIP.");
			activeChar.sendMessage("[EN] Congratulations! enjoy your " + Config.VIP_DAYS_FREE + " VIP days.");
		}
		
		if (Config.ALLOW_VIP_NCOLOR && activeChar.isVip())
		{
			activeChar.getAppearance().setNameColor(Config.VIP_NCOLOR);
		}
		
		if (Config.ALLOW_VIP_TCOLOR && activeChar.isVip())
		{
			activeChar.getAppearance().setTitleColor(Config.VIP_TCOLOR);
		}
		
		activeChar.broadcastUserInfo();
		activeChar.sendPacket(new EtcStatusUpdate(activeChar));
		
	}
	
	private static void InsertNewMacro(Player player, int id, int icon, String name, String descr, String acronym, String command)
	{
		try (Connection con = ConnectionPool.getConnection(); PreparedStatement statement = con.prepareStatement("INSERT INTO character_macroses (char_obj_id, id, icon, name, descr, acronym, commands) VALUES (?, ?, ?, ?, ?, ?, ?)"))
		{
			
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, id);
			statement.setInt(3, icon);
			statement.setString(4, name);
			statement.setString(5, descr);
			statement.setString(6, acronym);
			statement.setString(7, command);
			statement.execute();
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}