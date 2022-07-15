package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExVariationResult implements IClientOutgoingPacket
{
	private int _stat12;
	private int _stat34;
	private int _unk3;

	public ExVariationResult(int unk1, int unk2, int unk3)
	{
		_stat12 = unk1;
		_stat34 = unk2;
		_unk3 = unk3;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_VARIATION_RESULT.writeId(packetWriter);
		packetWriter.writeD(_stat12);
		packetWriter.writeD(_stat34);
		packetWriter.writeD(_unk3);

		return true;
	}
}