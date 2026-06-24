package net.sf.l2j.gameserver.instancemanager.custom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ConnectionPool;

public class AuctionTable
{
//	private static final Logger log = new Logger(AuctionTable.class.getName());
	private static final Logger log = Logger.getLogger(AuctionTable.class.getName());
	
	private ArrayList<AuctionItem> _items = new ArrayList<>();
	private int _maxId = 0;
	
	private static final String RESTORE_ITEM = "SELECT * FROM auction_table";
	private static final String ADD_ITEM = "INSERT INTO auction_table VALUES (?,?,?,?,?,?,?)";
	private static final String DELETE_ITEM = "DELETE FROM auction_table WHERE auctionid=?";
	
	public static AuctionTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected AuctionTable()
	{
		try (Connection con = ConnectionPool.getConnection();
			PreparedStatement stm = con.prepareStatement(RESTORE_ITEM))
		{	
			try (ResultSet rset = stm.executeQuery())
			{
				while (rset.next())
				{
					int auctionId = rset.getInt("auctionid");
					int ownerId = rset.getInt("ownerid");
					int itemId = rset.getInt("itemid");
					int count = rset.getInt("count");
					int enchant = rset.getInt("enchant");
					int costId = rset.getInt("costid");
					int costCount = rset.getInt("costcount");
					
					_items.add(new AuctionItem(auctionId, ownerId, itemId, count, enchant, costId, costCount));
					
					if (auctionId > _maxId)
						_maxId = auctionId;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		log.info("AuctionTable: Loaded "+ _items.size() +" items.");
	}
	
	public void addItem(AuctionItem item)
	{
		_items.add(item);
		try (Connection con = ConnectionPool.getConnection();
			PreparedStatement stm = con.prepareStatement(ADD_ITEM))
		{
			stm.setInt(1, item.getAuctionId());
			stm.setInt(2, item.getOwnerId());
			stm.setInt(3, item.getItemId());
			stm.setInt(4, item.getCount());
			stm.setInt(5, item.getEnchant());
			stm.setInt(6, item.getCostId());
			stm.setInt(7, item.getCostCount());
			stm.execute();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void deleteItem(AuctionItem item)
	{
		_items.remove(item);
		try (Connection con = ConnectionPool.getConnection();
			PreparedStatement stm = con.prepareStatement(DELETE_ITEM))
		{
			stm.setInt(1, item.getAuctionId());
			stm.execute();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public AuctionItem getItem(int auctionId)
	{
		AuctionItem ret = null;
		
		for (AuctionItem item : _items)
		{
			if (item.getAuctionId() == auctionId)
			{
				ret = item;
				break;
			}
		}
		
		return ret;
	}
	
	public ArrayList<AuctionItem> getItems()
	{
		return _items;
	}
	
	public int getNextAuctionId()
	{
		_maxId++;
		return _maxId;
	}
	
	private static class SingletonHolder
	{
		protected static final AuctionTable _instance = new AuctionTable();
	}
}