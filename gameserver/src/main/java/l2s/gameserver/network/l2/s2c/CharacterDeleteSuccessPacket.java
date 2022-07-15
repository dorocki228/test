package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class CharacterDeleteSuccessPacket implements IClientOutgoingPacket
{
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.CHARACTER_DELETE_SUCCESS.writeId(packetWriter);
		return true;
	}
}