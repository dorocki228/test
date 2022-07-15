package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

/**
 * @author Bonux
**/
public class FriendRemove implements IClientOutgoingPacket
{
	private final String _friendName;

	public FriendRemove(String name)
	{
		_friendName = name;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.FRIEND_REMOVE.writeId(packetWriter);
		packetWriter.writeD(1); //UNK
		packetWriter.writeS(_friendName); //FriendName

		return true;
	}
}
