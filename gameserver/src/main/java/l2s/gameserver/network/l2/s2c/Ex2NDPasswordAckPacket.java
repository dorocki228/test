package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class Ex2NDPasswordAckPacket implements IClientOutgoingPacket
{
	public static final int SUCCESS = 0x00;
	public static final int WRONG_PATTERN = 0x01;

	private int _response;

	public Ex2NDPasswordAckPacket(int response)
	{
		_response = response;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_2ND_PASSWORD_ACK.writeId(packetWriter);
		packetWriter.writeC(0x00);
		packetWriter.writeD(_response == WRONG_PATTERN ? 0x01 : 0x00);
		packetWriter.writeD(0x00);

		return true;
	}
}