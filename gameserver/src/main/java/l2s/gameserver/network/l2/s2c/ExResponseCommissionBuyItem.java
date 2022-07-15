package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author : Ragnarok & Bonux
 * @date : 22.04.12  12:09
 */
public class ExResponseCommissionBuyItem implements IClientOutgoingPacket
{
	public static final ExResponseCommissionBuyItem FAILED = new ExResponseCommissionBuyItem();

	private int _code;
	private int _itemId;
	private long _count;

	public ExResponseCommissionBuyItem()
	{
		_code = 0;
	}

	public ExResponseCommissionBuyItem(int itemId, long count)
	{
		_code = 1;
		_itemId = itemId;
		_count = count;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_RESPONSE_COMMISSION_BUY_ITEM.writeId(packetWriter);
		packetWriter.writeD(_code);
		if(_code == 0)
			return false;

		packetWriter.writeD(0x00); //unk, maybe item object Id
		packetWriter.writeD(_itemId);
		packetWriter.writeQ(_count);

		return true;
	}
}
