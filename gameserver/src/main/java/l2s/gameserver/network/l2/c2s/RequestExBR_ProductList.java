package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExBR_ProductListPacket;

public class RequestExBR_ProductList extends L2GameClientPacket
{
	private int _unk;

	@Override
	protected void readImpl()
	{
		_unk = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(_unk == 1)
		{
			activeChar.sendPacket(new ExBR_ProductListPacket(activeChar, false));
			activeChar.sendPacket(new ExBR_ProductListPacket(activeChar, true));
		}
	}
}
