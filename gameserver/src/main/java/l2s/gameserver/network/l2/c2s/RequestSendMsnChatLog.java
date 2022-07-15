package l2s.gameserver.network.l2.c2s;

@SuppressWarnings("unused")
public class RequestSendMsnChatLog implements IClientIncomingPacket
{
	private int unk3;
	private String unk, unk2;

	/**
	 * format: SSd
	 */
	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		unk = packet.readS();
		unk2 = packet.readS();
		unk3 = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		//_log.info.println(getType() + " :: " + unk + " :: " + unk2 + " :: " + unk3);
	}
}