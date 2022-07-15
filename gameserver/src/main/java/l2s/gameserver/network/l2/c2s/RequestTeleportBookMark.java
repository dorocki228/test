package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;

public class RequestTeleportBookMark implements IClientIncomingPacket
{
	private int slot;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		slot = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar != null)
			activeChar.getBookMarkList().tryTeleport(slot);
	}
}