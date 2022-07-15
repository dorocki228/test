package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class FriendAdd implements IClientOutgoingPacket
{
	public FriendAdd()
	{}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.FRIEND_ADD.writeId(packetWriter);

		return true;
	}
}