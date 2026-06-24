package net.sf.l2j.itemstime;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ConnectionPool;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class TimedItemManager {

	public final ConcurrentMap<Integer, Info> _timedItems = new ConcurrentHashMap<>();
    private static final Logger _log = Logger.getLogger(TimedItemManager.class.getName());
    
    public class Info {
        int _charId;
        int _itemId;
        public long _activationTime;
    }

    public static final TimedItemManager getInstance() {
        return SingletonHolder._instance;
    }

    private static class SingletonHolder {
        protected static final TimedItemManager _instance = new TimedItemManager();
    }

    public TimedItemManager() {
        restore();
        _startControlTask.schedule(60000);  // Run every 60 seconds
    }

    public boolean getActiveTimed(Player pl, boolean trade) {
        for (Info i : _timedItems.values()) {
            if (i != null && i._charId == pl.getObjectId()) {
                ItemInstance item = pl.getInventory().getItemByObjectId(i._itemId);
                if (item != null && System.currentTimeMillis() < i._activationTime) {
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized void destroy(ItemInstance item) {
        Info inf = _timedItems.remove(item.getObjectId());
        if (inf != null) {
            try (Connection con = ConnectionPool.getConnection()) {
                try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_timed_items WHERE charId = ? AND itemId = ?")) {
                    statement.setInt(1, inf._charId);
                    statement.setInt(2, inf._itemId);
                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                _log.warning("Failed to delete timed item from DB: " + e.getMessage());
            }
        }
    }

    public synchronized void setTimed(ItemInstance item)
    {
        long duration = TimeItemData.getInstance().getDuration(item.getItemId());

        // Se o item não estiver configurado, ignora.
        if (duration <= 0)
        {
            _log.warning("Item ID " + item.getItemId() + " não está configurado no TimeItems.xml. Ignorando.");
            return;
        }

        Info inf = _timedItems.get(item.getObjectId());

        if (inf == null)
        {
            inf = new Info();
            inf._itemId = item.getObjectId();
            inf._charId = item.getOwnerId();
            inf._activationTime = (System.currentTimeMillis() / 1000) + duration;

            _timedItems.put(inf._itemId, inf);

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            @SuppressWarnings("unused")
			String formattedDate = sdf.format(new java.util.Date(inf._activationTime * 1000));
    //        _log.info("Item temporário " + item.getItemId() + " adicionado para o player " + inf._charId + " até " + formattedDate);
        }
        else
        {
            inf._charId = item.getOwnerId();
        }

        long currentTime = System.currentTimeMillis() / 1000;
        if (inf._activationTime < currentTime)
        {
            delete(inf);
        }
        else
        {
            saveToDb(inf);
        }
    }



    public boolean isActive(ItemInstance item) {
        return _timedItems.containsKey(item.getObjectId());
    }

    private void restore() {
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement statement = con.prepareStatement("SELECT charId, itemId, time FROM character_timed_items");
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                Info inf = new Info();
                inf._charId = rs.getInt("charId");
                inf._itemId = rs.getInt("itemId");
                inf._activationTime = rs.getLong("time");
                _timedItems.put(inf._itemId, inf);
            }
            _log.info("TimedItems: loaded " + _timedItems.size() + " items.");
        } catch (SQLException e) {
            _log.warning("Failed to restore timed items from DB: " + e.getMessage());
        }
    }

    private static void saveToDb(Info temp) {
        try (Connection con = ConnectionPool.getConnection()) {
            try (PreparedStatement statement = con.prepareStatement("UPDATE character_timed_items SET charId = ? WHERE itemId = ?")) {
                statement.setInt(1, temp._charId);
                statement.setInt(2, temp._itemId);
                if (statement.executeUpdate() == 0) {
                    try (PreparedStatement insertStmt = con.prepareStatement("INSERT INTO character_timed_items (charId, itemId, time) VALUES (?, ?, ?)")) {
                        insertStmt.setInt(1, temp._charId);
                        insertStmt.setInt(2, temp._itemId);
                        insertStmt.setLong(3, temp._activationTime);
                        insertStmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            _log.warning("Failed to save timed item to DB: " + e.getMessage());
        }
    }

    public void delete(Info temp) {
        _timedItems.remove(temp._itemId);

        Player pl = L2World.getInstance().getPlayer(temp._charId);
        if (pl != null) {
            ItemInstance item = pl.getInventory().getItemByObjectId(temp._itemId);
            if (item != null) {
                // Remover o item do inventário antes de fazer qualquer atualização no banco de dados
                if (item.isEquipped()) {
                    pl.getInventory().unEquipItemInSlot(item.getLocationSlot());
                }
                pl.getInventory().destroyItem("timeLost", item, pl, pl);
                pl.sendPacket(new ItemList(pl, false));

                SystemMessage msg = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
                msg.addItemName(item.getItemId());
                pl.sendPacket(msg);

                // Agora, podemos remover do banco de dados
                try (Connection con = ConnectionPool.getConnection()) {
                    try (PreparedStatement statement = con.prepareStatement("DELETE FROM character_timed_items WHERE charId = ? AND itemId = ?")) {
                        statement.setInt(1, temp._charId);
                        statement.setInt(2, temp._itemId);
                        statement.executeUpdate();
                    }
                } catch (SQLException e) {
                    _log.warning("Failed to delete timed item from DB: " + e.getMessage());
                }
            }
        } else {
            // Caso o player esteja offline, remover o item diretamente da tabela de itens
            try (Connection con = ConnectionPool.getConnection()) {
                if (temp._charId != 0) {
                    try (PreparedStatement statement = con.prepareStatement("DELETE FROM items WHERE owner_id = ? AND object_id = ?")) {
                        statement.setInt(1, temp._charId);
                        statement.setInt(2, temp._itemId);
                        statement.executeUpdate();
                    }
                } else {
                    for (L2Object o : L2World.getInstance().getObjects()) {
                        if (o.getObjectId() == temp._itemId) {
                            L2World.getInstance().removeVisibleObject(o, o.getRegion());
                            break;
                        }
                    }
                }
            } catch (SQLException e) {
                _log.warning("Failed to remove item from world DB: " + e.getMessage());
            }
        }
    }

    private final ExclusiveTask _startControlTask = new ExclusiveTask() {
        @Override
        protected void onElapsed() {
            long currentTime = System.currentTimeMillis() / 1000;

            // Criar uma cópia dos valores para evitar ConcurrentModificationException
            for (Info temp : new ArrayList<>(_timedItems.values())) {
                if (temp._activationTime < currentTime) {
                    delete(temp);
                }
            }

            schedule(60000); // Run every 60 seconds
        }
    };
 // In TimedItemManager.java
    public long getRemainingTime(int itemObjectId) {
        Info itemInfo = _timedItems.get(itemObjectId);

        if (itemInfo != null) {
            long currentTime = System.currentTimeMillis() / 1000;
            long remainingTime = itemInfo._activationTime - currentTime;

            // Return remaining time if it's greater than 0
            if (remainingTime > 0) {
                return remainingTime;
            }
        }
        return 0; // Item expired or not found
    }

}
