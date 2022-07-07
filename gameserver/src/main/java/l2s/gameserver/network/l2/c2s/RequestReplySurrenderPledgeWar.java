package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.Request;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.ClanWar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RequestReplySurrenderPledgeWar extends L2GameClientPacket
{
	private static final Logger _log;
	private String _reqName;
	private int _answer;

	@Override
	protected void readImpl()
	{
		_reqName = readS();
		_answer = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		Request request = activeChar.getRequest();
		if(request == null || !request.isTypeOf(Request.L2RequestType.CLAN_WAR_SURRENDER))
			return;
		if(!request.isInProgress())
		{
			request.cancel();
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isOutOfControl())
		{
			request.cancel();
			activeChar.sendActionFailed();
			return;
		}
		Player requestor = request.getRequestor();
		if(requestor == null)
		{
			request.cancel();
			activeChar.sendActionFailed();
			return;
		}
		if(requestor.getRequest() != request)
		{
			request.cancel();
			activeChar.sendActionFailed();
			return;
		}
		if(_answer == 1)
			try
			{
				Clan clan = activeChar.getClan();
				ClanWar war = requestor.getClan().getClanWar(clan);
				if(war != null)
					war.setPeriod(clan, ClanWar.ClanWarPeriod.PEACE);
			}
			finally
			{
				request.done();
			}
		else
		{
			_log.warn(getClass().getSimpleName() + ": Missing implementation for answer: " + _answer + " and name: " + _reqName + "!");
			request.cancel();
		}
	}

	static
	{
		_log = LoggerFactory.getLogger(RequestReplySurrenderPledgeWar.class);
	}
}
