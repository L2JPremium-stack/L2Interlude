package net.sf.l2j.gameserver.instancemanager.custom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ConnectionPool;

public class RaidBossInfoManager
{
   private static final Logger _log = Logger.getLogger(RaidBossInfoManager.class.getName());
  
   private final Map<Integer, Long> _raidBosses;
  
   public static RaidBossInfoManager getInstance()
   {
       return SingletonHolder._instance;
   }
  
   protected RaidBossInfoManager()
   {
       _raidBosses = new ConcurrentHashMap<>();
       load();
   }
  
   public void load()
   {
       try (Connection con = ConnectionPool.getConnection())
       {
           PreparedStatement statement;
           ResultSet rs;
          
           statement = con.prepareStatement("SELECT boss_id, respawn_time FROM grandboss_data UNION SELECT boss_id, respawn_time FROM raidboss_spawnlist ORDER BY boss_id");
           rs = statement.executeQuery();
           while (rs.next())
           {
               int bossId = rs.getInt("boss_id");
               if (Config.LIST_RAID_BOSS_IDS.contains(bossId) || Config.LIST_GRAND_BOSS_IDS.contains(bossId))
                   _raidBosses.put(bossId, rs.getLong("respawn_time"));
           }
           rs.close();
           statement.close();
       }
       catch (Exception e)
       {
           _log.warning("Exception: RaidBossInfoManager load: " + e);
       }
      
       _log.info("RaidBossInfoManager: Loaded " + _raidBosses.size() + " instances.");
   }
  
   public void updateRaidBossInfo(int bossId, long respawnTime)
   {
       _raidBosses.put(bossId, respawnTime);
   }
  
   // Alteração no método getRaidBossRespawnTime para tratar o valor null e logar o ID do boss, mas ignorar e continuar o processamento
   public long getRaidBossRespawnTime(int bossId)
   {
       Long respawnTime = _raidBosses.get(bossId);
       if (respawnTime == null)
       {
           // Logando o ID do boss que não foi encontrado, mas não interrompendo o fluxo
           _log.warning("RaidBossInfoManager: Respawn time for bossId " + bossId + " not found in the map. Ignoring this boss.");

           // Retorna um valor padrão de 0L (pode ser ajustado conforme sua necessidade) e ignora o bossId
           return 0L; // Ou você pode retornar outro valor de sua escolha, como -1L
       }
       return respawnTime;
   }
  
   private static class SingletonHolder
   {
       protected static final RaidBossInfoManager _instance = new RaidBossInfoManager();
   }
}
