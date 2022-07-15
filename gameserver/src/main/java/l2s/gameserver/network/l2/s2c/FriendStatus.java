package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.actor.instances.player.Friend;
import l2s.gameserver.network.l2.OutgoingPackets;

/**
 * @author Bonux
**/
public class FriendStatus implements IClientOutgoingPacket
{
	private final Friend _friend;
	private final boolean _login;

	public FriendStatus(Friend friend, boolean login)
	{
		_friend = friend;
		_login = login;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.FRIEND_STATUS.writeId(packetWriter);
		packetWriter.writeD(_login);
		packetWriter.writeS(_friend.getName());
		if(!_login)
			packetWriter.writeD(_friend.getObjectId());

		return true;
	}
}
