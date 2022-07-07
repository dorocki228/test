package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.Request;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.network.l2.s2c.TradeStartPacket;

import java.util.concurrent.CopyOnWriteArrayList;

public class AnswerTradeRequest extends L2GameClientPacket
{
	private int _response;

	@Override
	protected void readImpl()
	{
		_response = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		Request request = activeChar.getRequest();
		if(request == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		if(!request.isTypeOf(Request.L2RequestType.TRADE_REQUEST))
		{
			request.cancel();
			activeChar.sendActionFailed();
			return;
		}
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
			activeChar.sendPacket(SystemMsg.THAT_PLAYER_IS_NOT_ONLINE);
			activeChar.sendActionFailed();
			return;
		}
		if(requestor.getRequest() != request)
		{
			request.cancel();
			activeChar.sendActionFailed();
			return;
		}
		if(_response == 0)
		{
			request.cancel();
			requestor.sendPacket(new SystemMessagePacket(SystemMsg.C1_HAS_DENIED_YOUR_REQUEST_TO_TRADE).addString(activeChar.getName()));
			return;
		}
		if(!activeChar.checkInteractionDistance(requestor))
		{
			request.cancel();
			activeChar.sendPacket(SystemMsg.YOUR_TARGET_IS_OUT_OF_RANGE);
			return;
		}
		if(requestor.isActionsDisabled())
		{
			request.cancel();
			activeChar.sendPacket(new SystemMessagePacket(SystemMsg.C1_IS_ON_ANOTHER_TASK).addString(requestor.getName()));
			activeChar.sendActionFailed();
			return;
		}
		try
		{
			new Request(Request.L2RequestType.TRADE, activeChar, requestor);
			requestor.setTradeList(new CopyOnWriteArrayList<>());
			requestor.sendPacket(new SystemMessagePacket(SystemMsg.YOU_BEGIN_TRADING_WITH_C1).addString(activeChar.getName()), new TradeStartPacket(requestor, activeChar));
			activeChar.setTradeList(new CopyOnWriteArrayList<>());
			activeChar.sendPacket(new SystemMessagePacket(SystemMsg.YOU_BEGIN_TRADING_WITH_C1).addString(requestor.getName()), new TradeStartPacket(activeChar, requestor));
		}
		finally
		{
			request.done();
		}
	}
}
