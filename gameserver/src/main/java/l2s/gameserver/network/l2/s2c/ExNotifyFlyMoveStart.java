package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public final class ExNotifyFlyMoveStart implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new ExNotifyFlyMoveStart();

	public ExNotifyFlyMoveStart()
	{
		//trigger
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_NOTIFY_FLY_MOVE_START.writeId(packetWriter);

		return true;
	}
}