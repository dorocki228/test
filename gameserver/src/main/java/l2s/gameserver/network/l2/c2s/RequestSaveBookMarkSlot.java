package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExUserBookMark;

/**
 * SdS
 */
public class RequestSaveBookMarkSlot implements IClientIncomingPacket
{
	private String name, acronym;
	private int icon;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		name = packet.readS(32);
		icon = packet.readD();
		acronym = packet.readS(4);
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar != null && activeChar.getBookMarkList().add(name, acronym, icon))
			activeChar.sendPacket(new ExUserBookMark(activeChar));
	}
}