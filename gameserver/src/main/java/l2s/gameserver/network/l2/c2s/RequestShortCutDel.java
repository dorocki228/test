package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.ShortCutList;

public class RequestShortCutDel extends L2GameClientPacket
{
	private int _slot;
	private int _page;

	@Override
	protected void readImpl()
	{
		int id = readD();
		_slot = id % ShortCutList.MAX_SHORT_CUT_ON_PAGE_COUNT;
		_page = id / ShortCutList.MAX_SHORT_CUT_ON_PAGE_COUNT;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		activeChar.deleteShortCut(_slot, _page);
	}
}
