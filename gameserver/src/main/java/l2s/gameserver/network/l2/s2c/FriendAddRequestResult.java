package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class FriendAddRequestResult implements IClientOutgoingPacket
{
	public FriendAddRequestResult()
	{}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.FRIEND_ADD_REQUEST_RESULT.writeId(packetWriter);
		// TODO: when implementing, consult an up-to-date packets_game_server.xml and/or savormix
		packetWriter.writeD(0); // Accepted
		packetWriter.writeD(0); // Character ID
		packetWriter.writeS(""); // Name
		packetWriter.writeD(0); // Online
		packetWriter.writeD(0); // Friend OID
		packetWriter.writeD(0); // Level
		packetWriter.writeD(0); // Class
		packetWriter.writeH(0); // ??? 0

		return true;
	}
}
