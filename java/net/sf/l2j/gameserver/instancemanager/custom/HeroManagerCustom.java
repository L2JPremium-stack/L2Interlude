package net.sf.l2j.gameserver.instancemanager.custom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ConnectionPool;
import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;

/**
 * @author rapfersan92
 */
public class HeroManagerCustom
{
    private static final Logger _log = Logger.getLogger(HeroManagerCustom.class.getName());

    private final Map<Integer, Long> _heros;
    protected final Map<Integer, Long> _herosTask;
    private ScheduledFuture<?> _scheduler;

    public static HeroManagerCustom getInstance()
    {
        return SingletonHolder._instance;
    }

    protected HeroManagerCustom()
    {
        _heros = new ConcurrentHashMap<>();
        _herosTask = new ConcurrentHashMap<>();
        startScheduler();
        load();
    }

    public void reload()
    {
        _heros.clear();
        _herosTask.clear();
        if (_scheduler != null)
            _scheduler.cancel(true);
        startScheduler();
        load();
    }

    private synchronized void startScheduler()
    {
        if (_scheduler != null)
        {
            _scheduler.cancel(false);
            _scheduler = null;
        }
        _scheduler = ThreadPool.scheduleAtFixedRate(new HeroTask(), 1000, 1000);
    }

    public void load()
    {
        try (Connection con = ConnectionPool.getConnection())
        {
            PreparedStatement statement = con.prepareStatement("SELECT objectId, duration FROM character_hero ORDER BY objectId");
            ResultSet rs = statement.executeQuery();
            while (rs.next())
                _heros.put(rs.getInt("objectId"), rs.getLong("duration"));
            rs.close();
            statement.close();
        }
        catch (Exception e)
        {
            _log.warning("Exception: HeroManager load: " + e.getMessage());
        }

        _log.info("HeroManager: Loaded " + _heros.size() + " characters with hero privileges.");
    }

//    public void addHero(int objectId, long duration)
//    {
//        _heros.put(objectId, duration);
//        _herosTask.put(objectId, duration);
//        addHeroPrivileges(objectId, true);
//
//        try (Connection con = ConnectionPool.getConnection())
//        {
//            PreparedStatement statement = con.prepareStatement("INSERT INTO character_hero (objectId, duration) VALUES (?, ?)");
//            statement.setInt(1, objectId);
//            statement.setLong(2, duration);
//            statement.execute();
//            statement.close();
//        }
//        catch (Exception e)
//        {
//            _log.warning("Exception: HeroManager addHero: " + e.getMessage());
//        }
//    }
    public void addHero(int objectId, long duration)
    {
        if (_heros.containsKey(objectId)) {
            // Se o herói já estiver registrado, atualize a duração
            updateHero(objectId, duration);
            return;
        }
        
        _heros.put(objectId, duration);
        _herosTask.put(objectId, duration);
        addHeroPrivileges(objectId, true);

        try (Connection con = ConnectionPool.getConnection()) {
            // Verificar se o herói já existe no banco
            try (PreparedStatement checkStmt = con.prepareStatement("SELECT objectId FROM character_hero WHERE objectId = ?")) {
                checkStmt.setInt(1, objectId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        // Se existir, atualize a duração
                        try (PreparedStatement updateStmt = con.prepareStatement("UPDATE character_hero SET duration = ? WHERE objectId = ?")) {
                            updateStmt.setLong(1, duration);
                            updateStmt.setInt(2, objectId);
                            updateStmt.execute();
                        }
                    } else {
                        // Caso contrário, insira como novo
                        try (PreparedStatement insertStmt = con.prepareStatement("INSERT INTO character_hero (objectId, duration) VALUES (?, ?)")) {
                            insertStmt.setInt(1, objectId);
                            insertStmt.setLong(2, duration);
                            insertStmt.execute();
                        }
                    }
                }
            }
        } catch (Exception e) {
            _log.warning("Exception: HeroManager addHero: " + e.getMessage());
        }
    }

    public void updateHero(int objectId, long duration)
    {
        _heros.put(objectId, duration);
        _herosTask.put(objectId, duration);

        // Envia uma mensagem ao jogador informando sobre a duração do Hero
        final Player player = L2World.getInstance().getPlayer(objectId);
        if (player != null)
        {
            player.sendMessage("Your Hero privileges are updated. You now have " + (duration - System.currentTimeMillis()) / 86400000 + " days left.");
            player.broadcastUserInfo();
        }

        // Atualiza no banco
        try (Connection con = ConnectionPool.getConnection())
        {
            PreparedStatement statement = con.prepareStatement("UPDATE character_hero SET duration = ? WHERE objectId = ?");
            statement.setLong(1, duration);
            statement.setInt(2, objectId);
            statement.execute();
            statement.close();
        }
        catch (Exception e)
        {
            _log.warning("Exception: HeroManager updateHero: " + e.getMessage());
        }
    }

    public void removeHero(int objectId)
    {
        _heros.remove(objectId);
        _herosTask.remove(objectId);
        removeHeroPrivileges(objectId, false);

        try (Connection con = ConnectionPool.getConnection())
        {
            PreparedStatement statement = con.prepareStatement("DELETE FROM character_hero WHERE objectId = ?");
            statement.setInt(1, objectId);
            statement.execute();
            statement.close();
        }
        catch (Exception e)
        {
            _log.warning("Exception: HeroManager removeHero: " + e.getMessage());
        }
    }

    public boolean hasHeroPrivileges(int objectId)
    {
        if (_heros.containsKey(objectId))
        {
            long heroEndTime = _heros.get(objectId);
            return System.currentTimeMillis() < heroEndTime; // Verifica se o Hero ainda é válido
        }
        return false;
    }

    public long getHeroDuration(int objectId)
    {
        return _heros.getOrDefault(objectId, 0L);
    }

    public void addHeroTask(int objectId, long duration)
    {
        _herosTask.put(objectId, duration);
    }

    public void removeHeroTask(int objectId)
    {
        _herosTask.remove(objectId);
    }

    public void addHeroPrivileges(int objectId, boolean apply)
    {
        final Player player = L2World.getInstance().getPlayer(objectId);
        if (player != null)
        {
            player.setHero(true);
            player.setNoble(true, true);  // Definindo como nobre também, caso seja necessário
            player.broadcastUserInfo();
            player.addItem("Hero Item", 6842, 1, player, true); // Adiciona o item herói (exemplo 6842)
        }
    }

    public void removeHeroPrivileges(int objectId, boolean apply)
    {
        final Player player = L2World.getInstance().getPlayer(objectId);
        if (player != null)
        {
            player.setHero(false);
            player.broadcastUserInfo();
            player.getInventory().destroyItemByItemId("", 6842, 1, player, null); // Remove o item herói
        }
    }

    public class HeroTask implements Runnable
    {
        @Override
        public final void run()
        {
            if (_herosTask.isEmpty())
                return;

            for (Map.Entry<Integer, Long> entry : _herosTask.entrySet())
            {
                final long duration = entry.getValue();
                if (System.currentTimeMillis() > duration)
                {
                    final int objectId = entry.getKey();
                    removeHero(objectId);

                    final Player player = L2World.getInstance().getPlayer(objectId);
                    if (player != null)
                    {
                        player.sendPacket(new ExShowScreenMessage("Your Hero privileges were removed.", 10000));
                        player.sendMessage("Your Hero privileges have expired.");
                    }
                }
            }
        }
    }

    private static class SingletonHolder
    {
        protected static final HeroManagerCustom _instance = new HeroManagerCustom();
    }
}
