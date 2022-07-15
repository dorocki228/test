package l2s.gameserver.network.l2.c2s;

public class RequestTeleport implements IClientIncomingPacket
{
	private int unk, _type, unk2, unk3, unk4;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		unk = packet.readD();
		_type = packet.readD();
		if(_type == 2)
		{
			unk2 = packet.readD();
			unk3 = packet.readD();
		}
		else if(_type == 3)
		{
			unk2 = packet.readD();
			unk3 = packet.readD();
			unk4 = packet.readD();
		}
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		//TODO not implemented
	}
}