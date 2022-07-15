package l2s.gameserver.network.l2.c2s;

import com.google.common.flogger.FluentLogger;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Request;
import l2s.gameserver.model.Request.L2RequestType;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.ClanWar;
import l2s.gameserver.model.pledge.ClanWar.ClanWarPeriod;

/**
 * @author GodWorld & reworked by Bonux
**/
public final class RequestReplySurrenderPledgeWar implements IClientIncomingPacket
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	

	private String _reqName;
	private int _answer;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_reqName = packet.readS();
		_answer = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		Request request = activeChar.getRequest();
		if(request == null || !request.isTypeOf(L2RequestType.CLAN_WAR_SURRENDER))
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

		Clan clan = requestor.getClan();
		if(clan == null)
		{
			request.cancel();
			activeChar.sendActionFailed();
			return;
		}

		if(_answer == 1)
		{
			try
			{
				//requestor.deathPenalty(false, false, false);

				ClanWar war = clan.getWarWith(activeChar.getClanId());
				if(war != null)
					war.setPeriod(ClanWarPeriod.PEACE);
			}
			finally
			{
				request.done();
			}
		}
		else
		{
			_log.atWarning().log( "%s: Missing implementation for answer: %s and name: %s!", getClass().getSimpleName(), _answer, _reqName );
			request.cancel();
		}
	}
}