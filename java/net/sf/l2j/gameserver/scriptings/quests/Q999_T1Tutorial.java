package net.sf.l2j.gameserver.scriptings.quests;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2j.Config;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.actor.L2Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.scriptings.Quest;
import net.sf.l2j.gameserver.scriptings.QuestState;

public class Q999_T1Tutorial extends Quest
{
	private static String qn = "Q999_T1Tutorial";
	private static String qnTutorial = "Tutorial";
	
	private static final HashMap<Object, Object[]> Events = new HashMap<>();
	private static final Map<Integer, Talk> Talks = new HashMap<>();
	
	private static int RECOMMENDATION_01 = 1067;
	private static int RECOMMENDATION_02 = 1068;
	private static int LEAF_OF_MOTHERTREE = 1069;
	private static int BLOOD_OF_JUNDIN = 1070;
	private static int LICENSE_OF_MINER = 1498;
	private static int VOUCHER_OF_FLAME = 1496;
	private static int SOULSHOT_NOVICE = 5789;
	private static int SPIRITSHOT_NOVICE = 5790;
	private static int BLUE_GEM = 6353;
	private static int TOKEN = 8542;
	private static int SCROLL = 8594;
	
	private static final int[] NPCS =
	{
		30008,
		30009,
		30017,
		30019,
		30129,
		30131,
		30573,
		30575,
		30370,
		30528,
		30530,
		30400,
		30401,
		30402,
		30403,
		30404
	};
	
	private static class Talk
	{
		public int raceId;
		public String[] htmlfiles;
		public int npcTyp;
		public int item;
		
		public Talk(int _raceId, String[] _htmlfiles, int _npcTyp, int _item)
		{
			raceId = _raceId;
			htmlfiles = _htmlfiles;
			npcTyp = _npcTyp;
			item = _item;
		}
	}
	
	//public Q999_T1Tutorial(int questId, String name, String descr)
	public Q999_T1Tutorial()
	{
		//super(questId, name, descr);
		//super(questId, name);
		super(999, "T1Tutorial");
		
		Events.put("30008_02", new Object[]
		{
			"30008-03.htm",
			-84058,
			243239,
			-3730,
			RECOMMENDATION_01,
			0x00,
			SOULSHOT_NOVICE,
			200,
			0x00,
			0,
			0
		});
		
		Events.put("30008_04", new Object[]
		{
			"30008-04.htm",
			0,
			0,
			0,
			0,
			0x00,
			0,
			0,
			0,
			0,
			0
		});
		
		Events.put("30017_02", new Object[]
		{
			"30017-03.htm",
			-84058,
			243239,
			-3730,
			RECOMMENDATION_02,
			0x0a,
			SPIRITSHOT_NOVICE,
			100,
			0x00,
			0,
			0
		});
		
		Events.put("30017_04", new Object[]
		{
			"30017-04.htm",
			0,
			0,
			0,
			0,
			0x0a,
			0,
			0,
			0x00,
			0,
			0
		});
		
		Events.put("30370_02", new Object[]
		{
			"30370-03.htm",
			45491,
			48359,
			-3086,
			LEAF_OF_MOTHERTREE,
			0x19,
			SPIRITSHOT_NOVICE,
			100,
			0x12,
			SOULSHOT_NOVICE,
			200
		});
		
		Events.put("30370_04", new Object[]
		{
			"30370-04.htm",
			0,
			0,
			0,
			0,
			0x19,
			0,
			0,
			0x12,
			0,
			0
		});
		
		Events.put("30129_02", new Object[]
		{
			"30129-03.htm",
			12116,
			16666,
			-4610,
			BLOOD_OF_JUNDIN,
			0x26,
			SPIRITSHOT_NOVICE,
			100,
			0x1f,
			SOULSHOT_NOVICE,
			200
		});
		
		Events.put("30129_04", new Object[]
		{
			"30129-04.htm",
			0,
			0,
			0,
			0,
			0x26,
			0,
			0,
			0x1f,
			0,
			0
		});
		
		Events.put("30528_02", new Object[]
		{
			"30528-03.htm",
			115642,
			-178046,
			-941,
			LICENSE_OF_MINER,
			0x35,
			SOULSHOT_NOVICE,
			200,
			0x00,
			0,
			0
		});
		
		Events.put("30528_04", new Object[]
		{
			"30528-04.htm",
			0,
			0,
			0,
			0,
			0x35,
			0,
			0,
			0x00,
			0,
			0
		});
		
		Events.put("30573_02", new Object[]
		{
			"30573-03.htm",
			-45067,
			-113549,
			-235,
			VOUCHER_OF_FLAME,
			0x31,
			SPIRITSHOT_NOVICE,
			100,
			0x2c,
			SOULSHOT_NOVICE,
			200
		});
		
		Events.put("30573_04", new Object[]
		{
			"30573-04.htm",
			0,
			0,
			0,
			0,
			0x31,
			0,
			0,
			0x2c,
			0,
			0
		});
		
		Talks.put(30017, new Talk(0, new String[]
		{
			"30017-01.htm",
			"30017-02.htm",
			"30017-04.htm"
		}, 0, 0));
		
		Talks.put(30008, new Talk(0, new String[]
		{
			"30008-01.htm",
			"30008-02.htm",
			"30008-04.htm"
		}, 0, 0));
		
		Talks.put(30370, new Talk(1, new String[]
		{
			"30370-01.htm",
			"30370-02.htm",
			"30370-04.htm"
		}, 0, 0));
		
		Talks.put(30129, new Talk(2, new String[]
		{
			"30129-01.htm",
			"30129-02.htm",
			"30129-04.htm"
		}, 0, 0));
		
		Talks.put(30573, new Talk(3, new String[]
		{
			"30573-01.htm",
			"30573-02.htm",
			"30573-04.htm"
		}, 0, 0));
		
		Talks.put(30528, new Talk(4, new String[]
		{
			"30528-01.htm",
			"30528-02.htm",
			"30528-04.htm"
		}, 0, 0));
		
		Talks.put(30018, new Talk(0, new String[]
		{
			"30131-01.htm",
			"",
			"30019-03a.htm",
			"30019-04.htm",
		}, 1, RECOMMENDATION_02));
		
		Talks.put(30019, new Talk(0, new String[]
		{
			"30131-01.htm",
			"",
			"30019-03a.htm",
			"30019-04.htm",
		}, 1, RECOMMENDATION_02));
		
		Talks.put(30020, new Talk(0, new String[]
		{
			"30131-01.htm",
			"",
			"30019-03a.htm",
			"30019-04.htm",
		}, 1, RECOMMENDATION_02));
		
		Talks.put(30021, new Talk(0, new String[]
		{
			"30131-01.htm",
			"",
			"30019-03a.htm",
			"30019-04.htm",
		}, 1, RECOMMENDATION_02));
		
		Talks.put(30009, new Talk(0, new String[]
		{
			"30530-01.htm",
			"30009-03.htm",
			"",
			"30009-04.htm",
		}, 1, RECOMMENDATION_01));
		
		Talks.put(30011, new Talk(0, new String[]
		{
			"30530-01.htm",
			"30009-03.htm",
			"",
			"30009-04.htm",
		}, 1, RECOMMENDATION_01));
		
		Talks.put(30012, new Talk(0, new String[]
		{
			"30530-01.htm",
			"30009-03.htm",
			"",
			"30009-04.htm",
		}, 1, RECOMMENDATION_01));
		
		Talks.put(30056, new Talk(0, new String[]
		{
			"30530-01.htm",
			"30009-03.htm",
			"",
			"30009-04.htm",
		}, 1, RECOMMENDATION_01));
		
		Talks.put(30400, new Talk(1, new String[]
		{
			"30131-01.htm",
			"30400-03.htm",
			"30400-03a.htm",
			"30400-04.htm",
		}, 1, LEAF_OF_MOTHERTREE));
		
		Talks.put(30401, new Talk(1, new String[]
		{
			"30131-01.htm",
			"30400-03.htm",
			"30400-03a.htm",
			"30400-04.htm",
		}, 1, LEAF_OF_MOTHERTREE));
		
		Talks.put(30402, new Talk(1, new String[]
		{
			"30131-01.htm",
			"30400-03.htm",
			"30400-03a.htm",
			"30400-04.htm",
		}, 1, LEAF_OF_MOTHERTREE));
		
		Talks.put(30403, new Talk(1, new String[]
		{
			"30131-01.htm",
			"30400-03.htm",
			"30400-03a.htm",
			"30400-04.htm",
		}, 1, LEAF_OF_MOTHERTREE));
		
		Talks.put(30131, new Talk(2, new String[]
		{
			"30131-01.htm",
			"30131-03.htm",
			"30131-03a.htm",
			"30131-04.htm",
		}, 1, BLOOD_OF_JUNDIN));
		
		Talks.put(30404, new Talk(2, new String[]
		{
			"30131-01.htm",
			"30131-03.htm",
			"30131-03a.htm",
			"30131-04.htm",
		}, 1, BLOOD_OF_JUNDIN));
		
		Talks.put(30574, new Talk(3, new String[]
		{
			"30575-01.htm",
			"30575-03.htm",
			"30575-03a.htm",
			"30575-04.htm",
		}, 1, VOUCHER_OF_FLAME));
		
		Talks.put(30575, new Talk(3, new String[]
		{
			"30575-01.htm",
			"30575-03.htm",
			"30575-03a.htm",
			"30575-04.htm",
		}, 1, VOUCHER_OF_FLAME));
		
		Talks.put(30530, new Talk(4, new String[]
		{
			"30530-01.htm",
			"30530-03.htm",
			"",
			"30530-04.htm",
		}, 1, LICENSE_OF_MINER));
		
		addStartNpc(NPCS);
		addFirstTalkId(NPCS);
		addTalkId(NPCS);
		addKillId(18342, 20001);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, Player player)
	{
		if (Config.DISABLE_TUTORIAL)
		{
			return null;
		}
		
		String htmltext = event;
		
		QuestState st = player.getQuestState(qn);
		QuestState qs = player.getQuestState(qnTutorial);
		if (qs == null)
			return htmltext;
		
		int Ex = qs.getInt("Ex");
		
		if (event.equalsIgnoreCase("TimerEx_NewbieHelper"))
		{
			if (Ex == 0)
			{
				if (player.isMageClass())
				{
					st.playTutorialVoice("tutorial_voice_009b");
				}
				else
				{
					st.playTutorialVoice("tutorial_voice_009a");
				}
				qs.set("Ex", "1");
			}
			else if (Ex == 3)
			{
				st.playTutorialVoice("tutorial_voice_010a");
				qs.set("Ex", "4");
			}
			return null;
		}
		else if (event.equalsIgnoreCase("TimerEx_GrandMaster"))
		{
			if (Ex >= 4)
			{
				st.showQuestionMark(7);
				st.playSound("ItemSound.quest_tutorial");
				st.playTutorialVoice("tutorial_voice_025");
			}
			return null;
		}
		else
		{
			Object[] map = Events.get(event);
			htmltext = (String) map[0];
			final int radarX = (Integer) map[1];
			final int radarY = (Integer) map[2];
			final int radarZ = (Integer) map[3];
			final int item = (Integer) map[4];
			final int classId1 = (Integer) map[5];
			final int gift1 = (Integer) map[6];
			final int count1 = (Integer) map[7];
			final int classId2 = (Integer) map[8];
			final int gift2 = (Integer) map[9];
			final int count2 = (Integer) map[10];
			if (radarX != 0)
			{
				st.addRadar(radarX, radarY, radarZ);
			}
			if ((st.getQuestItemsCount(item) > 0) && (st.getInt("onlyone") == 0))
			{
				st.rewardExpAndSp(0, 50);
				startQuestTimer("TimerEx_GrandMaster", 60000, null, player, false);
				st.takeItems(item, 1);
				st.set("step", "3");
				if (Ex <= 3)
				{
					qs.set("Ex", "4");
				}
				if (player.getClassId().getId() == classId1)
				{
					st.giveItems(gift1, count1);
					if (gift1 == SPIRITSHOT_NOVICE)
					{
						st.playTutorialVoice("tutorial_voice_027");
					}
					else
					{
						st.playTutorialVoice("tutorial_voice_026");
					}
				}
				else if (player.getClassId().getId() == classId2)
				{
					if (gift2 != 0)
					{
						st.giveItems(gift2, count2);
						st.playTutorialVoice("tutorial_voice_026");
					}
				}
				st.set("step", "4");
				st.set("onlyone", "1");
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, Player player)
	{
		if (Config.DISABLE_TUTORIAL)
		{
			return null;
		}
		
		String htmltext = "";
		QuestState st = player.getQuestState(qn);
		QuestState qs = player.getQuestState(qnTutorial);
		
		if ((qs == null) || qs.isCompleted())
		{
			npc.showChatWindow(player);
			return null;
		}
		
		if (st == null)
		{
			st = newQuestState(player);
		}
		
		int onlyone = st.getInt("onlyone");
		int Ex = qs.getInt("Ex");
		int step = st.getInt("step");
		Talk talk = Talks.get(npc.getNpcId());
		
		if (((player.getLevel() >= 10) || (onlyone != 0)) && (talk.npcTyp == 1))
		{
			htmltext = "30575-05.htm";
		}
		else if ((onlyone == 0) && (player.getLevel() < 10))
		{
			if ((talk != null) && (player.getRace().ordinal() == talk.raceId))
			{
				htmltext = talk.htmlfiles[0];
				if (talk.npcTyp == 1)
				{
					if ((step == 0) && (Ex < 0))
					{
						qs.set("Ex", "0");
						startQuestTimer("TimerEx_NewbieHelper", 30000, null, player, false);
						if (player.isMageClass())
						{
							st.set("step", "1");
							st.setState(STATE_STARTED);
						}
						else
						{
							htmltext = "30530-01.htm";
							st.set("step", "1");
							st.setState(STATE_STARTED);
						}
					}
					else if ((step == 1) && (st.getQuestItemsCount(talk.item) == 0) && (Ex <= 2))
					{
						if (st.getQuestItemsCount(BLUE_GEM) != 0)
						{
							st.takeItems(BLUE_GEM, -1);
							st.giveItems(talk.item, 1);
							st.set("step", "2");
							qs.set("Ex", "3");
							startQuestTimer("TimerEx_NewbieHelper", 30000, null, player, false);
							qs.set("ucMemo", "3");
							if (player.isMageClass())
							{
								st.playTutorialVoice("tutorial_voice_027");
								st.giveItems(SPIRITSHOT_NOVICE, 100);
								htmltext = talk.htmlfiles[2];
								if (htmltext.equals(""))
								{
									htmltext = "<html><body>I am sorry.  I only help warriors.  Please go to another Newbie Helper who may assist you.</body></html>";
								}
							}
							else
							{
								st.playTutorialVoice("tutorial_voice_026");
								st.giveItems(SOULSHOT_NOVICE, 200);
								htmltext = talk.htmlfiles[1];
								if (htmltext.equals(""))
								{
									htmltext = "<html><body>I am sorry.  I only help warriors.  Please go to another Newbie Helper who may assist you.</body></html>";
								}
							}
							
							st.giveItems(TOKEN, 12);
							if (Rnd.get(2) == 0)
								st.giveItems(SCROLL, 2);
						}
						else
						{
							if (player.isMageClass())
							{
								htmltext = "30131-02.htm";
							}
							if (player.getRace().ordinal() == 3)
							{
								htmltext = "30575-02.htm";
							}
							else
							{
								htmltext = "30530-02.htm";
							}
						}
					}
					else if (step == 2)
					{
						htmltext = talk.htmlfiles[3];
					}
				}
				else if (talk.npcTyp == 0)
				{
					if (step == 1)
					{
						htmltext = talk.htmlfiles[0];
					}
					else if (step == 2)
					{
						htmltext = talk.htmlfiles[1];
					}
					else if (step == 3)
					{
						htmltext = talk.htmlfiles[2];
					}
				}
			}
		}
		else if (step == 4)
		{
			if (player.getLevel() < 10)
			{
				htmltext = npc.getNpcId() + "-04.htm";
			}
		}
		
		if ((htmltext == null) || htmltext.equals(""))
		{
			npc.showChatWindow(player);
		}
		
		npc.showChatWindow(player);
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, Player player, boolean isSummon)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return null;
		}
		
		QuestState qs = player.getQuestState(qnTutorial);
		if (qs == null)
		{
			return null;
		}
		
		int Ex = qs.getInt("Ex");
		
		if (Ex <= 1)
		{
			st.playTutorialVoice("tutorial_voice_011");
			st.showQuestionMark(3);
			qs.set("Ex", "2");
		}
		
		if ((Ex <= 2) && (st.getQuestItemsCount(BLUE_GEM) < 1))
		{
			if (Rnd.get(100) < 100)
			{
				((L2MonsterInstance) npc).dropItem(player, BLUE_GEM, 1);
				st.playSound("ItemSound.quest_tutorial");
			}
		}
		return null;
	}
	
//	public static void main(String[] args)
//	{
//		new Q999_T1Tutorial(999, qn, "T1Tutorial");
//	}
}