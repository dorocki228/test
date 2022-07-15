package l2s.gameserver.network.l2.s2c;

import java.util.Collection;
import java.util.List;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.TradeItem;
import l2s.gameserver.network.l2.OutgoingPackets;

public class PrivateStoreList extends AbstractItemPacket
{
	private int _sellerId;
	private long _adena;
	private final boolean _package;
	private Collection<TradeItem> _sellList;

	/**
	 * Список вещей в личном магазине продажи, показываемый покупателю
	 * @param buyer
	 * @param seller
	 */
	public PrivateStoreList(Player buyer, Player seller)
	{
		_sellerId = seller.getObjectId();
		_adena = buyer.getAdena();
		_package = seller.getPrivateStoreType() == Player.STORE_PRIVATE_SELL_PACKAGE;
		_sellList = seller.getSellList().values();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.PRIVATE_STORE_LIST.writeId(packetWriter);
		packetWriter.writeD(_sellerId);
		packetWriter.writeD(_package ? 1 : 0);
		packetWriter.writeQ(_adena);
		packetWriter.writeD(0x00); //TODO: [Bonux] Количество свободных ячеек в инвентаре.
		packetWriter.writeD(_sellList.size());
		for(TradeItem si : _sellList)
		{
			writeItem(packetWriter, si);
			packetWriter.writeQ(si.getOwnersPrice());
			packetWriter.writeQ(si.getStorePrice());
		}

		return true;
	}
}