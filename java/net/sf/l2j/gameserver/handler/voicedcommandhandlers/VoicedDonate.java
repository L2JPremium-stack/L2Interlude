package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.gameserver.ConnectionPool;
import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.datatables.CharNameTable;
import net.sf.l2j.gameserver.datatables.MultisellData;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.handler.admincommandhandlers.AdminEditChar;
import net.sf.l2j.gameserver.instancemanager.custom.HeroManagerCustom;
import net.sf.l2j.gameserver.model.L2Augmentation;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.base.Sex;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.itemcontainer.Inventory;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.clientpackets.RequestBypassToServer;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.EtcStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.HennaInfo;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.templates.StatsSet;

/**
 * @author Sarada
 *
 */
public class VoicedDonate implements IVoicedCommandHandler
{

	private static final String[] VOICED_COMMANDS =
	{
		//"multisell",
		Config.STRING_MULTISELL_DONATE,
		"donate",
		"others",
		"argument_weapon_active",
		"argument_weapon_passive",
		"argument_active",
		"argument_active_2",
		"htmlArgument",
		"argument_alternative",
		"vip_",
		"setvip_",
		"clan_services",
		"clan_lv",
		"clan_rep",
		"clan_skills",
		"name_",
		"name_select",
		"nobless",
		"CleanPk",
		"class_select",
		"class_",
		"setclass_",
		"sethero_",
		"hero_",
		"sex_",
		"setsex_",
		"setname_"
	
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if (command.startsWith("donate"))
		{
			showMainHtml(activeChar);
		}
		else if (command.startsWith("htmlArgument"))
		{
			showArgumentHtml(activeChar);
		}
		//VIP COMPRAS FIXED
		else if (command.startsWith("vip_"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			try
			{
				String type = st.nextToken();
				switch (type)
				{
					case "30_Days":
						activeChar._vip_days = 30;
						Buy_Vip(activeChar);
						break;
					case "60_Days":
						activeChar._vip_days = 60;
						Buy_Vip(activeChar);
						break;
					case "90_Days":
						activeChar._vip_days = 90;
						Buy_Vip(activeChar);
						break;
					case "Eternal":
						activeChar._vip_days = 9999;
						Buy_Vip(activeChar);
						break;
					default:
						activeChar.sendMessage("Please select another value!");
						showMainHtml(activeChar);
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("ERROR , CONTATE O ADMINISTRADOR");
			}
		}
		else if (command.startsWith("setvip_") && activeChar._vip_days == 30)
		{
			if(activeChar.isVip())
			{
				activeChar.sendMessage("You Are Vip!.");
				return true;
			}
			if (activeChar.destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.VIP_30_DAYS_PRICE, null, true))
			{
				if (activeChar.isVip())
				{
					long daysleft = (activeChar.getVipEndTime() - Calendar.getInstance().getTimeInMillis()) / 86400000L;
					activeChar.setEndTime("vip", (int)(daysleft + 30));
					activeChar.sendMessage("Congratulations, You just received another " + 30 + " day of VIP.");
				}
				else
				{
					activeChar.setVip(true);
					activeChar.setEndTime("vip", 30);
					activeChar.sendMessage("Congrats, you just became VIP per " + 30 + " day.");
				}
				
				if (Config.ALLOW_VIP_NCOLOR && activeChar.isVip())
				{
					activeChar.getAppearance().setNameColor(Config.VIP_NCOLOR);
					activeChar.broadcastUserInfo();
				}
					
				
				if (Config.ALLOW_VIP_TCOLOR && activeChar.isVip()) 
				{
					activeChar.getAppearance().setTitleColor(Config.VIP_TCOLOR);
					activeChar.broadcastUserInfo();
				}
				
				activeChar.broadcastUserInfo();
				activeChar.sendPacket(new EtcStatusUpdate(activeChar));
			}
			else
			{
				Incorrect_item(activeChar);
			}
		}
		else if (command.startsWith("setvip_") && activeChar._vip_days == 60)
		{
			if(activeChar.isVip())
			{
				activeChar.sendMessage("You Are Vip!.");
				return true;
			}
			if (activeChar.destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.VIP_60_DAYS_PRICE, null, true))
			{
				if (activeChar.isVip())
				{
					long daysleft = (activeChar.getVipEndTime() - Calendar.getInstance().getTimeInMillis()) / 86400000L;
					activeChar.setEndTime("vip", (int)(daysleft + 60));
					activeChar.sendMessage("Congratulations, You just received another " + 60 + " day of VIP.");
				}
				else
				{
					activeChar.setVip(true);
					activeChar.setEndTime("vip", 60);
					activeChar.sendMessage("Congrats, you just became VIP per " + 60 + " day.");
				}
				
				if (Config.ALLOW_VIP_NCOLOR && activeChar.isVip())
				{
					activeChar.getAppearance().setNameColor(Config.VIP_NCOLOR);
					activeChar.broadcastUserInfo();
				}
					
				
				if (Config.ALLOW_VIP_TCOLOR && activeChar.isVip()) 
				{
					activeChar.getAppearance().setTitleColor(Config.VIP_TCOLOR);
					activeChar.broadcastUserInfo();
				}
				
				activeChar.broadcastUserInfo();
				activeChar.sendPacket(new EtcStatusUpdate(activeChar));
			}
			else
			{
				Incorrect_item(activeChar);
			}
		}
		else if (command.startsWith("setvip_") && activeChar._vip_days == 90)
		{
			if(activeChar.isVip())
			{
				activeChar.sendMessage("You Are Vip!.");
				return true;
			}
			if (activeChar.destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.VIP_90_DAYS_PRICE, null, true))
			{
				if (activeChar.isVip())
				{
					long daysleft = (activeChar.getVipEndTime() - Calendar.getInstance().getTimeInMillis()) / 86400000L;
					activeChar.setEndTime("vip", (int)(daysleft + 90));
					activeChar.sendMessage("Congratulations, You just received another " + 90 + " day of VIP.");
				}
				else
				{
					activeChar.setVip(true);
					activeChar.setEndTime("vip", 90);
					activeChar.sendMessage("Congrats, you just became VIP per " + 90 + " day.");
				}
				
				if (Config.ALLOW_VIP_NCOLOR && activeChar.isVip())
				{
					activeChar.getAppearance().setNameColor(Config.VIP_NCOLOR);
					activeChar.broadcastUserInfo();
				}
					
				
				if (Config.ALLOW_VIP_TCOLOR && activeChar.isVip()) 
				{
					activeChar.getAppearance().setTitleColor(Config.VIP_TCOLOR);
					activeChar.broadcastUserInfo();
				}
				
				activeChar.broadcastUserInfo();
				activeChar.sendPacket(new EtcStatusUpdate(activeChar));
			}
			else
			{
				Incorrect_item(activeChar);
			}
		}
		else if (command.startsWith("setvip_") && activeChar._vip_days == 9999)
		{
			if(activeChar.isVip())
			{
				activeChar.sendMessage("You Are Vip!.");
				return true;
			}
			if (activeChar.destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.VIP_ETERNAL_PRICE, null, true))
			{
				if (activeChar.isVip())
				{
					long daysleft = (activeChar.getVipEndTime() - Calendar.getInstance().getTimeInMillis()) / 86400000L;
					activeChar.setEndTime("vip", (int)(daysleft + 9999));
					activeChar.sendMessage("Congratulations, You just received another " + 9999 + " day of VIP.");
				}
				else
				{
					activeChar.setVip(true);
					activeChar.setEndTime("vip", 9999);
					activeChar.sendMessage("Congrats, you just became VIP per " + 9999 + " day.");
				}
				
				if (Config.ALLOW_VIP_NCOLOR && activeChar.isVip())
				{
					activeChar.getAppearance().setNameColor(Config.VIP_NCOLOR);
					activeChar.broadcastUserInfo();
				}
					
				
				if (Config.ALLOW_VIP_TCOLOR && activeChar.isVip()) 
				{
					activeChar.getAppearance().setTitleColor(Config.VIP_TCOLOR);
					activeChar.broadcastUserInfo();
				}
				
				activeChar.broadcastUserInfo();
				activeChar.sendPacket(new EtcStatusUpdate(activeChar));
			}
			else
			{
				Incorrect_item(activeChar);
			}
		}
		
		else if (command.startsWith("hero_"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			try
			{
				String type = st.nextToken();
				switch (type)
				{
					case "1_Days":
						activeChar._hero_days = 1;
						Buy_Hero(activeChar);
						break;
					case "30_Days":
						activeChar._hero_days = 30;
						Buy_Hero(activeChar);
						break;
					case "60_Days":
						activeChar._hero_days = 60;
						Buy_Hero(activeChar);
						break;
					case "90_Days":
						activeChar._hero_days = 90;
						Buy_Hero(activeChar);
						break;
					case "Eternal":
						activeChar._hero_days = 999;
						Buy_Hero(activeChar);
						break;
					default:
						activeChar.sendMessage("Please select another value!");
						showMainHtml(activeChar);
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("ERROR , CONTATE O ADMINISTRADOR");
			}
		}
		else if (command.startsWith("sethero_"))
		{
			if(activeChar.isHero())
			{
				activeChar.sendMessage("You Are Hero!.");
				return true;
			}
			if (activeChar._hero_days == 30)
			{
				if (activeChar.destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.HERO_30_DAYS_PRICE, null, true))
				{
					if (HeroManagerCustom.getInstance().hasHeroPrivileges(activeChar.getObjectId()))
					{
						long now = Calendar.getInstance().getTimeInMillis();
						long duration = HeroManagerCustom.getInstance().getHeroDuration(activeChar.getObjectId());
						long endDay = duration;
						
						long _daysleft = (endDay - now) / 86400000L + activeChar._hero_days + 1L;
						
						Calendar calendar = Calendar.getInstance();
						if (_daysleft >= 30L)
						{
							while (_daysleft >= 30L)
							{
								if (calendar.get(2) == 11)
								{
									calendar.roll(1, true);
								}
								calendar.roll(2, true);
								_daysleft -= 30L;
							}
						}
						if ((_daysleft < 30L) && (_daysleft > 0L))
						{
							while (_daysleft > 0L)
							{
								if ((calendar.get(5) == 28) && (calendar.get(2) == 1))
								{
									calendar.roll(2, true);
								}
								if (calendar.get(5) == 30)
								{
									if (calendar.get(2) == 11)
									{
										calendar.roll(1, true);
									}
									calendar.roll(2, true);
								}
								calendar.roll(5, true);
								_daysleft -= 1L;
							}
						}
						long end_day = calendar.getTimeInMillis();
						HeroManagerCustom.getInstance().updateHero(activeChar.getObjectId(), end_day);
					}
					else
					{
						Calendar calendar = Calendar.getInstance();
						if (activeChar._hero_days >= 30)
						{
							while (activeChar._hero_days >= 30)
							{
								if (calendar.get(2) == 11)
								{
									calendar.roll(1, true);
								}
								calendar.roll(2, true);
								activeChar._hero_days -= 30;
							}
						}
						if ((activeChar._hero_days < 30) && (activeChar._hero_days > 0))
						{
							while (activeChar._hero_days > 0)
							{
								if ((calendar.get(5) == 28) && (calendar.get(2) == 1))
								{
									calendar.roll(2, true);
								}
								if (calendar.get(5) == 30)
								{
									if (calendar.get(2) == 11)
									{
										calendar.roll(1, true);
									}
									calendar.roll(2, true);
								}
								calendar.roll(5, true);
								activeChar._hero_days -= 1;
							}
						}
						long end_day = calendar.getTimeInMillis();
						HeroManagerCustom.getInstance().addHero(activeChar.getObjectId(), end_day);
					}
					long now = Calendar.getInstance().getTimeInMillis();
					long duration = HeroManagerCustom.getInstance().getHeroDuration(activeChar.getObjectId());
					long endDay = duration;
					long _daysleft = (endDay - now) / 86400000L;
					if (_daysleft < 270L)
					{
						activeChar.sendMessage("Your Hero privileges ends at " + new SimpleDateFormat("dd MMM, HH:mm").format(new Date(duration)) + ".");
					}
				}
				else
				{
					Incorrect_item(activeChar);
				}
			}
			else if (activeChar._hero_days == 60)
			{
				if (activeChar.destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.HERO_60_DAYS_PRICE, null, true))
				{
					if (HeroManagerCustom.getInstance().hasHeroPrivileges(activeChar.getObjectId()))
					{
						long now = Calendar.getInstance().getTimeInMillis();
						long duration = HeroManagerCustom.getInstance().getHeroDuration(activeChar.getObjectId());
						long endDay = duration;
						
						long _daysleft = (endDay - now) / 86400000L + activeChar._hero_days + 1L;
						
						Calendar calendar = Calendar.getInstance();
						if (_daysleft >= 30L)
						{
							while (_daysleft >= 30L)
							{
								if (calendar.get(2) == 11)
								{
									calendar.roll(1, true);
								}
								calendar.roll(2, true);
								_daysleft -= 30L;
							}
						}
						if ((_daysleft < 30L) && (_daysleft > 0L))
						{
							while (_daysleft > 0L)
							{
								if ((calendar.get(5) == 28) && (calendar.get(2) == 1))
								{
									calendar.roll(2, true);
								}
								if (calendar.get(5) == 30)
								{
									if (calendar.get(2) == 11)
									{
										calendar.roll(1, true);
									}
									calendar.roll(2, true);
								}
								calendar.roll(5, true);
								_daysleft -= 1L;
							}
						}
						long end_day = calendar.getTimeInMillis();
						HeroManagerCustom.getInstance().updateHero(activeChar.getObjectId(), end_day);
					}
					else
					{
						Calendar calendar = Calendar.getInstance();
						if (activeChar._hero_days >= 30)
						{
							while (activeChar._hero_days >= 30)
							{
								if (calendar.get(2) == 11)
								{
									calendar.roll(1, true);
								}
								calendar.roll(2, true);
								activeChar._hero_days -= 30;
							}
						}
						if ((activeChar._hero_days < 30) && (activeChar._hero_days > 0))
						{
							while (activeChar._hero_days > 0)
							{
								if ((calendar.get(5) == 28) && (calendar.get(2) == 1))
								{
									calendar.roll(2, true);
								}
								if (calendar.get(5) == 30)
								{
									if (calendar.get(2) == 11)
									{
										calendar.roll(1, true);
									}
									calendar.roll(2, true);
								}
								calendar.roll(5, true);
								activeChar._hero_days -= 1;
							}
						}
						long end_day = calendar.getTimeInMillis();
						HeroManagerCustom.getInstance().addHero(activeChar.getObjectId(), end_day);
					}
					long now = Calendar.getInstance().getTimeInMillis();
					long duration = HeroManagerCustom.getInstance().getHeroDuration(activeChar.getObjectId());
					long endDay = duration;
					long _daysleft = (endDay - now) / 86400000L;
					if (_daysleft < 270L)
					{
						activeChar.sendMessage("Your Hero privileges ends at " + new SimpleDateFormat("dd MMM, HH:mm").format(new Date(duration)) + ".");
					}
				}
				else
				{
					Incorrect_item(activeChar);
				}
			}
			else if (activeChar._hero_days == 90)
			{
				if (activeChar.destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.HERO_90_DAYS_PRICE, null, true))
				{
					if (HeroManagerCustom.getInstance().hasHeroPrivileges(activeChar.getObjectId()))
					{
						long now = Calendar.getInstance().getTimeInMillis();
						long duration = HeroManagerCustom.getInstance().getHeroDuration(activeChar.getObjectId());
						long endDay = duration;
						
						long _daysleft = (endDay - now) / 86400000L + activeChar._hero_days + 1L;
						
						Calendar calendar = Calendar.getInstance();
						if (_daysleft >= 30L)
						{
							while (_daysleft >= 30L)
							{
								if (calendar.get(2) == 11)
								{
									calendar.roll(1, true);
								}
								calendar.roll(2, true);
								_daysleft -= 30L;
							}
						}
						if ((_daysleft < 30L) && (_daysleft > 0L))
						{
							while (_daysleft > 0L)
							{
								if ((calendar.get(5) == 28) && (calendar.get(2) == 1))
								{
									calendar.roll(2, true);
								}
								if (calendar.get(5) == 30)
								{
									if (calendar.get(2) == 11)
									{
										calendar.roll(1, true);
									}
									calendar.roll(2, true);
								}
								calendar.roll(5, true);
								_daysleft -= 1L;
							}
						}
						long end_day = calendar.getTimeInMillis();
						HeroManagerCustom.getInstance().updateHero(activeChar.getObjectId(), end_day);
					}
					else
					{
						Calendar calendar = Calendar.getInstance();
						if (activeChar._hero_days >= 30)
						{
							while (activeChar._hero_days >= 30)
							{
								if (calendar.get(2) == 11)
								{
									calendar.roll(1, true);
								}
								calendar.roll(2, true);
								activeChar._hero_days -= 30;
							}
						}
						if ((activeChar._hero_days < 30) && (activeChar._hero_days > 0))
						{
							while (activeChar._hero_days > 0)
							{
								if ((calendar.get(5) == 28) && (calendar.get(2) == 1))
								{
									calendar.roll(2, true);
								}
								if (calendar.get(5) == 30)
								{
									if (calendar.get(2) == 11)
									{
										calendar.roll(1, true);
									}
									calendar.roll(2, true);
								}
								calendar.roll(5, true);
								activeChar._hero_days -= 1;
							}
						}
						long end_day = calendar.getTimeInMillis();
						HeroManagerCustom.getInstance().addHero(activeChar.getObjectId(), end_day);
					}
					long now = Calendar.getInstance().getTimeInMillis();
					long duration = HeroManagerCustom.getInstance().getHeroDuration(activeChar.getObjectId());
					long endDay = duration;
					long _daysleft = (endDay - now) / 86400000L;
					if (_daysleft < 270L)
					{
						activeChar.sendMessage("Your Hero privileges ends at " + new SimpleDateFormat("dd MMM, HH:mm").format(new Date(duration)) + ".");
					}
				}
				else
				{
					Incorrect_item(activeChar);
				}
			}
			else if (activeChar._hero_days == 999)
			{
				if (activeChar.destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.HERO_ETERNAL_PRICE, null, true))
				{
					if (HeroManagerCustom.getInstance().hasHeroPrivileges(activeChar.getObjectId()))
					{
						long now = Calendar.getInstance().getTimeInMillis();
						long duration = HeroManagerCustom.getInstance().getHeroDuration(activeChar.getObjectId());
						long endDay = duration;
						
						long _daysleft = (endDay - now) / 86400000L + activeChar._hero_days + 1L;
						
						Calendar calendar = Calendar.getInstance();
						if (_daysleft >= 30L)
						{
							while (_daysleft >= 30L)
							{
								if (calendar.get(2) == 11)
								{
									calendar.roll(1, true);
								}
								calendar.roll(2, true);
								_daysleft -= 30L;
							}
						}
						if ((_daysleft < 30L) && (_daysleft > 0L))
						{
							while (_daysleft > 0L)
							{
								if ((calendar.get(5) == 28) && (calendar.get(2) == 1))
								{
									calendar.roll(2, true);
								}
								if (calendar.get(5) == 30)
								{
									if (calendar.get(2) == 11)
									{
										calendar.roll(1, true);
									}
									calendar.roll(2, true);
								}
								calendar.roll(5, true);
								_daysleft -= 1L;
							}
						}
						long end_day = calendar.getTimeInMillis();
						HeroManagerCustom.getInstance().updateHero(activeChar.getObjectId(), end_day);
					}
					else
					{
						Calendar calendar = Calendar.getInstance();
						if (activeChar._hero_days >= 30)
						{
							while (activeChar._hero_days >= 30)
							{
								if (calendar.get(2) == 11)
								{
									calendar.roll(1, true);
								}
								calendar.roll(2, true);
								activeChar._hero_days -= 30;
							}
						}
						if ((activeChar._hero_days < 30) && (activeChar._hero_days > 0))
						{
							while (activeChar._hero_days > 0)
							{
								if ((calendar.get(5) == 28) && (calendar.get(2) == 1))
								{
									calendar.roll(2, true);
								}
								if (calendar.get(5) == 30)
								{
									if (calendar.get(2) == 11)
									{
										calendar.roll(1, true);
									}
									calendar.roll(2, true);
								}
								calendar.roll(5, true);
								activeChar._hero_days -= 1;
							}
						}
						long end_day = calendar.getTimeInMillis();
						HeroManagerCustom.getInstance().addHero(activeChar.getObjectId(), end_day);
					}
					activeChar.sendPacket(new ExShowScreenMessage("Congratulations! You have activated Eternal Hero.", 10000));
					activeChar.sendMessage("Congratulations! You have activated Eternal Hero.");
				}
				else
				{
					Incorrect_item(activeChar);
				}
			}
			else
			{
				activeChar.sendMessage("ERROR , CONTATE O ADMINISTRADOR");
			}
		}
		else if (command.startsWith("argument_alternative"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			try
			{
				String type = st.nextToken();
				switch (type)
				{
					case "buy_str":
						augmentsAlternative(activeChar, 1070930999, 0, 0);
						break;
					case "buy_int":
						augmentsAlternative(activeChar, 1071062077, 0, 0);
						break;
					case "buy_men":
						augmentsAlternative(activeChar, 1071123995, 0, 0);
						break;
					case "buy_con":
						augmentsAlternative(activeChar, 1070992947, 0, 0);
						break;
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage : Bar>");
			}
		}
			/*else if (command.startsWith("buy_str"))
			{
				augments(activeChar, 1070930999, 0, 0);
				return false;
			}
			else if (command.startsWith("buy_int"))
			{
				augments(activeChar, 1071062077, 0, 0);
				return false;
			}
			else if (command.startsWith("buy_men"))
			{
				augments(activeChar, 1071123995, 0, 0);
				return false;
			}
			else if (command.startsWith("buy_con"))
			{
				augments(activeChar, 1070992947, 0, 0);
				return false;
			}*/
		else if (command.startsWith("argument_active_2"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			try
			{
				String type = st.nextToken();
				switch (type)
				{
					case "DuelMight":
						augments(activeChar, 1067260101, 3243, 10);
						break;
					case "Might":
						augments(activeChar, 1067125363, 3240, 10);
						break;
					case "Shield":
						augments(activeChar, 1067194549, 3244, 10);
						break;
					case "MagicBarrier":
						augments(activeChar, 962068481, 3245, 10);
						break;
					case "Empower":
						augments(activeChar, 1066994296, 3241, 10);
						break;
					case "Agility":
						augments(activeChar, 965279745, 3247, 10);
						break;
					case "Guidance":
						augments(activeChar, 1070537767, 3248, 10);
						break;
					case "Focus":
						augments(activeChar, 1070406728, 3249, 10);
						break;
					case "WildMagic":
						augments(activeChar, 1070599653, 3250, 10);
						break;
					case "ReflectDamage":
						augments(activeChar, 1070472227, 3259, 3);
						break;
					case "HealEmpower":
						augments(activeChar, 1066866909, 3246, 10);
						break;
					case "Prayer":
						augments(activeChar, 1066932422, 3238, 10);
						break;
					
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage : Bar>");
			}
		}
		else if (command.startsWith("argument_active"))
		{
			StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			try
			{
				String type = st.nextToken();
				switch (type)
				{
					case "Might":
						augments(activeChar, 1062079106, 3132, 10);
						break;
					case "Empower":
						augments(activeChar, 1061423766, 3133, 10);
						break;
					case "DuelMight":
						augments(activeChar, 1062406807, 3134, 10);
						break;
					case "Shield":
						augments(activeChar, 968884225, 3135, 10);
						break;
					case "MagicBarrier":
						augments(activeChar, 956760065, 3136, 10);
						break;
					case "WildMagic":
						augments(activeChar, 1067850844, 3142, 10);
						break;
					case "Focus":
						augments(activeChar, 1067523168, 3141, 10);
						break;
					case "BattleRoad":
						augments(activeChar, 968228865, 3125, 10);
						break;
					case "BlessedBody":
						augments(activeChar, 991625216, 3124, 10);
						break;
					case "Agility":
						augments(activeChar, 1060444351, 3139, 10);
						break;
					case "Heal":
						augments(activeChar, 1061361888, 3123, 10);
						break;
					case "HydroBlast":
						augments(activeChar, 1063590051, 3167, 10);
						break;
					case "AuraFlare":
						augments(activeChar, 1063455338, 3172, 10);
						break;
					case "Hurricane":
						augments(activeChar, 1064108032, 3168, 10);
						break;
					case "ReflectDamage":
						augments(activeChar, 1067588698, 3204, 3);
						break;
					case "Celestial":
						augments(activeChar, 974454785, 3158, 1);
						break;
					case "Guidance":
						augments(activeChar, 974454785, 3140, 10);
						break;
					case "Stone":
						augments(activeChar, 1060640984, 3169, 10);
						break;
					case "HealEmpower":
						augments(activeChar, 1061230760, 3138, 10);
						break;
					case "ShadowFlare":
						augments(activeChar, 1063520931, 3171, 10);
						break;
					case "Prominence":
						augments(activeChar, 1063327898, 3165, 10);
						break;
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage : Bar>");
			}
		}
			
	
		else if (command.startsWith("argument_weapon_active"))
		{
			showArgWeaponActive(activeChar);
		}
		else if (command.startsWith("argument_weapon_passive"))
		{
			showArgWeaponPassive(activeChar);
		}
		else if (command.startsWith("clan_skills"))
		{
			if (!activeChar.isClanLeader())
			{
				activeChar.sendMessage("Only a clan leader can add clan skills.!");
				showClanServices(activeChar);
				return false;
			}
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, -1) < Config.CLAN_SKILL_ITEM_COUNT)
			{
				activeChar.sendMessage("You do not have enough Donate Coins.");
				showClanServices(activeChar);
				return false;
			}
			activeChar.getClan().addNewSkill(SkillTable.getInstance().getInfo(370, 3));
			activeChar.getClan().addNewSkill(SkillTable.getInstance().getInfo(371, 3));
			activeChar.getClan().addNewSkill(SkillTable.getInstance().getInfo(372, 3));
			activeChar.getClan().addNewSkill(SkillTable.getInstance().getInfo(373, 3));
			activeChar.getClan().addNewSkill(SkillTable.getInstance().getInfo(374, 3));
			activeChar.getClan().addNewSkill(SkillTable.getInstance().getInfo(375, 3));
			activeChar.getClan().addNewSkill(SkillTable.getInstance().getInfo(376, 3));
			activeChar.getClan().addNewSkill(SkillTable.getInstance().getInfo(377, 3));
			activeChar.getClan().addNewSkill(SkillTable.getInstance().getInfo(378, 3));
			activeChar.getClan().addNewSkill(SkillTable.getInstance().getInfo(379, 3));
			activeChar.getClan().addNewSkill(SkillTable.getInstance().getInfo(380, 3));
			activeChar.getClan().addNewSkill(SkillTable.getInstance().getInfo(381, 3));
			activeChar.getClan().addNewSkill(SkillTable.getInstance().getInfo(382, 3));
			activeChar.getClan().addNewSkill(SkillTable.getInstance().getInfo(383, 3));
			activeChar.getClan().addNewSkill(SkillTable.getInstance().getInfo(384, 3));
			activeChar.getClan().addNewSkill(SkillTable.getInstance().getInfo(385, 3));
			activeChar.getClan().addNewSkill(SkillTable.getInstance().getInfo(386, 3));
			activeChar.getClan().addNewSkill(SkillTable.getInstance().getInfo(387, 3));
			activeChar.getClan().addNewSkill(SkillTable.getInstance().getInfo(388, 3));
			activeChar.getClan().addNewSkill(SkillTable.getInstance().getInfo(389, 3));
			activeChar.getClan().addNewSkill(SkillTable.getInstance().getInfo(390, 3));
			activeChar.getClan().addNewSkill(SkillTable.getInstance().getInfo(391, 1));
			activeChar.destroyItemByItemId("Consume", Config.DONATE_COIN_ID, Config.CLAN_SKILL_ITEM_COUNT, activeChar, true);
			showClanServices(activeChar);
		}
		else if (command.startsWith("clan_rep"))
		{
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, -1) < Config.CLAN_REP_ITEM_COUNT)
			{
				activeChar.sendMessage("You do not have enough Donate Coins.");
				showClanServices(activeChar);
				return false;
			}
			if (activeChar.getClan() == null)
			{
				activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
				showClanServices(activeChar);
				return false;
			}
			if (!activeChar.isClanLeader())
			{
				activeChar.sendPacket(SystemMessageId.NOT_AUTHORIZED_TO_BESTOW_RIGHTS);
				showClanServices(activeChar);
				return false;
			}
			activeChar.destroyItemByItemId("Consume", Config.DONATE_COIN_ID, Config.CLAN_REP_ITEM_COUNT, activeChar, true);
			activeChar.getClan().addReputationScore(Config.CLAN_REPS);
			activeChar.getClan().broadcastClanStatus();
			activeChar.sendMessage("Your clan reputation score has been increased.");
			showClanServices(activeChar);
		}
		else if (command.startsWith("clan_lv"))
		{
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, -1) < Config.CLAN_ITEM_COUNT)
			{
				activeChar.sendMessage("You do not have enough Donate Coins.");
				showClanServices(activeChar);
				return false;
			}
			else if (activeChar.getClan() == null)
			{
				activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
				showClanServices(activeChar);
				return false;
			}
			if (!activeChar.isClanLeader())
			{
				activeChar.sendPacket(SystemMessageId.NOT_AUTHORIZED_TO_BESTOW_RIGHTS);
				showClanServices(activeChar);
				return false;
			}
			if (activeChar.getClan().getLevel() == 8)
			{
				activeChar.sendMessage("Your clan is already level 8.");
				showClanServices(activeChar);
				return false;
			}
			activeChar.getClan().changeLevel(activeChar.getClan().getLevel() + 1);
			activeChar.getClan().broadcastClanStatus();
			activeChar.broadcastPacket(new MagicSkillUse(activeChar, activeChar, 5103, 1, 1000, 0));
			activeChar.destroyItemByItemId("Consume", Config.DONATE_COIN_ID, Config.CLAN_ITEM_COUNT, activeChar, true);
			showClanServices(activeChar);
		}
		else if (command.startsWith("clan_services"))
		{
			showClanServices(activeChar);
		}
		else if (command.startsWith("others"))
		{
			showOthersHtml(activeChar);
		}
		else if (command.startsWith("CleanPk"))
		{
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, -1) < Config.PK_ITEM_COUNT)
			{
				activeChar.sendMessage("You do not have enough Donate Coins.");
				showOthersHtml(activeChar);
				return false;
			}
			if (activeChar.getPkKills() < 50)
			{
				activeChar.sendMessage("You do not have enough Pk kills for clean.");
				showOthersHtml(activeChar);
				return false;
			}
			activeChar.destroyItemByItemId("Consume", Config.DONATE_COIN_ID, Config.PK_ITEM_COUNT, activeChar, true);
			activeChar.setPkKills(activeChar.getPkKills() - Config.PK_CLEAN);
			activeChar.sendMessage("You have successfully clean " + Config.PK_CLEAN + " pks!");
			activeChar.broadcastUserInfo();
			showMainHtml(activeChar);
		}
		else if (command.startsWith("nobless"))
		{
			if (activeChar.isNoble())
			{
				activeChar.sendMessage("You Are Already A Noblesse!.");
				showOthersHtml(activeChar);
				return false;
			}
			if (activeChar.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, -1) < Config.NOBL_ITEM_COUNT)
			{
				activeChar.sendMessage("You do not have enough Donate Coins.");
				showOthersHtml(activeChar);
				return false;
			}
			activeChar.getInventory().addItem("Noblesse Tiara", 7694, 1, activeChar, null);
			activeChar.destroyItemByItemId("Consume", Config.DONATE_COIN_ID, Config.NOBL_ITEM_COUNT, activeChar, true);
			activeChar.setNoble(true, true);
			activeChar.broadcastPacket(new SocialAction(activeChar, 16));
			activeChar.sendMessage("You Are Now a Noble,You Are Granted With Noblesse Status , And Noblesse Skills.");
			activeChar.broadcastUserInfo();
			showMainHtml(activeChar);
		}
		
		/*else if (command.startsWith("multisell"))
		{
			try
			{
				activeChar.setIsUsingCMultisell(true);
				MultisellData.getInstance().separateAndSend(command.substring(9).trim(), activeChar, null, false);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("This list does not exist.");
			}
		}*/
		else if (command.startsWith(Config.STRING_MULTISELL_DONATE))
		{
			try
			{
				activeChar.setIsUsingCMultisell(true);
				MultisellData.getInstance().separateAndSend(command.substring(12).trim(), activeChar, null, false);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("This list does not exist.");
			}
		}
			else if (command.startsWith("name_select"))
				name_select(activeChar);
			else if (command.startsWith("name_"))
			{
				StringTokenizer st = new StringTokenizer(command);
				st.nextToken();
				try
				{
					String name = st.nextToken();
					
					activeChar._change_Name = name;
					
					if (activeChar._change_Name.length() > 16)
					{
						activeChar.sendMessage("The chosen name cannot exceed 16 characters in length.");
						showMainHtml(activeChar);
						return false;
					}
					else if (activeChar._change_Name.length() < 3)
					{
						activeChar.sendMessage("Your name can not be mention that 3 characters in length.");
						showMainHtml(activeChar);
						return false;
					}
					else if (!StringUtil.isValidPlayerName(activeChar._change_Name))
					{
						activeChar.sendMessage("The new name doesn't fit with the regex pattern.");
						showMainHtml(activeChar);
						return false;
					}
					//else if (CharNameTable.getInstance().getPlayerObjectId(activeChar._change_Name) > 0)
					else if (CharNameTable.getInstance().getIdByName(activeChar._change_Name) > 0)
					{
						activeChar.sendMessage("The chosen name already exists.");
						showMainHtml(activeChar);
						return false;
					}
					
					name_finish(activeChar);
				}
				catch (Exception e)
				{
					activeChar.sendMessage("ERROR , CONTACT STAFF.");
				}
			}
			else if (command.startsWith("class_select"))
				class_select(activeChar);
			else if (command.startsWith("class_"))
			{
				StringTokenizer st = new StringTokenizer(command);
				st.nextToken();
				try
				{
					String type = st.nextToken();
					activeChar._class_id = Integer.parseInt(type);
					class_finish(activeChar);
				}
				catch (Exception e)
				{
					activeChar.sendMessage("ERROR , CONTACT STAFF.");
				}
			}
			else if (command.startsWith("setclass_"))
			{
				activeChar.abortAttack();
				activeChar.abortCast();
				
				if (activeChar.isInCombat())
				{
					activeChar.sendMessage("You can not be in combat mode..");			
					return false;
				}

				if (activeChar.getBaseClass() != activeChar.getClassId().getId())
				{
					activeChar.sendMessage("You is not with its base class.");
					activeChar.sendPacket(new ExShowScreenMessage("You is not with its base class.", 6000, 0x02, true));
					return false;
				}
				else if (activeChar.isInOlympiadMode())
				{
					activeChar.sendMessage("This Item Cannot Be Used On Olympiad Games.");
					return false;
				}
				else if (activeChar.destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.DONATE_CLASS_PRICE, null, true))
				{
					String nameclasse = activeChar.getTemplate().getClassName();
					if (activeChar._class_id == 1)
					{
						if (activeChar.getClassId().getId() == 88)
						{
							activeChar.sendMessage("Sorry you're already in the " + nameclasse + " class");
							return false;
						}
						
						//for (L2Skill skill : activeChar.getSkills().values())
						//	activeChar.removeSkill(skill.getId(), true);
						RequestBypassToServer.RemoverSkills(activeChar);
						
						activeChar.setClassId(88);
						
						if (!activeChar.isSubClassActive())
							activeChar.setBaseClass(88);
						
						Finish(activeChar);
					}
					else if (activeChar._class_id == 2)
					{
						if (activeChar.getClassId().getId() == 89)
						{
							activeChar.sendMessage("Sorry you're already in the " + nameclasse + " class");
							return false;
						}
						
					//	for (L2Skill skill : activeChar.getSkills().values())
					//		activeChar.removeSkill(skill.getId(), true);
						RequestBypassToServer.RemoverSkills(activeChar);
						
						activeChar.setClassId(89);
						
						if (!activeChar.isSubClassActive())
							activeChar.setBaseClass(89);
						
						Finish(activeChar);
					}
					else if (activeChar._class_id == 3)
					{
						if (activeChar.getClassId().getId() == 90)
						{
							activeChar.sendMessage("Sorry you're already in the " + nameclasse + " class");
							return false;
						}
						
					//	for (L2Skill skill : activeChar.getSkills().values())
					//		activeChar.removeSkill(skill.getId(), true);
						RequestBypassToServer.RemoverSkills(activeChar);
						
						activeChar.setClassId(90);
						
						if (!activeChar.isSubClassActive())
							activeChar.setBaseClass(90);
						
						Finish(activeChar);
					}
					else if (activeChar._class_id == 4)
					{
						if (activeChar.getClassId().getId() == 91)
						{
							activeChar.sendMessage("Sorry you're already in the " + nameclasse + " class");
							return false;
						}
						
					//	for (L2Skill skill : activeChar.getSkills().values())
					//		activeChar.removeSkill(skill.getId(), true);
						RequestBypassToServer.RemoverSkills(activeChar);
						
						activeChar.setClassId(91);
						
						if (!activeChar.isSubClassActive())
							activeChar.setBaseClass(91);
						
						Finish(activeChar);
					}
					else if (activeChar._class_id == 5)
					{
						if (activeChar.getClassId().getId() == 92)
						{
							activeChar.sendMessage("Sorry you're already in the " + nameclasse + " class");
							return false;
						}
						
						//for (L2Skill skill : activeChar.getSkills().values())
						//	activeChar.removeSkill(skill.getId(), true);
						RequestBypassToServer.RemoverSkills(activeChar);
						
						activeChar.setClassId(92);
						
						if (!activeChar.isSubClassActive())
							activeChar.setBaseClass(92);
						
						Finish(activeChar);
					}
					else if (activeChar._class_id == 6)
					{
						if (activeChar.getClassId().getId() == 93)
						{
							activeChar.sendMessage("Sorry you're already in the " + nameclasse + " class");
							return false;
						}
						
					//	for (L2Skill skill : activeChar.getSkills().values())
					//		activeChar.removeSkill(skill.getId(), true);
						RequestBypassToServer.RemoverSkills(activeChar);
						
						activeChar.setClassId(93);
						
						if (!activeChar.isSubClassActive())
							activeChar.setBaseClass(93);
						
						Finish(activeChar);
					}
					else if (activeChar._class_id == 7)
					{
						if (activeChar.getClassId().getId() == 94)
						{
							activeChar.sendMessage("Sorry you're already in the " + nameclasse + " class");
							return false;
						}
						
					//	for (L2Skill skill : activeChar.getSkills().values())
					//		activeChar.removeSkill(skill.getId(), true);
						RequestBypassToServer.RemoverSkills(activeChar);
						
						activeChar.setClassId(94);
						
						if (!activeChar.isSubClassActive())
							activeChar.setBaseClass(94);
						
						Finish(activeChar);
					}
					else if (activeChar._class_id == 8)
					{
						if (activeChar.getClassId().getId() == 95)
						{
							activeChar.sendMessage("Sorry you're already in the " + nameclasse + " class");
							return false;
						}
						
					//	for (L2Skill skill : activeChar.getSkills().values())
					//		activeChar.removeSkill(skill.getId(), true);
						RequestBypassToServer.RemoverSkills(activeChar);
						
						activeChar.setClassId(95);
						
						if (!activeChar.isSubClassActive())
							activeChar.setBaseClass(95);
						
						Finish(activeChar);
					}
					else if (activeChar._class_id == 9)
					{
						if (activeChar.getClassId().getId() == 96)
						{
							activeChar.sendMessage("Sorry you're already in the " + nameclasse + " class");
							return false;
						}
						
				//		for (L2Skill skill : activeChar.getSkills().values())
				//			activeChar.removeSkill(skill.getId(), true);
						RequestBypassToServer.RemoverSkills(activeChar);
						
						activeChar.setClassId(96);
						
						if (!activeChar.isSubClassActive())
							activeChar.setBaseClass(96);
						
						Finish(activeChar);
					}
					else if (activeChar._class_id == 10)
					{
						if (activeChar.getClassId().getId() == 97)
						{
							activeChar.sendMessage("Sorry you're already in the " + nameclasse + " class");
							return false;
						}
						
					//	for (L2Skill skill : activeChar.getSkills().values())
					//		activeChar.removeSkill(skill.getId(), true);
						RequestBypassToServer.RemoverSkills(activeChar);
						
						activeChar.setClassId(97);
						
						if (!activeChar.isSubClassActive())
							activeChar.setBaseClass(97);
						
						Finish(activeChar);
					}
					else if (activeChar._class_id == 11)
					{
						if (activeChar.getClassId().getId() == 98)
						{
							activeChar.sendMessage("Sorry you're already in the " + nameclasse + " class");
							return false;
						}
						
					//	for (L2Skill skill : activeChar.getSkills().values())
						//	activeChar.removeSkill(skill.getId(), true);
						RequestBypassToServer.RemoverSkills(activeChar);
						
						activeChar.setClassId(98);
						
						if (!activeChar.isSubClassActive())
							activeChar.setBaseClass(98);
						
						Finish(activeChar);
					}
					else if (activeChar._class_id == 12)
					{
						if (activeChar.getClassId().getId() == 99)
						{
							activeChar.sendMessage("Sorry you're already in the " + nameclasse + " class");
							return false;
						}
						
					//	for (L2Skill skill : activeChar.getSkills().values())
					//		activeChar.removeSkill(skill.getId(), true);
						RequestBypassToServer.RemoverSkills(activeChar);
						
						activeChar.setClassId(99);
						
						if (!activeChar.isSubClassActive())
							activeChar.setBaseClass(99);
						
						Finish(activeChar);
					}
					else if (activeChar._class_id == 13)
					{
						if (activeChar.getClassId().getId() == 100)
						{
							activeChar.sendMessage("Sorry you're already in the " + nameclasse + " class");
							return false;
						}
						
					//	for (L2Skill skill : activeChar.getSkills().values())
					//		activeChar.removeSkill(skill.getId(), true);
						RequestBypassToServer.RemoverSkills(activeChar);
						
						activeChar.setClassId(100);
						
						if (!activeChar.isSubClassActive())
							activeChar.setBaseClass(100);
						
						Finish(activeChar);
					}
					else if (activeChar._class_id == 14)
					{
						if (activeChar.getClassId().getId() == 101)
						{
							activeChar.sendMessage("Sorry you're already in the " + nameclasse + " class");
							return false;
						}
						
						//for (L2Skill skill : activeChar.getSkills().values())
					//		activeChar.removeSkill(skill.getId(), true);
						RequestBypassToServer.RemoverSkills(activeChar);
						
						activeChar.setClassId(101);
						
						if (!activeChar.isSubClassActive())
							activeChar.setBaseClass(101);
						
						Finish(activeChar);
					}
					else if (activeChar._class_id == 15)
					{
						if (activeChar.getClassId().getId() == 102)
						{
							activeChar.sendMessage("Sorry you're already in the " + nameclasse + " class");
							return false;
						}
						
					//	for (L2Skill skill : activeChar.getSkills().values())
						//	activeChar.removeSkill(skill.getId(), true);
						RequestBypassToServer.RemoverSkills(activeChar);
						
						activeChar.setClassId(102);
						
						if (!activeChar.isSubClassActive())
							activeChar.setBaseClass(102);
						
						Finish(activeChar);
					}
					else if (activeChar._class_id == 16)
					{
						if (activeChar.getClassId().getId() == 103)
						{
							activeChar.sendMessage("Sorry you're already in the " + nameclasse + " class");
							return false;
						}
						
					//	for (L2Skill skill : activeChar.getSkills().values())
					//		activeChar.removeSkill(skill.getId(), true);
						RequestBypassToServer.RemoverSkills(activeChar);
						
						activeChar.setClassId(103);
						if (!activeChar.isSubClassActive())
							activeChar.setBaseClass(103);
						
						Finish(activeChar);
					}
					else if (activeChar._class_id == 17)
					{
						if (activeChar.getClassId().getId() == 104)
						{
							activeChar.sendMessage("Sorry you're already in the " + nameclasse + " class");
							return false;
						}
						
					//	for (L2Skill skill : activeChar.getSkills().values())
					//		activeChar.removeSkill(skill.getId(), true);
						RequestBypassToServer.RemoverSkills(activeChar);
						
						activeChar.setClassId(104);
						
						if (!activeChar.isSubClassActive())
							activeChar.setBaseClass(104);
						
						Finish(activeChar);
					}
					else if (activeChar._class_id == 18)
					{
						
						if (activeChar.getClassId().getId() == 105)
						{
							activeChar.sendMessage("Sorry you're already in the " + nameclasse + " class");
							return false;
						}
						
					//	for (L2Skill skill : activeChar.getSkills().values())
					//		activeChar.removeSkill(skill.getId(), true);
						RequestBypassToServer.RemoverSkills(activeChar);
						
						activeChar.setClassId(105);
						
						if (!activeChar.isSubClassActive())
							activeChar.setBaseClass(105);
						
						Finish(activeChar);
					}
					else if (activeChar._class_id == 19)
					{
						if (activeChar.getClassId().getId() == 106)
						{
							activeChar.sendMessage("Sorry you're already in the " + nameclasse + " class");
							return false;
						}
						
					//	for (L2Skill skill : activeChar.getSkills().values())
					//		activeChar.removeSkill(skill.getId(), true);
						RequestBypassToServer.RemoverSkills(activeChar);
						
						activeChar.setClassId(106);
						
						if (!activeChar.isSubClassActive())
							activeChar.setBaseClass(106);
						
						Finish(activeChar);
					}
					else if (activeChar._class_id == 20)
					{
						if (activeChar.getClassId().getId() == 107)
						{
							activeChar.sendMessage("Sorry you're already in the " + nameclasse + " class");
							return false;
						}
						
					//	for (L2Skill skill : activeChar.getSkills().values())
						//	activeChar.removeSkill(skill.getId(), true);
						RequestBypassToServer.RemoverSkills(activeChar);
						
						activeChar.setClassId(107);
						
						if (!activeChar.isSubClassActive())
							activeChar.setBaseClass(107);
						
						Finish(activeChar);
					}
					else if (activeChar._class_id == 21)
					{
						if (activeChar.getClassId().getId() == 108)
						{
							activeChar.sendMessage("Sorry you're already in the " + nameclasse + " class");
							return false;
						}
						
				//		for (L2Skill skill : activeChar.getSkills().values())
				//			activeChar.removeSkill(skill.getId(), true);
						RequestBypassToServer.RemoverSkills(activeChar);
						
						activeChar.setClassId(108);
						
						if (!activeChar.isSubClassActive())
							activeChar.setBaseClass(108);
						
						Finish(activeChar);
					}
					else if (activeChar._class_id == 22)
					{
						if (activeChar.getClassId().getId() == 109)
						{
							activeChar.sendMessage("Sorry you're already in the " + nameclasse + " class");
							return false;
						}
						
					//	for (L2Skill skill : activeChar.getSkills().values())
					//		activeChar.removeSkill(skill.getId(), true);
						RequestBypassToServer.RemoverSkills(activeChar);
						
						activeChar.setClassId(109);
						
						if (!activeChar.isSubClassActive())
							activeChar.setBaseClass(109);
						
						Finish(activeChar);
					}
					else if (activeChar._class_id == 23)
					{
						if (activeChar.getClassId().getId() == 110)
						{
							activeChar.sendMessage("Sorry you're already in the " + nameclasse + " class");
							return false;
						}
						
					//	for (L2Skill skill : activeChar.getSkills().values())
					//		activeChar.removeSkill(skill.getId(), true);
						RequestBypassToServer.RemoverSkills(activeChar);
						
						activeChar.setClassId(110);
						
						if (!activeChar.isSubClassActive())
							activeChar.setBaseClass(110);
						
						Finish(activeChar);
					}
					else if (activeChar._class_id == 24)
					{
						if (activeChar.getClassId().getId() == 111)
						{
							activeChar.sendMessage("Sorry you're already in the " + nameclasse + " class");
							return false;
						}
						
					//	for (L2Skill skill : activeChar.getSkills().values())
					//		activeChar.removeSkill(skill.getId(), true);
						RequestBypassToServer.RemoverSkills(activeChar);
						
						activeChar.setClassId(111);
						
						if (!activeChar.isSubClassActive())
							activeChar.setBaseClass(111);
						
						Finish(activeChar);
					}
					else if (activeChar._class_id == 25)
					{
						if (activeChar.getClassId().getId() == 112)
						{
							activeChar.sendMessage("Sorry you're already in the " + nameclasse + " class");
							return false;
						}
						
					//	for (L2Skill skill : activeChar.getSkills().values())
						//	activeChar.removeSkill(skill.getId(), true);
						RequestBypassToServer.RemoverSkills(activeChar);
						
						activeChar.setClassId(112);
						
						if (!activeChar.isSubClassActive())
							activeChar.setBaseClass(112);
						
						Finish(activeChar);
					}
					else if (activeChar._class_id == 26)
					{
						if (activeChar.getClassId().getId() == 113)
						{
							activeChar.sendMessage("Sorry you're already in the " + nameclasse + " class");
							return false;
						}
						
					//	for (L2Skill skill : activeChar.getSkills().values())
					//		activeChar.removeSkill(skill.getId(), true);
						RequestBypassToServer.RemoverSkills(activeChar);
						
						activeChar.setClassId(113);
						
						if (!activeChar.isSubClassActive())
							activeChar.setBaseClass(113);
						
						Finish(activeChar);
					}
					else if (activeChar._class_id == 27)
					{
						if (activeChar.getClassId().getId() == 114)
						{
							activeChar.sendMessage("Sorry you're already in the " + nameclasse + " class");
							return false;
						}
						
					//	for (L2Skill skill : activeChar.getSkills().values())
					//		activeChar.removeSkill(skill.getId(), true);
						RequestBypassToServer.RemoverSkills(activeChar);
						
						activeChar.setClassId(114);
						
						if (!activeChar.isSubClassActive())
							activeChar.setBaseClass(114);
						
						Finish(activeChar);
					}
					else if (activeChar._class_id == 28)
					{
						if (activeChar.getClassId().getId() == 115)
						{
							activeChar.sendMessage("Sorry you're already in the " + nameclasse + " class");
							return false;
						}
						
					//	for (L2Skill skill : activeChar.getSkills().values())
					//		activeChar.removeSkill(skill.getId(), true);
						RequestBypassToServer.RemoverSkills(activeChar);
						
						activeChar.setClassId(115);
						
						if (!activeChar.isSubClassActive())
							activeChar.setBaseClass(115);
						
						Finish(activeChar);
					}
					else if (activeChar._class_id == 29)
					{
						if (activeChar.getClassId().getId() == 116)
						{
							activeChar.sendMessage("Sorry you're already in the " + nameclasse + " class");
							return false;
						}
						
					//	for (L2Skill skill : activeChar.getSkills().values())
						//	activeChar.removeSkill(skill.getId(), true);
						RequestBypassToServer.RemoverSkills(activeChar);
						
						activeChar.setClassId(116);
						
						if (!activeChar.isSubClassActive())
							activeChar.setBaseClass(116);
						
						Finish(activeChar);
					}
					else if (activeChar._class_id == 30)
					{
						if (activeChar.getClassId().getId() == 117)
						{
							activeChar.sendMessage("Sorry you're already in the " + nameclasse + " class");
							return false;
						}
						
					//	for (L2Skill skill : activeChar.getSkills().values())
						//	activeChar.removeSkill(skill.getId(), true);
						RequestBypassToServer.RemoverSkills(activeChar);
						
						activeChar.setClassId(117);
						
						if (!activeChar.isSubClassActive())
							activeChar.setBaseClass(117);
						
						Finish(activeChar);
					}
					else if (activeChar._class_id == 31)
					{
						if (activeChar.getClassId().getId() == 118)
						{
							activeChar.sendMessage("Sorry you're already in the " + nameclasse + " class");
							return false;
						}
						
					//	for (L2Skill skill : activeChar.getSkills().values())
						//	activeChar.removeSkill(skill.getId(), true);
						RequestBypassToServer.RemoverSkills(activeChar);
						
						activeChar.setClassId(118);
						
						if (!activeChar.isSubClassActive())
							activeChar.setBaseClass(118);
						
						Finish(activeChar);
					}
					else
						activeChar.sendMessage("ERROR , CONTACT STAFF.");
					
				}
				else
					Incorrect_item(activeChar);
				
			}
			else if (command.startsWith("sex_"))
			{
				Sex male = Sex.MALE;
				Sex female = Sex.FEMALE;
				
				if (activeChar.getAppearance().getSex() == male)
					activeChar._sex_id = 1;
				else if (activeChar.getAppearance().getSex() == female)
					activeChar._sex_id = 2;
				
				sex_select(activeChar);
				
			}
			else if (command.startsWith("setsex_"))
			{
				if (activeChar.destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.DONATE_SEX_PRICE, null, true))
				{
					Sex male = Sex.MALE;
					Sex female = Sex.FEMALE;
					
					if (activeChar.getAppearance().getSex() == male)
					{
						activeChar.getAppearance().setSex(female);
						activeChar.sendPacket(new ExShowScreenMessage("Congratulations. Your sex has been changed.", 6000, 0x02, true));
						activeChar.broadcastUserInfo();
						activeChar.decayMe();
						activeChar.spawnMe();
					}
					else if (activeChar.getAppearance().getSex() == female)
					{
						activeChar.getAppearance().setSex(male);
						activeChar.sendPacket(new ExShowScreenMessage("Congratulations. Your sex has been changed.", 6000, 0x02, true));
						activeChar.broadcastUserInfo();
						activeChar.decayMe();
						activeChar.spawnMe();
						showMainHtml(activeChar);
					}
					
					ThreadPool.schedule(new Runnable()
					{
						
						@Override
						public void run()
						{
							activeChar.getClient().closeNow();
						}
					}, 3000);
				}
				else
					Incorrect_item(activeChar);
				
			}
			else if (command.startsWith("setname_"))
			{
				if (activeChar._change_Name.length() > 16)
				{
					activeChar.sendMessage("The chosen name cannot exceed 16 characters in length.");
					showMainHtml(activeChar);
					return false;
				}
				else if (activeChar._change_Name.length() < 3)
				{
					activeChar.sendMessage("Your name can not be mention that 3 characters in length.");
					showMainHtml(activeChar);
					return false;
				}
				else if (Config.FORBIDDEN_NAMES.length > 1)
				{
					for (String st : Config.FORBIDDEN_NAMES)
					{
						if (activeChar._change_Name.toLowerCase().contains(st.toLowerCase()))
						{
							activeChar.sendMessage("This name is inappropriate.");
							return false;
						}
					}
				}
				else if (!StringUtil.isValidPlayerName(activeChar._change_Name))
				{
					activeChar.sendMessage("The new name doesn't fit with the regex pattern.");
					showMainHtml(activeChar);
					return false;
				}
			//	else if (PlayerInfoTable.getInstance().getPlayerObjectId(activeChar._change_Name) > 0)
				else if (CharNameTable.getInstance().getIdByName(activeChar._change_Name) > 0)
				{
					activeChar.sendMessage("The chosen name already exists.");
					showMainHtml(activeChar);
					return false;
				}
				
				if (activeChar.destroyItemByItemId("Donate Coin", Config.DONATE_COIN_ID, Config.DONATE_NAME_PRICE, null, true))
				{
					activeChar.setName(activeChar._change_Name);
					CharNameTable.getInstance().register(activeChar);
					//CharNameTable.getInstance().updatePlayerData(activeChar, false);
					activeChar.sendPacket(new ExShowScreenMessage("Congratulations. Your name has been changed.", 6000, 0x02, true));
					activeChar.broadcastUserInfo();
					activeChar.store();
					activeChar.sendPacket(new PlaySound("ItemSound.quest_finish"));
					showMainHtml(activeChar);
				}
				else
					Incorrect_item(activeChar);
				
			}
		return false;
	}

	

	
	public void Buy_Hero(Player player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/mods/menu/hero.htm";
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(filename);
		if (player._hero_days == 0)
		{
			html.replace("%coin%", "0");
		}
		else if (player._hero_days == 30)
		{
			html.replace("%coin%", "<font color=\"00FF00\">" + Config.HERO_30_DAYS_PRICE + "</font>");
		}
		else if (player._hero_days == 60)
		{
			html.replace("%coin%", "<font color=\"00FF00\">" + Config.HERO_60_DAYS_PRICE + "</font>");
		}
		else if (player._hero_days == 90)
		{
			html.replace("%coin%", "<font color=\"00FF00\">" + Config.HERO_90_DAYS_PRICE + "</font>");
		}
		else if (player._hero_days >= 360)
		{
			html.replace("%coin%", "<font color=\"00FF00\">" + Config.HERO_ETERNAL_PRICE + "</font>");
		}
		if (player._hero_days == 0)
		{
			html.replace("%hero_days%", "0");
		}
	
		else if (player._hero_days == 30)
		{
			html.replace("%hero_days%", "<font color=\"00FF00\">30</font> Days");
		}
		else if (player._hero_days == 60)
		{
			html.replace("%hero_days%", "<font color=\"00FF00\">60</font> Days");
		}
		else if (player._hero_days == 90)
		{
			html.replace("%hero_days%", "<font color=\"00FF00\">90</font> Days");
		}
		else if (player._hero_days >= 360)
		{
			html.replace("%hero_days%", "<font color=\"00FF00\">Eternal</font>");
		}
		player.sendPacket(html);
	}

	public void augmentsAlternative(Player player, int attributes, int idaugment, int levelaugment)
	{
		ItemInstance rhand = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);

		if (rhand == null)
		{
			player.sendMessage(player.getName() + " have to equip a weapon.");
			return;
		}
		else if (rhand.getItem().getCrystalType().getId() == 0 || rhand.getItem().getCrystalType().getId() == 1 || rhand.getItem().getCrystalType().getId() == 2)
		{
			player.sendMessage("You can't augment under " + rhand.getItem().getCrystalType() + " Grade Weapon!");
			return;
		}
		else if (rhand.isHeroItem())
		{
			player.sendMessage("You Cannot be add Augment On " + rhand.getItemName() + " !");
			return;
		}
		else if (player.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, -1) < Config.AUGM_PRICE_ALTERNATIVE)
		{
			player.sendMessage("You do not have enough Ticket Donate.");
			return;
		}
		if (!rhand.isAugmented())
		{
			player.sendMessage("Purchase made successfully, check your weapon.");
			augmentweapondatabase(player, attributes, idaugment, levelaugment);
		}
		else
		{
			player.sendMessage("You Have Augment on weapon!");
			return;
		}
	}

	public void augments(Player player, int attributes, int idaugment, int levelaugment)
	{
	ItemInstance rhand = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
				
	if (rhand == null)
	{
	player.sendMessage(player.getName() + " have to equip a weapon.");
	return;
	}
	else if (rhand.getItem().getCrystalType().getId() == 0 || rhand.getItem().getCrystalType().getId() == 1 || rhand.getItem().getCrystalType().getId() == 2)
	{
		player.sendMessage("You can't augment under " + rhand.getItem().getCrystalType() + " Grade Weapon!");
		return;
	}
	else if (rhand.isHeroItem())
	{
		player.sendMessage("You Cannot be add Augment On " + rhand.getItemName() + " !");
		return;
	}
	else if (player.getInventory().getInventoryItemCount(Config.DONATE_COIN_ID, -1) < Config.AUGM_ITEM_COUNT)
	{
		player.sendMessage("You do not have enough Donate Coins.");
		return;
	}
	if (!rhand.isAugmented())
	{
		player.sendMessage("Successfully To Add " + SkillTable.getInstance().getInfo(idaugment, levelaugment).getName() + ".");
		augmentweapondatabase(player, attributes, idaugment, levelaugment);
	}
	else
	{
		player.sendMessage("You Have Augment on weapon!");
		return;
	}
}

	public void augmentweapondatabase(Player player, int attributes, int id, int level)
	{
		ItemInstance item = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		L2Augmentation augmentation = new L2Augmentation(attributes, id, level);
		augmentation.applyBonus(player);
		player.destroyItemByItemId("Consume", Config.DONATE_COIN_ID, Config.AUGM_ITEM_COUNT, player, true);
		item.setAugmentation(augmentation);
		player.disarmWeapons();
		
		try (Connection con = ConnectionPool.getConnection())
		{
			PreparedStatement statement = con.prepareStatement("REPLACE INTO augmentations VALUES(?,?,?,?)");
			statement.setInt(1, item.getObjectId());
			statement.setInt(2, attributes);
			statement.setInt(3, id);
			statement.setInt(4, level);
			InventoryUpdate iu = new InventoryUpdate();
			player.sendPacket(iu);
			statement.execute();
			statement.close();
		}
		catch (SQLException e)
		{
			System.out.println(e);
		}
	}

	public void class_select(Player activeChar)
	{
		activeChar._class_id = 0;
		activeChar.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/mods/menu/class_select.htm";
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(filename);
		activeChar.sendPacket(html);
	}

	public void class_finish(Player activeChar)
	{
		activeChar.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/mods/menu/class_finish.htm";
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(filename);
		html.replace("%coin%", "<font color=\"00FF00\">" + Config.DONATE_CLASS_PRICE + "</font>");
		
		if (activeChar._class_id == 1)
			html.replace("%class_name%", "Duelist");
		else if (activeChar._class_id == 2)
			html.replace("%class_name%", "Dreadnought");
		else if (activeChar._class_id == 3)
			html.replace("%class_name%", "Phoenix Knight");
		else if (activeChar._class_id == 4)
			html.replace("%class_name%", "Hell Knight");
		else if (activeChar._class_id == 5)
			html.replace("%class_name%", "Sagittarius");
		else if (activeChar._class_id == 6)
			html.replace("%class_name%", "Adventurer");
		else if (activeChar._class_id == 7)
			html.replace("%class_name%", "Archmage");
		else if (activeChar._class_id == 8)
			html.replace("%class_name%", "Soultaker");
		else if (activeChar._class_id == 9)
			html.replace("%class_name%", "Arcana Lord");
		else if (activeChar._class_id == 10)
			html.replace("%class_name%", "Cardinal");
		else if (activeChar._class_id == 11)
			html.replace("%class_name%", "Hierophant");
		else if (activeChar._class_id == 12)
			html.replace("%class_name%", "Eva's Templar");
		else if (activeChar._class_id == 13)
			html.replace("%class_name%", "Sword Muse");
		else if (activeChar._class_id == 14)
			html.replace("%class_name%", "Wind Rider");
		else if (activeChar._class_id == 15)
			html.replace("%class_name%", "Moonlight Sentinel");
		else if (activeChar._class_id == 16)
			html.replace("%class_name%", "Mystic Muse");
		else if (activeChar._class_id == 17)
			html.replace("%class_name%", "Elemental Master");
		else if (activeChar._class_id == 18)
			html.replace("%class_name%", "Eva's Saint");
		else if (activeChar._class_id == 19)
			html.replace("%class_name%", "Shillien Templar");
		else if (activeChar._class_id == 20)
			html.replace("%class_name%", "Spectral Dancer");
		else if (activeChar._class_id == 21)
			html.replace("%class_name%", "Ghost Hunter");
		else if (activeChar._class_id == 22)
			html.replace("%class_name%", "Ghost Sentinel");
		else if (activeChar._class_id == 23)
			html.replace("%class_name%", "Storm Screamer");
		else if (activeChar._class_id == 24)
			html.replace("%class_name%", "Spectral Master");
		else if (activeChar._class_id == 25)
			html.replace("%class_name%", "Shillien Saint");
		else if (activeChar._class_id == 26)
			html.replace("%class_name%", "Titan");
		else if (activeChar._class_id == 27)
			html.replace("%class_name%", "Grand Khavatari");
		else if (activeChar._class_id == 28)
			html.replace("%class_name%", "Dominator");
		else if (activeChar._class_id == 29)
			html.replace("%class_name%", "Doomcryer");
		else if (activeChar._class_id == 30)
			html.replace("%class_name%", "Fortune Seeker");
		else if (activeChar._class_id == 31)
			html.replace("%class_name%", "Maestro");
		
		if (activeChar._class_id >= 1 && activeChar._class_id <= 6)
			html.replace("%race%", "Human Fighter");
		else if (activeChar._class_id >= 7 && activeChar._class_id <= 11)
			html.replace("%race%", "Human Mystic");
		else if (activeChar._class_id >= 12 && activeChar._class_id <= 15)
			html.replace("%race%", "Elf Fighter");
		else if (activeChar._class_id >= 16 && activeChar._class_id <= 18)
			html.replace("%race%", "Elf Mystic");
		else if (activeChar._class_id >= 19 && activeChar._class_id <= 22)
			html.replace("%race%", "Dark Elf Fighter");
		else if (activeChar._class_id >= 23 && activeChar._class_id <= 25)
			html.replace("%race%", "Dark Elf Mystic");
		else if (activeChar._class_id >= 26 && activeChar._class_id <= 27)
			html.replace("%race%", "Orc Fighter");
		else if (activeChar._class_id >= 28 && activeChar._class_id <= 29)
			html.replace("%race%", "Orc Mystic");
		else if (activeChar._class_id >= 30 && activeChar._class_id <= 31)
			html.replace("%race%", "Dwarf Fighter");
		showMainHtml(activeChar);
		activeChar.sendPacket(html);
	}

	public static void Finish(Player activeChar)
	{
		String newclass = activeChar.getTemplate().getClassName();
		
		activeChar.sendMessage(activeChar.getName() + " is now a " + newclass + ".");
		activeChar.sendPacket(new ExShowScreenMessage("Congratulations. You is now a " + newclass + ".", 6000, 0x02, true));
		
		activeChar.refreshOverloaded();
		activeChar.store();
		activeChar.sendPacket(new HennaInfo(activeChar));
		activeChar.sendSkillList();
		activeChar.broadcastUserInfo();
		activeChar.removeItens();
		activeChar.sendPacket(new PlaySound("ItemSound.quest_finish"));
		
		if (activeChar.isNoble())
		{
			StatsSet playerStat = Olympiad.getNobleStats(activeChar.getObjectId());
			if (playerStat != null)
			{
				try (Connection con = ConnectionPool.getConnection())
				{
					PreparedStatement statement;
					statement = con.prepareStatement("DELETE FROM olympiad_nobles WHERE char_id=?");
					statement.setInt(1, activeChar.getObjectId());
					statement.execute();
					statement.close();
				}
				catch (Exception e)
				{
					System.out.println("Class Item: " + e);
				}
				
				Olympiad.getNobleStats(activeChar.getObjectId());
				AdminEditChar.DeleteHero(activeChar);
				activeChar.sendMessage("You now has " + Olympiad.getInstance().getNoblePoints(activeChar.getObjectId()) + " Olympiad points.");
			}
		}
		
		try (Connection con = ConnectionPool.getConnection())
		{
			// Remove all henna info stored for this sub-class.
			PreparedStatement statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=?");
			statement.setInt(1, activeChar.getObjectId());
			statement.setInt(2, 0);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			System.out.println("Class Item: " + e);
		}
		
		ThreadPool.schedule(new Runnable()
		{
			
			@Override
			public void run()
			{
				activeChar.getClient().closeNow();
			}
		}, 3000);
	}

	public void sex_select(Player activeChar)
	{
		activeChar.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/mods/menu/sex.htm";
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(filename);
		
		if (activeChar.getAppearance().getSex() == Sex.MALE)
			html.replace("%old_sex%", "Male / Masculino");
		else
			html.replace("%old_sex%", "Female / Feminino");
		
		if (activeChar._sex_id == 1)
			html.replace("%new_sex%", "Female / Feminino");
		else
			html.replace("%new_sex%", "Male / Masculino");
		
		html.replace("%coin%", "<font color=\"00FF00\">" + Config.DONATE_SEX_PRICE + "</font>");
		activeChar.sendPacket(html);
	}
	
	
	public void name_select(Player activeChar)
	{
		activeChar._change_Name = "";
		activeChar.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/mods/menu/name_select.htm";
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(filename);
		html.replace("%name%", activeChar.getName());
		activeChar.sendPacket(html);
	}

	public void name_finish(Player activeChar)
	{
		activeChar.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/mods/menu/name_finish.htm";
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(filename);
		html.replace("%old_name%", activeChar.getName());
		html.replace("%new_name%", activeChar._change_Name);
		html.replace("%coin%", "<font color=\"00FF00\">" + Config.DONATE_NAME_PRICE + "</font>");
		activeChar.sendPacket(html);
	}
	
	public void Incorrect_item(Player activeChar)
	{
		activeChar._vip_days = 0;
		activeChar._hero_days = 0;
		//activeChar._class_id = 0;
		activeChar._sex_id = 0;
		activeChar._change_Name = "";
		activeChar.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/mods/menu/incorrect.htm";
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(filename);
		activeChar.sendPacket(html);
	}
	public void Buy_Vip(Player player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
		String filename = "data/html/mods/menu/vip.htm";
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(filename);
		if (player._vip_days == 0)
		{
			html.replace("%coin%", "0");
		}

		else if (player._vip_days == 30)
		{
			html.replace("%coin%", "<font color=\"00FF00\">" + Config.VIP_30_DAYS_PRICE + "</font>");
		}
		else if (player._vip_days == 60)
		{
			html.replace("%coin%", "<font color=\"00FF00\">" + Config.VIP_60_DAYS_PRICE + "</font>");
		}
		else if (player._vip_days == 90)
		{
			html.replace("%coin%", "<font color=\"00FF00\">" + Config.VIP_90_DAYS_PRICE + "</font>");
		}
		else if (player._vip_days >= 360)
		{
			html.replace("%coin%", "<font color=\"00FF00\">" + Config.VIP_ETERNAL_PRICE + "</font>");
		}
		if (player._vip_days == 0)
		{
			html.replace("%vip_days%", "0");
		}
	
		else if (player._vip_days == 30)
		{
			html.replace("%vip_days%", "<font color=\"00FF00\">30</font> Days");
		}
		else if (player._vip_days == 60)
		{
			html.replace("%vip_days%", "<font color=\"00FF00\">60</font> Days");
		}
		else if (player._vip_days == 90)
		{
			html.replace("%vip_days%", "<font color=\"00FF00\">90</font> Days");
		}
		else if (player._vip_days >= 360)
		{
			html.replace("%vip_days%", "<font color=\"00FF00\">Eternal</font>");
		}
		player.sendPacket(html);
	}
	public static void showOthersHtml(Player activeChar)
	{
		//activeChar._class_id = 0;
		//activeChar._sex_id = 0;
		//activeChar._change_Name = "";
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/mods/menu/Others.htm");
		activeChar.sendPacket(html);
	}
	
	public static void showArgumentHtml(Player activeChar)
	{
		//activeChar._class_id = 0;
		//activeChar._sex_id = 0;
		//activeChar._change_Name = "";
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/mods/menu/ArgumentAlternative.htm");
		activeChar.sendPacket(html);
	}
	
	public static void showMainHtml(Player activeChar)
	{
		//activeChar._class_id = 0;
		activeChar._sex_id = 0;
		activeChar._change_Name = "";
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/mods/menu/donate.htm");
		activeChar.sendPacket(html);
	}
	
	public static void showArgWeaponPassive(Player activeChar)
	{
		//activeChar._class_id = 0;
		//activeChar._sex_id = 0;
		//activeChar._change_Name = "";
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/mods/menu/weapon_passive.htm");
		activeChar.sendPacket(html);
	}
	public static void showArgWeaponActive(Player activeChar)
	{
		//activeChar._class_id = 0;
		//activeChar._sex_id = 0;
		//activeChar._change_Name = "";
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/mods/menu/weapon_active.htm");
		activeChar.sendPacket(html);
	}
	public static void showClanServices(Player activeChar)
	{
		//activeChar._class_id = 0;
		//activeChar._sex_id = 0;
		//activeChar._change_Name = "";
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/mods/menu/ClanServices.htm");
		activeChar.sendPacket(html);
	}
	

	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
	
}
