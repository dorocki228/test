package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExPutItemResultForVariationMake implements IClientOutgoingPacket
{
	private int _itemObjId;
	private int _unk1;
	private int _unk2;

	public ExPutItemResultForVariationMake(int itemObjId)
	{
		_itemObjId = itemObjId;
		_unk1 = 1;
		_unk2 = 1;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PUT_ITEM_RESULT_FOR_VARIATION_MAKE.writeId(packetWriter);
		packetWriter.writeD(_itemObjId);
		packetWriter.writeD(_unk1);
		packetWriter.writeD(_unk2);

		return true;
	}
}