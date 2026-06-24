package net.sf.l2j.gameserver.model.actor.instance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import net.sf.l2j.Config;
import net.sf.l2j.clan.ranking.ClanRankingConfig;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.event.bossevent.KTBConfig;
import net.sf.l2j.event.partyfarm.RewardHolder;
import net.sf.l2j.event.soloboss.SoloBoss;
import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.instancemanager.RaidBossPointsManager;
import net.sf.l2j.gameserver.instancemanager.RaidBossSpawnManager;
import net.sf.l2j.gameserver.instancemanager.RaidBossSpawnManager.StatusEnum;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.entity.Hero;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.itemcontainer.Inventory;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.taskmanager.PvpFlagTaskManager;
import net.sf.l2j.gameserver.util.Broadcast;
import net.sf.l2j.upgrade.UpgradeItem;
import net.sf.l2j.upgrade.UpgradeItemData;

/**
 * This class manages all RaidBoss. In a group mob, there are one master called RaidBoss and several slaves called Minions.
 */
public class L2RaidBossInstance extends L2MonsterInstance
{
	private StatusEnum _raidStatus;
	protected ScheduledFuture<?> _maintenanceTask;
	
	/**
	 * Constructor of L2RaidBossInstance (use L2Character and L2NpcInstance constructor).
	 * <ul>
	 * <li>Call the L2Character constructor to set the _template of the L2RaidBossInstance (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR)</li>
	 * <li>Set the name of the L2RaidBossInstance</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it</li>
	 * </ul>
	 * @param objectId Identifier of the object to initialized
	 * @param template L2NpcTemplate to apply to the NPC
	 */
	public L2RaidBossInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
		setIsRaid(true);
	}
	
	@Override
	public void onSpawn()
	{
		setIsNoRndWalk(true);
		super.onSpawn();
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
			return false;
		
		if (_maintenanceTask != null)
		{
			_maintenanceTask.cancel(false);
			_maintenanceTask = null;
		}
		
		if (killer != null)
		{
		    final Player player = killer.getActingPlayer();
		    if (player != null)
		    {
		        if (Config.SOLO_BOSS_EVENT && SoloBoss.is_started())
		        {
		            // Variáveis para o próximo Boss ID e suas coordenadas
		            final int nextBossId;
		            final int[] nextBossLoc;

		            // Condicional para o controle de sequência dos spawns dos bosses
		            if (getNpcId() == Config.SOLO_BOSS_ID_ONE)
		            {
		                nextBossId = Config.SOLO_BOSS_ID_TWO;
		                nextBossLoc = Config.SOLO_BOSS_ID_TWO_LOC; // Localização do Boss 2

		                // Agendar o spawn do próximo boss (Boss 2) após o intervalo de 2 a 3 segundos
		                ThreadPool.schedule(() -> {
		                    SoloBoss.SpawnRaid(nextBossId, nextBossLoc);
		                }, Rnd.get(2000, 3000)); // Tempo aleatório entre 2 a 3 segundos
		            }
		            else if (getNpcId() == Config.SOLO_BOSS_ID_TWO)
		            {
		                nextBossId = Config.SOLO_BOSS_ID_THREE;
		                nextBossLoc = Config.SOLO_BOSS_ID_THREE_LOC; // Localização do Boss 3

		                // Agendar o spawn do próximo boss (Boss 3) após o intervalo de 2 a 3 segundos
		                ThreadPool.schedule(() -> {
		                	 SoloBoss.SpawnRaid(nextBossId, nextBossLoc);
		                }, Rnd.get(2000, 3000)); // Tempo aleatório entre 2 a 3 segundos
		            }
		            else if (getNpcId() == Config.SOLO_BOSS_ID_THREE)
		            {
		                nextBossId = Config.SOLO_BOSS_ID_FOUR;
		                nextBossLoc = Config.SOLO_BOSS_ID_FOUR_LOC; // Localização do Boss 4

		                // Agendar o spawn do próximo boss (Boss 4) após o intervalo de 2 a 3 segundos
		                ThreadPool.schedule(() -> {
		                	 SoloBoss.SpawnRaid(nextBossId, nextBossLoc);
		                }, Rnd.get(2000, 3000)); // Tempo aleatório entre 2 a 3 segundos
		            }
		            else if (getNpcId() == Config.SOLO_BOSS_ID_FOUR)
		            {
		                nextBossId = Config.SOLO_BOSS_ID_FIVE;
		                nextBossLoc = Config.SOLO_BOSS_ID_FIVE_LOC; // Localização do Boss 5

		                // Agendar o spawn do próximo boss (Boss 5) após o intervalo de 2 a 3 segundos
		                ThreadPool.schedule(() -> {
		                	 SoloBoss.SpawnRaid(nextBossId, nextBossLoc);
		                }, Rnd.get(2000, 3000)); // Tempo aleatório entre 2 a 3 segundos
		            }
		            else if (getNpcId() == Config.SOLO_BOSS_ID_FIVE)
		            {
		                // Finaliza o evento quando o último Boss é morto
		                ThreadPool.schedule(() -> SoloBoss.Finish_Event(), 1000);
		                SoloBoss._started = false;
		                SoloBoss._finish = true;
		                SoloBoss._aborted = true;
		            }
		        }
				
		        if (ClanRankingConfig.ENABLE_CLAN_RANKING)
		        {
		            if (ClanRankingConfig.CLAN_RANKING_BOSS_POINTS_MAIN_KILLER.containsKey(Integer.valueOf(getNpcId())))
		            {
		                L2Clan clan = player.getClan();
		                if (clan != null)
		                {
		                    int points = ClanRankingConfig.CLAN_RANKING_BOSS_POINTS_MAIN_KILLER.get(Integer.valueOf(getNpcId())).intValue();
		                    clan.addRankingBossPoints(points);

		                    if (ClanRankingConfig.ENABLE_ANNOUNCE_POINTS_EARNED_ONKILL)
		                        Broadcast.gameAnnounceToOnlinePlayers("Clan " + clan.getName() + " gained " + points + " points defeating a Raid!");
		                }
		            }
		        }

				if (Config.NOBLESS_FROM_BOSS)
				{
					if (getNpcId() == Config.BOSS_ID)
					{
						if (player.getParty() != null)
						{
							for (Player member : player.getParty().getPartyMembers())
							{
								if (member.isNoble() == true)
								{
									member.sendMessage("Your party gained nobless status for defeating " + getName() + "!");
								}
								else if (member.isInsideRadius(getX(), getY(), getZ(), Config.RADIUS_TO_RAID, false, false))
								{
									member.setNoble(true, true);
									member.addItem("Quest", 7694, 1, member, true);
									member.broadcastPacket(new SocialAction(player, 16));
									NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
									html.setHtml("<html><body>Congratulations, you're now a noble!<br1>Open the Skills & Magic (ALT+K) to see your acquired abilities.</body></html>");
									member.sendPacket(html);
								}
								else
								{
									member.sendMessage("Your party killed " + getName() + "! But you were to far...");
								}
							}
						}
						else if (player.getParty() == null && !player.isNoble())
						{
							player.setNoble(true, true);
							player.addItem("Quest", 7694, 1, player, true);
							player.broadcastPacket(new SocialAction(player, 16));
							NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
							html.setHtml("<html><body>Congratulations, you're now a noble!<br1>Open the Skills & Magic (ALT+K) to see your acquired abilities.</body></html>");
							player.sendPacket(html);
						}
					}
				}
				
				Inventory inv = player.getInventory();

				// Cria uma lista para juntar inventário + equipado
				List<ItemInstance> allItems = new ArrayList<>();

				// Adiciona itens do inventário
				allItems.addAll(inv.getItems());

				// Adiciona itens equipados
				for (ItemInstance equippedItem : player.getInventory().getPaperdollItems())
				{
				    if (equippedItem != null)
				        allItems.add(equippedItem);
				}

				// Ordena a lista pelo enchant level decrescente
				Collections.sort(allItems, new Comparator<ItemInstance>() {
				    @Override
				    public int compare(ItemInstance i1, ItemInstance i2) {
				        return Integer.compare(i2.getEnchantLevel(), i1.getEnchantLevel()); // decrescente
				    }
				});

				boolean upgraded = false;

				for (ItemInstance item : allItems)
				{
				    if (upgraded)
				        break;

				    UpgradeItem upgrade = UpgradeItemData.getInstance().getUpgrade(item.getItemId(), getNpcId());
				    if (upgrade == null)
				        continue;

				    if (Rnd.get(100) < upgrade.getChance())
				    {
				        int enchantLevel = item.getEnchantLevel();

				        // Remove o item (do inventário ou equipado)
				        inv.destroyItem("UpgradeSuccess", item, player, this);

				        // Adiciona o novo item upgradeado
				        ItemInstance newItem = inv.addItem("UpgradeSuccess", upgrade.getNewItemId(), 1, player, this);
				        if (newItem != null)
				        {
				            newItem.setEnchantLevel(enchantLevel);

				            InventoryUpdate iu = new InventoryUpdate();
				            iu.addModifiedItem(newItem);
				            player.sendPacket(iu);

				            player.broadcastPacket(new SocialAction(player, 16));

				            String playerName = player.getName();
				            String itemName = newItem.getName();
				            int newEnchantLevel = newItem.getEnchantLevel();

				            String enchantText = (newEnchantLevel > 0) ? "+" + newEnchantLevel + " " : "";

				            for (Player character : L2World.getInstance().getPlayers())
				            {
				                character.sendPacket(new ExShowScreenMessage(
				                    "Player " + playerName + " successfully upgraded the item " + enchantText + itemName + "!",
				                    7 * 1000,
				                    ExShowScreenMessage.SMPOS.TOP_CENTER,
				                    true
				                ));
				            }
				        }
				        else
				        {
				            player.sendMessage("Error: Could not add the upgraded item.");
				        }

				        upgraded = true; // já fez upgrade, sai do loop
				    }
				    else
				    {
				        player.sendMessage("Failed Upgrade Item :(");
				        upgraded = true; // considera item processado mesmo falhando
				    }
				}

				if (Config.ANNOUNCE_RAIDBOS_KILL && !(getNpcId() == Config.SOLO_BOSS_ID_ONE) && !(getNpcId() == Config.SOLO_BOSS_ID_TWO) && !(getNpcId() == Config.SOLO_BOSS_ID_THREE) && !(getNpcId() == Config.SOLO_BOSS_ID_FOUR) && !(getNpcId() == Config.SOLO_BOSS_ID_FIVE))
				{
					if (player.getClan() != null)
						Broadcast.gameAnnounceToOnlinePlayers("Boss: " + getName() + " was killed by " + player.getName()+ " of the clan: " + player.getClan().getName());
					else
						Broadcast.gameAnnounceToOnlinePlayers("Boss: " + getName() + " was killed by " + player.getName());
				}
				if(SoloBoss.is_started())
				{
					if ((getNpcId() == Config.SOLO_BOSS_ID_ONE) || (getNpcId() == Config.SOLO_BOSS_ID_TWO) || (getNpcId() == Config.SOLO_BOSS_ID_THREE) || (getNpcId() == Config.SOLO_BOSS_ID_FOUR) || (getNpcId() == Config.SOLO_BOSS_ID_FIVE))
					{
						for(Player character : L2World.getInstance().getPlayers())
						{
							character.sendPacket(new ExShowScreenMessage("[Solo Boss]: " + getName() + " was killed by " + player.getName(), 3 * 1000, ExShowScreenMessage.SMPOS.TOP_CENTER, true));
						}
						
					}
					
				}
				
				if (Config.ACTIVE_MISSION && Config.BOSS_LIST_MISSION.contains(Integer.valueOf(getTemplate().getNpcId())))
				{
					if (!player.check_obj_mission(player.getObjectId()))
						player.updateMission(); 
					if (!player.isRaidCompleted() && player.getRaidCont() < Config.MISSION_RAID_CONT)
						player.setRaidCont(player.getRaidCont() + 1);
				}
				broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.RAID_WAS_SUCCESSFUL));
				broadcastPacket(new PlaySound("systemmsg_e.1209"));
				
				if (player.isInParty())
				{
					for (Player member : player.getParty().getPartyMembers())
					{
						RaidBossPointsManager.getInstance().addPoints(member, getNpcId(), (getLevel() / 2) + Rnd.get(-5, 5));
						if (member.isNoble())
							Hero.getInstance().setRBkilled(member.getObjectId(), getNpcId());
					}
				}
				else
				{
					RaidBossPointsManager.getInstance().addPoints(player, getNpcId(), (getLevel() / 2) + Rnd.get(-5, 5));
					if (player.isNoble())
						Hero.getInstance().setRBkilled(player.getObjectId(), getNpcId());
				}
			}
		}
		if(Config.ALLOW_FLAG_ONKILL_BY_ID)
		{
			updatePvpFlagById();
		}
		if (Config.SOLO_BOSS_EVENT)
		{
			rewardSoloEventPlayer();
		}
		if (!getSpawn().is_customBossInstance())
		RaidBossSpawnManager.getInstance().updateStatus(this, true);
		return true;
	}
		
	@Override
	public void deleteMe()
	{
		if (_maintenanceTask != null)
		{
			_maintenanceTask.cancel(false);
			_maintenanceTask = null;
		}
		
		super.deleteMe();
	}
	private static final Map<Integer, List<RewardHolder>> rewardsMap = new HashMap<>();

	static
	{
	    rewardsMap.put(Config.SOLO_BOSS_ID_ONE, Config.SOLO_BOSS_REWARDS_ONE);
	    rewardsMap.put(Config.SOLO_BOSS_ID_TWO, Config.SOLO_BOSS_REWARDS_TWO);
	    rewardsMap.put(Config.SOLO_BOSS_ID_THREE, Config.SOLO_BOSS_REWARDS_THREE);
	    rewardsMap.put(Config.SOLO_BOSS_ID_FOUR, Config.SOLO_BOSS_REWARDS_FOUR);
	    rewardsMap.put(Config.SOLO_BOSS_ID_FIVE, Config.SOLO_BOSS_REWARDS_FIVE);
	}

	private void rewardSoloEventPlayer()
	{
	    int npcId = getNpcId();

	    // Verifica se o NPC é um Solo Boss válido usando o Map
	    if (rewardsMap.containsKey(npcId))
	    {
	        List<String> rewardedHWID = new ArrayList<>();

	        // Itera sobre os jogadores próximos
	        for (Player player : getKnownList().getKnownTypeInRadius(Player.class, Config.RANGE_SOLO_BOSS)) 
	        {
	            String playerIp = player.getHWID();

	            // Evita recompensar o mesmo jogador duas vezes
	            if (rewardedHWID.contains(playerIp)) continue;
	            rewardedHWID.add(playerIp);

	            // Aplica recompensas para o jogador
	            applyRewardsToPlayer(player, npcId);
	        }
	    }
	}

	// Aplica as recompensas para o jogador dependendo do NPC
	private static void applyRewardsToPlayer(Player player, int npcId) {
	    List<RewardHolder> rewards = rewardsMap.get(npcId);

	    // Itera sobre as recompensas
	    for (RewardHolder reward : rewards) {
	        if (Rnd.get(100) <= reward.getRewardChance()) {
	            // Define a quantidade de recompensa
	            int quantity = Rnd.get(reward.getRewardMin(), reward.getRewardMax());

	            // Se o jogador for VIP, aplica o VIP_DROP_RATE
	            if (player.isVip()) {
	                quantity = (int) (quantity * Config.VIP_DROP_RATE);
	            }

	            // Adiciona a recompensa ao inventário do jogador
	            player.addItem("Solo Reward", reward.getRewardId(), quantity, player, true);
	        }
	    }

	    // Transmite o uso de magia
	    broadcastSkillUse(player);
	}

	// Transmite a magia de recompensa para o jogador
	private static void broadcastSkillUse(Player player)
	{
	    MagicSkillUse MSU = new MagicSkillUse(player, player, 2023, 1, 1, 0);
	    player.broadcastPacket(MSU);
	}

	@Override
	protected void startMaintenanceTask() {
	 //   System.out.println("startMaintenanceTask() foi chamado!");

	    super.startMaintenanceTask();

	    if (_maintenanceTask != null) {
	        _maintenanceTask.cancel(false);
	        _maintenanceTask = null;
	    }

	    _maintenanceTask = ThreadPool.scheduleAtFixedRate(
	        this::checkAndReturnToSpawn,
	        1_000L,  // delay inicial em milissegundos
	        1_000L   // intervalo entre execuções
	    );
	}


	protected void checkAndReturnToSpawn()
	{
	    final int npcId = getNpcId();

	    // Verificações iniciais
	    if (isDead() || isMovementDisabled())
	        return;

	    // Verifica se o NPC está na lista de exceções
	    if (npcId == 29095 ||
	        npcId == Config.SOLO_BOSS_ID_ONE ||
	        npcId == Config.SOLO_BOSS_ID_TWO ||
	        npcId == Config.SOLO_BOSS_ID_THREE ||
	        npcId == Config.SOLO_BOSS_ID_FOUR ||
	        npcId == Config.SOLO_BOSS_ID_FIVE ||
	        (KTBConfig.LIST_KTB_EVENT_BOSS_ID != null && KTBConfig.LIST_KTB_EVENT_BOSS_ID.contains(npcId)))
	    {
	        return;
	    }

	    final L2Spawn spawn = getSpawn();
	    if (spawn == null)
	        return;

	    // Verifica se está dentro do raio permitido — se sim, nem continua
	    if (isInsideRadius(spawn.getLocX(), spawn.getLocY(), spawn.getLocZ(), Config.RANGE_BOSS_LIMIT, true, false))
	        return;

	    // Fora do range: faz o teleport e cura se necessário
	//    System.out.println("NPC ID " + npcId + " está fora do range. Teleportando para o spawn.");
	 //   if (getAggroList().isEmpty())
	//    {
	    	  teleToLocation(spawn.getLoc(), 0);
	//    }

	    if (Config.ENABLE_FULL_HP_RECALL_BOSS)
	    {
	   //     System.out.println("Curando completamente NPC ID: " + npcId);
	        healFull();
	    }
	}
	
	public void setRaidStatus(StatusEnum status)
	{
		_raidStatus = status;
	}
	
	public StatusEnum getRaidStatus()
	{
		return _raidStatus;
	}
	private void updatePvpFlagById()
	{
		if (Config.NPCS_FLAG_LIST.contains(getNpcId()))
		{
			for (Player playerInRadius : getKnownList().getKnownTypeInRadius(Player.class, Config.NPCS_FLAG_RANGE)) 
			{
				final L2Party party = playerInRadius.getParty();
				if (party != null)
				{
					for (Player member : party.getPartyMembers())
					{
						PvpFlagTaskManager.getInstance().add(member, 60000);
					}
				}
				else
					PvpFlagTaskManager.getInstance().add(playerInRadius, 60000);
			}
		}		
	}

	public void healFull()
	{
		super.setCurrentHp(super.getMaxHp());
		super.setCurrentMp(super.getMaxMp());
	}
}