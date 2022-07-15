package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
 */
public class ExFriendNotifyNameChange implements IClientOutgoingPacket
{
	public ExFriendNotifyNameChange()
	{
		//
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_FRIEND_NOTIFY_NAME_CHANGED.writeId(packetWriter);
		//TODO: [Bonux]

		return true;
	}
}
