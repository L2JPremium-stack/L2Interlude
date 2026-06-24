package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.commons.lang.Tokenizer;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.solofarm.data.SoloFarmData;
import net.sf.l2j.solofarm.holder.SoloFarmConfig;
import net.sf.l2j.solofarm.instancemanager.SoloFarmManager;

public class VoicedSoloFarm implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"solofarm"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player player, String target)
	{
		if (command.startsWith("solofarm"))
		{
			final Tokenizer tokenizer = new Tokenizer(command);
			final String param = tokenizer.getToken(1);
			if (param == null)
			{
				SoloFarmManager.getInstance().showMainPage(player);
				return true;
			}
			switch (param.toLowerCase())
			{
				case "index":
				{
					SoloFarmManager.getInstance().showMainPage(player);
					break;
				}
				case "info":
				{
					SoloFarmManager.getInstance().sendInfoSoloFarm(player);
					break;
				}
				case "status":
				{
					SoloFarmManager.getInstance().sendDetalhesSoloFarm(player);
					break;
				}
				case "enter":
				{
					SoloFarmManager.getInstance().enter(player);
					break;
				}
				case "running":
				{
					SoloFarmManager.getInstance().sendTutorial(player);
					break;
				}
				case "buy_500":
				{
					SoloFarmManager.getInstance().buyMonsters(player, 500);
					SoloFarmManager.getInstance().showMainPage(player);
					break;
				}
				case "buy_1000":
				{
					SoloFarmManager.getInstance().buyMonsters(player, 1000);
					SoloFarmManager.getInstance().showMainPage(player);
					break;
				}
				case "buy_custom":
				{
				    if (tokenizer.size() < 3)
				    {
				        player.sendMessage("Usage: .solofarm buy_custom <amount>");
				        return true;
				    }

				    final int amount = tokenizer.getAsInteger(2, -1);
				    final SoloFarmConfig config = SoloFarmData.getInstance().getConfig();

				    if (amount <= 0)
				    {
				        player.sendMessage("Invalid amount.");
				        return true;
				    }

				    if (amount < config.getMinBuy() || amount > config.getMaxBuy())
				    {
				        player.sendMessage("Amount must be between " + config.getMinBuy() + " and " + config.getMaxBuy());
				        return true;
				    }

				    SoloFarmManager.getInstance().buyMonsters(player, amount);
				    SoloFarmManager.getInstance().showMainPage(player);

				    break;
				}
			}
		}
		return false;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}