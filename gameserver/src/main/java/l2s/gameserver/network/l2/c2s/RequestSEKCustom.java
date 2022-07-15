package l2s.gameserver.network.l2.c2s;

public class RequestSEKCustom implements IClientIncomingPacket
{
	private int SlotNum, Direction;

	/**
	 * format: dd
	 */
	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		SlotNum = packet.readD();
		Direction = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		//TODO not implemented
	}
}