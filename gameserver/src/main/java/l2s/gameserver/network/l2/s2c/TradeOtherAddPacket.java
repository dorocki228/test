package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.network.l2.OutgoingPackets;

public class TradeOtherAddPacket extends AbstractItemPacket
{
	private final int _type;
	private final ItemInfo _item;
	private final long _amount;

	public TradeOtherAddPacket(int type, ItemInfo item, long amount)
	{
		_type = type;
		_item = item;
		_amount = amount;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.TRADE_OTHER_ADD.writeId(packetWriter);
		packetWriter.writeC(_type);
		packetWriter.writeD(1);	// Count
		if(_type == 2)
		{
			packetWriter.writeH(1);	// Count
			packetWriter.writeC(0x00); // UNK 140
			packetWriter.writeC(0x00); // UNK 140
			writeItem(packetWriter, _item, _amount);
		}

		return true;
	}
}