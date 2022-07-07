package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Request;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.network.l2.s2c.TradeRequestPacket;
import l2s.gameserver.utils.Util;

public class TradeRequest extends L2GameClientPacket
{
	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(!activeChar.getPlayerAccess().UseTrade)
		{
			activeChar.sendPacket(SystemMsg.SOME_LINEAGE_II_FEATURES_HAVE_BEEN_LIMITED_FOR_FREE_TRIALS_);
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isInStoreMode() || activeChar.isPrivateBuffer())
		{
			activeChar.sendPacket(SystemMsg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}
		if(activeChar.isFishing())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}
		if(activeChar.isInTrade())
		{
			activeChar.sendPacket(SystemMsg.YOU_ARE_ALREADY_TRADING_WITH_SOMEONE);
			return;
		}
		if(activeChar.isProcessingRequest())
		{
			activeChar.sendPacket(SystemMsg.WAITING_FOR_ANOTHER_REPLY);
			return;
		}
		String tradeBan = activeChar.getVar("tradeBan");
		if(tradeBan != null && ("-1".equals(tradeBan) || Long.parseLong(tradeBan) >= System.currentTimeMillis()))
		{
			if("-1".equals(tradeBan))
				activeChar.sendMessage(new CustomMessage("common.TradeBannedPermanently"));
			else
				activeChar.sendMessage(new CustomMessage("common.TradeBanned").addString(Util.formatTime((int) (Long.parseLong(tradeBan) / 1000L - System.currentTimeMillis() / 1000L))));
			return;
		}
		GameObject target = activeChar.getVisibleObject(_objectId);
		if(target == null || !target.isPlayer() || target == activeChar)
		{
			activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return;
		}

		if(!activeChar.checkInteractionDistance(target))
		{
			activeChar.sendPacket(SystemMsg.YOUR_TARGET_IS_OUT_OF_RANGE);
			return;
		}

		Player receiver = target.getPlayer();

		if(activeChar.getFraction().canAttack(target.getFraction())
				&& (!activeChar.isInZone(ZoneType.peace_zone) || !receiver.isInZone(ZoneType.peace_zone)))
		{
			activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return;
		}

		if(!receiver.getPlayerAccess().UseTrade)
		{
			activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return;
		}

		tradeBan = receiver.getVar("tradeBan");
		if(tradeBan != null && ("-1".equals(tradeBan) || Long.parseLong(tradeBan) >= System.currentTimeMillis()))
		{
			activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return;
		}

		if(receiver.getBlockList().contains(activeChar))
		{
			activeChar.sendPacket(SystemMsg.YOU_HAVE_BEEN_BLOCKED_FROM_CHATTING_WITH_THAT_CONTACT);
			return;
		}

		if(receiver.getTradeRefusal() || receiver.isBusy())
		{
			activeChar.sendPacket(new SystemMessagePacket(SystemMsg.C1_IS_ON_ANOTHER_TASK).addString(receiver.getName()));
			return;
		}

		new Request(Request.L2RequestType.TRADE_REQUEST, activeChar, receiver).setTimeout(10000L);

		receiver.sendPacket(new TradeRequestPacket(activeChar.getObjectId()));
		activeChar.sendPacket(new SystemMessagePacket(SystemMsg.YOU_HAVE_REQUESTED_A_TRADE_WITH_C1).addString(receiver.getName()));
	}
}
