package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;

public class RequestItemList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(!activeChar.getPlayerAccess().UseInventory || activeChar.isBlocked())
		{
			activeChar.sendActionFailed();
			return;
		}
		activeChar.sendItemList(true);
		activeChar.sendStatusUpdate(false, false, 14);
	}
}
