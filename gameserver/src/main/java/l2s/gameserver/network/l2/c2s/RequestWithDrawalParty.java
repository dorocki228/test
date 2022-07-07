package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;

public class RequestWithDrawalParty extends L2GameClientPacket
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
		Party party = activeChar.getParty();
		if(party == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage("\u0412\u044b \u043d\u0435 \u043c\u043e\u0436\u0435\u0442\u0435 \u0441\u0435\u0439\u0447\u0430\u0441 \u0432\u044b\u0439\u0442\u0438 \u0438\u0437 \u0433\u0440\u0443\u043f\u043f\u044b.");
			return;
		}

		Reflection r = activeChar.getParty().getReflection();
		if(r != null)
		{
			if(!r.canModifyParty())
			{
				var message = activeChar.isLangRus()
							  ? "Нельзя покидать или расформировывать группу находясь внутри инстанса."
							  : "You can not leave or disband a group while inside the instance.";
				activeChar.sendMessage(message);
				return;
			}

			if(activeChar.isInCombat())
			{
				activeChar.sendMessage("\u0412\u044b \u043d\u0435 \u043c\u043e\u0436\u0435\u0442\u0435 \u0441\u0435\u0439\u0447\u0430\u0441 \u0432\u044b\u0439\u0442\u0438 \u0438\u0437 \u0433\u0440\u0443\u043f\u043f\u044b.");
				return;
			}
		}

		activeChar.leaveParty();
	}
}
