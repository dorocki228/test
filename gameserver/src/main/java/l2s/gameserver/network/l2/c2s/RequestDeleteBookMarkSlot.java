package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExGetBookMarkInfoPacket;

public class RequestDeleteBookMarkSlot extends L2GameClientPacket
{
	private int slot;

	@Override
	protected void readImpl()
	{
		slot = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar != null)
		{
			activeChar.getBookMarkList().remove(slot);
			activeChar.sendPacket(new ExGetBookMarkInfoPacket(activeChar));
		}
	}
}
