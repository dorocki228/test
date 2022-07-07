package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.handler.bbs.BbsHandlerHolder;
import l2s.gameserver.handler.bbs.IBbsHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.network.l2.components.SystemMsg;

public class RequestShowBoard extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _unknown;

	@Override
	public void readImpl()
	{
		_unknown = readD();
	}

	@Override
	public void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(!Config.COMMUNITYBOARD_ENABLED)
		{
			activeChar.sendPacket(SystemMsg.THE_COMMUNITY_SERVER_IS_CURRENTLY_OFFLINE);
			return;
		}

		if(activeChar.getFraction() == Fraction.NONE)
		{
			activeChar.sendPacket(SystemMsg.THE_COMMUNITY_SERVER_IS_CURRENTLY_OFFLINE);
			return;
		}

		for(Event e : activeChar.getEvents())
		{
			if(!e.canUseCommunityFunctions(activeChar))
			{
				activeChar.sendPacket(SystemMsg.THE_COMMUNITY_SERVER_IS_CURRENTLY_OFFLINE);
				return;
			}
		}

		IBbsHandler handler = BbsHandlerHolder.getInstance().getCommunityHandler(Config.BBS_DEFAULT);
		if(handler != null)
			handler.onBypassCommand(activeChar, Config.BBS_DEFAULT);
	}
}
