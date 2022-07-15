package l2s.gameserver.network.l2.c2s;

/**
 * @author Bonux
**/
public final class RequestVipLuckyGameItemList implements IClientIncomingPacket
{
	private int _unk1;

	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_unk1 = packet.readC();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		System.out.println("RequestVipLuckyGameItemList _unk1=" + _unk1);
	}
}