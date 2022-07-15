package l2s.gameserver.network.l2.c2s;

public class RequestTimeCheck implements IClientIncomingPacket
{
	private int unk, unk2;

	/**
	 * format: dd
	 */
	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		unk = packet.readD();
		unk2 = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		//System.out.println("Unk1: " + unk + ", unk2: " + unk2);
		//TODO not implemented
	}
}