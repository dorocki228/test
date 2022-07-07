package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.TradeItem;

import java.util.List;

public class PrivateStoreList extends L2GameServerPacket
{
	private final int _sellerId;
	private final long _adena;
	private final boolean _package;
	private final List<TradeItem> _sellList;

	public PrivateStoreList(Player buyer, Player seller)
	{
		_sellerId = seller.getObjectId();
		_adena = buyer.getAdena();
		_package = seller.getPrivateStoreType() == 8;
		_sellList = seller.getSellList();
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_sellerId);
        writeD(_package ? 1 : 0);
		writeQ(_adena);
        writeD(0);
        writeD(_sellList.size());
		for(TradeItem si : _sellList)
		{
            writeItemInfo(si);
			writeQ(si.getOwnersPrice());
			writeQ(si.getStorePrice());
		}
	}
}
