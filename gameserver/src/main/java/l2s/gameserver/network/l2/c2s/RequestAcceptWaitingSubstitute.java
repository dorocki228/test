package l2s.gameserver.network.l2.c2s;

/**
 * Created by IntelliJ IDEA. User: Cain Date: 23.05.12 Time: 23:09 ответ от чара
 * выбранного на замену
 */
public class RequestAcceptWaitingSubstitute implements IClientIncomingPacket
{
	private int _flag;
	private int _unk1;
	private int _unk2;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_flag = packet.readD();
		_unk1 = packet.readD();
		_unk2 = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		//
	}
}
