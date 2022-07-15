package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExPutCommissionResultForVariationMake implements IClientOutgoingPacket
{
	private int _gemstoneObjId, _unk1, _unk3;
	private long _gemstoneCount, _unk2;

	public ExPutCommissionResultForVariationMake(int gemstoneObjId, long count)
	{
		_gemstoneObjId = gemstoneObjId;
		_unk1 = 1;
		_gemstoneCount = count;
		_unk2 = 1;
		_unk3 = 1;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PUT_COMMISSION_RESULT_FOR_VARIATION_MAKE.writeId(packetWriter);
		packetWriter.writeD(_gemstoneObjId);
		packetWriter.writeD(_unk1);
		packetWriter.writeQ(_gemstoneCount);
		packetWriter.writeQ(_unk2);
		packetWriter.writeD(_unk3);

		return true;
	}
}