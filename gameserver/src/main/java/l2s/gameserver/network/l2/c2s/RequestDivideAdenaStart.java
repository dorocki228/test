package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExDivideAdenaStart;

public class RequestDivideAdenaStart extends L2GameClientPacket
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
		if(activeChar.getParty() == null)
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_PROCEED_AS_YOU_ARE_NOT_IN_AN_ALLIANCE_OR_PARTY);
			return;
		}
		if(activeChar.getParty().getPartyLeader() != activeChar)
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_PROCEED_AS_YOU_ARE_NOT_A_PARTY_LEADER);
			return;
		}

		if(activeChar.getParty().getCommandChannel() != null && activeChar.getParty().getCommandChannel().isLeaderCommandChannel(activeChar))
			activeChar.getParty().getCommandChannel().broadCast(SystemMsg.ADENA_DISTRIBUTION_HAS_STARTED);
		else
			activeChar.getParty().broadCast(SystemMsg.ADENA_DISTRIBUTION_HAS_STARTED);

		activeChar.sendPacket(ExDivideAdenaStart.STATIC);
	}
}
