package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.tradelist.TradeItem;

public class PrivateStoreListBuy extends L2GameServerPacket
{
	private final Player _activeChar;
	private final Player _storePlayer;
	private final int _playerAdena;
	private final List<TradeItem> _items;
	
	public PrivateStoreListBuy(Player player, Player storePlayer)
	{
		_activeChar = player;
		_storePlayer = storePlayer;
		_storePlayer.getSellList().updateItems();
		
	//	_playerAdena = player.getAdena();
		if (Config.ENABLE_WARNING)
		{
			final String name = ItemTable.getInstance().getTemplate(Config.STORE_BUY_CURRENCY).getName();
			final CreatureSay cs11 = new CreatureSay(0, 15, "ATTENTION", "Private buy is based on "+name+"."); // 8D
			_activeChar.sendPacket(cs11);
		}
		_playerAdena = player.getBuyStoreCurrency();
		_items = _storePlayer.getBuyList().getAvailableItems(player.getInventory());
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xb8);
		writeD(_storePlayer.getObjectId());
		writeD(_playerAdena);
		writeD(_items.size());
		
		for (TradeItem item : _items)
		{
			writeD(item.getObjectId());
			writeD(item.getItem().getItemId());
			writeH(item.getEnchant());
			writeD(item.getCount()); // give max possible sell amount
			
			writeD(item.getItem().getReferencePrice());
			writeH(0);
			
			writeD(item.getItem().getBodyPart());
			writeH(item.getItem().getType2());
			writeD(item.getPrice());// buyers price
			
			writeD(item.getCount()); // maximum possible tradecount
		}
	}
}