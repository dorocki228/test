package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.network.l2.components.CustomMessage;

public class RequestOustPartyMember extends L2GameClientPacket
{
	private String _name;

	@Override
	protected void readImpl()
	{
		_name = readS(16);
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		Party party = activeChar.getParty();
		if(party == null || !activeChar.getParty().isLeader(activeChar))
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInOlympiadMode())
		{
			activeChar.sendActionFailed();
			return;
		}

		Player member = party.getPlayerByName(_name);
		if(member == activeChar)
		{
			activeChar.sendActionFailed();
			return;
		}
		if(member == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		Reflection r = party.getReflection();
		if(r != null && !r.canModifyParty())
		{
			var message = activeChar.isLangRus()
						  ? "Нельзя покидать или расформировывать группу находясь внутри инстанса."
						  : "You can not leave or disband a group while inside the instance.";
			activeChar.sendMessage(message);
			return;
		}
		if(r != null)
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.RequestOustPartyMember.CantOustInDungeon"));
			return;
		}

		party.removePartyMember(member, true);
	}
}
