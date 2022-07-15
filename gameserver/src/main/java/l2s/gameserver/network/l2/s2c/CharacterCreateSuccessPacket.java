package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class CharacterCreateSuccessPacket implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new CharacterCreateSuccessPacket();

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.CHARACTER_CREATE_SUCCESS.writeId(packetWriter);
		packetWriter.writeD(0x01);
		return true;
	}
}