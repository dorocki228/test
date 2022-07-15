package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExPutIntensiveResultForVariationMake implements IClientOutgoingPacket
{
	private int _refinerItemObjId, _lifestoneItemId, _gemstoneItemId, _unk;
	private long _gemstoneCount;

	public ExPutIntensiveResultForVariationMake(int refinerItemObjId, int lifeStoneId, int gemstoneItemId, long gemstoneCount)
	{
		_refinerItemObjId = refinerItemObjId;
		_lifestoneItemId = lifeStoneId;
		_gemstoneItemId = gemstoneItemId;
		_gemstoneCount = gemstoneCount;
		_unk = 1;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PUT_INTENSIVE_RESULT_FOR_VARIATION_MAKE.writeId(packetWriter);
		packetWriter.writeD(_refinerItemObjId);
		packetWriter.writeD(_lifestoneItemId);
		packetWriter.writeD(_gemstoneItemId);
		packetWriter.writeQ(_gemstoneCount);
		packetWriter.writeD(_unk);

		return true;
	}
}