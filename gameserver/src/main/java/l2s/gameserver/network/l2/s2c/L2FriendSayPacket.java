package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

/**
 * Send Private (Friend) Message
 *
 * Format: c dSSS
 *
 * d: Unknown
 * S: Sending Player
 * S: Receiving Player
 * S: Message
 */
public class L2FriendSayPacket implements IClientOutgoingPacket
{
	private String _sender, _receiver, _message;

	public L2FriendSayPacket(String sender, String reciever, String message)
	{
		_sender = sender;
		_receiver = reciever;
		_message = message;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.L2_FRIEND_SAY.writeId(packetWriter);
		packetWriter.writeD(0);
		packetWriter.writeS(_receiver);
		packetWriter.writeS(_sender);
		packetWriter.writeS(_message);

		return true;
	}
}