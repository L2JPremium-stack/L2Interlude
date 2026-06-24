package net.sf.l2j.gameserver.util;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class FloodProtectors
{
	public static enum Action
	{
		ROLL_DICE(Config.ROLL_DICE_TIME),
		HERO_VOICE(Config.HERO_VOICE_TIME),
		USER_ITEM(Config.USER_ITEM_TIME),
		USER_ITEM_ENCHANT(Config.USER_ITEM_TIME_ENCHANT),
		USER_ENCHANT_SKILL(Config.USER_TIME_ENCHANT_SKILL * 1000),
		USER_ARGUMENT_ITEM(Config.USER_TIME_ARGUMENT_ITEM * 1000),
		SUBCLASS(Config.SUBCLASS_TIME),
		DROP_ITEM(Config.DROP_ITEM_TIME),
		SERVER_BYPASS(Config.SERVER_BYPASS_TIME),
		MULTISELL(Config.MULTISELL_TIME),
		MANUFACTURE(Config.MANUFACTURE_TIME),
		MANOR(Config.MANOR_TIME),
		SENDMAIL(Config.SENDMAIL_TIME),
		CHARACTER_SELECT(Config.CHARACTER_SELECT_TIME),
		GLOBAL_CHAT(Config.GLOBAL_CHAT_TIME),
		TRADE_CHAT(Config.TRADE_CHAT_TIME),
		SOCIAL(Config.SOCIAL_TIME),
		GLOBAL_VIP(Config.GLOBAL_VIP_TIME * 1000);
		
		
		private final int _reuseDelay;
		
		private Action(int reuseDelay)
		{
			_reuseDelay = reuseDelay;
		}
		
		public int getReuseDelay()
		{
			return _reuseDelay;
		}
		
		public static final int VALUES_LENGTH = Action.values().length;
	}
	
	/**
	 * Try to perform an action according to client FPs value. A 0 reuse delay means the action is always possible.
	 * @param client : The client to check protectors on.
	 * @param action : The action to track.
	 * @return True if the action is possible, False otherwise.
	 */
	public static boolean performAction(L2GameClient client, Action action)
	{
		final int reuseDelay = action.getReuseDelay();
		if (reuseDelay == 0)
			return true;
		
		long[] value = client.getFloodProtectors();
		
		synchronized (value)
		{
			if (value[action.ordinal()] > System.currentTimeMillis())
				return false;
			
			value[action.ordinal()] = System.currentTimeMillis() + reuseDelay;
			return true;
		}
	}
}