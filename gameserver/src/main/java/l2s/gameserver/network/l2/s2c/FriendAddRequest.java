package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

/**
 * format: cS
 */
public class FriendAddRequest implements IClientOutgoingPacket
{
	private String _requestorName;

	public FriendAddRequest(String requestorName)
	{
		_requestorName = requestorName;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.FRIEND_ADD_REQUEST.writeId(packetWriter);
		packetWriter.writeC(0); // 0
		packetWriter.writeS(_requestorName);

		return true;
	}
}