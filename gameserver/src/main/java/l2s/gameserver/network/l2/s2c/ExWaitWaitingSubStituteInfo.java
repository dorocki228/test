package l2s.gameserver.network.l2.s2c;

public class ExWaitWaitingSubStituteInfo implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket OPEN = new ExWaitWaitingSubStituteInfo(true);
	public static final IClientOutgoingPacket CLOSE = new ExWaitWaitingSubStituteInfo(false);

	private boolean _open;

	public ExWaitWaitingSubStituteInfo(boolean open)
	{
		_open = open;
	}

	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		// FIXME OutgoingExPackets..writeId(packet);
		packetWriter.writeD(_open);

		return true;
	}
}