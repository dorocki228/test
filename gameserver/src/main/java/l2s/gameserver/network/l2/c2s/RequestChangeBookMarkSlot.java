package l2s.gameserver.network.l2.c2s;

public class RequestChangeBookMarkSlot implements IClientIncomingPacket
{
	private int slot_old, slot_new;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		slot_old = packet.readD();
		slot_new = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		//TODO not implemented
	}
}