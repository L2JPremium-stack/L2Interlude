package net.sf.l2j.gameserver.instancemanager.custom;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPool;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;

public class AutoGoldBar implements Runnable {

    private final Map<Player, Long> _players = new ConcurrentHashMap<>();
    private ScheduledFuture<?> _task = null;

    protected AutoGoldBar() {
        // Apenas inicializa, sem print
    }

    @Override
    public final void run() {
        if (_players.isEmpty()) {
            stopTask();
            return;
        }

        for (Map.Entry<Player, Long> entry : _players.entrySet()) {
            final Player player = entry.getKey();

            if (player == null || player.isOff() || player.isOffShop()) {
                remove(player);
                continue;
            }

            long adenaCount = player.getInventory().getInventoryItemCount(57, 0);

            if (adenaCount >= Config.BANKING_SYSTEM_ADENA) {
                player.getInventory().reduceAdena("Goldbar", Config.BANKING_SYSTEM_ADENA, player, null);
                player.getInventory().addItem("Goldbar", Config.ID_NEW_GOLD_BAR, Config.BANKING_SYSTEM_GOLDBARS, player, null);
                player.getInventory().updateDatabase();
                player.sendPacket(new ItemList(player, false));
            } else {
                remove(player);
            }
        }
    }

    public final void add(Player player) {
        if (player == null || player.isOff()) {
            return;
        }

        long adenaCount = player.getInventory().getInventoryItemCount(57, 0);
        if (adenaCount >= Config.BANKING_SYSTEM_ADENA) {
            boolean wasEmpty = _players.isEmpty();

            _players.put(player, System.currentTimeMillis());

            if (wasEmpty || _task == null || _task.isCancelled()) {
                _task = ThreadPool.scheduleAtFixedRate(this, 1000, 1000);
            }
        }
    }

    public final void remove(Creature player) {
        if (player == null) return;

        _players.remove(player);

        if (_players.isEmpty()) {
            stopTask();
        }
    }

    private void stopTask() {
        if (_task != null) {
            _task.cancel(false);
            _task = null;
        }
    }

    public static final AutoGoldBar getInstance() {
        return SingletonHolder._instance;
    }

    private static class SingletonHolder {
        protected static final AutoGoldBar _instance = new AutoGoldBar();
    }
}
