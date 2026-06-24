package net.sf.l2j.community.marketplace;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ConnectionPool;
 
public class AuctionTableCommunity
{
    private static Logger log = Logger.getLogger(AuctionTableCommunity.class.getName());
   
    private Map<Integer, AuctionHolder> items;
    private int maxId;
   
    public static AuctionTableCommunity getInstance()
    {
        return SingletonHolder._instance;
    }
   
    protected AuctionTableCommunity()
    {
        items = new ConcurrentHashMap<>();
        maxId = 0;
       
        load();
    }
   
    private void load()
    {
        try (Connection con = ConnectionPool.getConnection())
        {
            PreparedStatement stm = con.prepareStatement("SELECT * FROM auction_table");
            ResultSet rset = stm.executeQuery();
           
            while (rset.next())
            {
                int auctionId = rset.getInt("auctionid");
                int ownerId = rset.getInt("ownerid");
                int itemId = rset.getInt("itemid");
                int count = rset.getInt("count");
                int enchant = rset.getInt("enchant");
                int costId = rset.getInt("costid");
                int costCount = rset.getInt("costcount");
               
                items.put(auctionId, new AuctionHolder(auctionId, ownerId, itemId, count, enchant, costId, costCount));
               
                if (auctionId > maxId)
                    maxId = auctionId;
            }
           
            rset.close();
            stm.close();
            con.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
       
        log.info("AuctionTable: Loaded "+items.size()+" items.");
    }
   
    public void addItem(AuctionHolder item)
    {
        items.put(item.getAuctionId(), item);
       
        try (Connection con = ConnectionPool.getConnection())
        {
            PreparedStatement stm = con.prepareStatement("INSERT INTO auction_table VALUES (?,?,?,?,?,?,?)");
            stm.setInt(1, item.getAuctionId());
            stm.setInt(2, item.getOwnerId());
            stm.setInt(3, item.getItemId());
            stm.setInt(4, item.getCount());
            stm.setInt(5, item.getEnchant());
            stm.setInt(6, item.getCostId());
            stm.setInt(7, item.getCostCount());
           
            stm.execute();
            stm.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
   
    public void deleteItem(AuctionHolder item)
    {
        items.remove(item.getAuctionId());
       
        try (Connection con = ConnectionPool.getConnection())
        {
            PreparedStatement stm = con.prepareStatement("DELETE FROM auction_table WHERE auctionid=?");
            stm.setInt(1, item.getAuctionId());
           
            stm.execute();
            stm.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
   
    public AuctionHolder getItem(int auctionId)
    {
        return items.get(auctionId);
    }
   
    public Map<Integer, AuctionHolder> getItems()
    {
        return items;
    }
   
    public int getNextAuctionId()
    {
        maxId++;
        return maxId;
    }
   
    private static class SingletonHolder
    {
        protected static final AuctionTableCommunity _instance = new AuctionTableCommunity();
    }
}