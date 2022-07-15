package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExRotation implements IClientOutgoingPacket
{
	private int _charObjId, _degree;

	public ExRotation(int charId, int degree)
	{
		_charObjId = charId;
		_degree = degree;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_ROTATION.writeId(packetWriter);
		packetWriter.writeD(_charObjId);
		packetWriter.writeD(_degree);

		return true;
	}
}
