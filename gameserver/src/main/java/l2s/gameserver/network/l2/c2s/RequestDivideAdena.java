package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExDivideAdenaDone;

import java.util.List;

public class RequestDivideAdena extends L2GameClientPacket
{
	private long _count;

	@Override
	protected void readImpl()
	{
		readD();
		_count = readQ();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		long count = activeChar.getAdena();
		if(_count > count)
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_PROCEED_AS_THERE_IS_INSUFFICIENT_ADENA);
			return;
		}
		int membersCount = 0;
		long dividedCount = 0;
		List<Player> partyMembers;

		if(activeChar.getParty().getCommandChannel() != null && activeChar.getParty().getCommandChannel().isLeaderCommandChannel(activeChar))
		{
			membersCount = activeChar.getParty().getCommandChannel().getMemberCount();
			dividedCount = (long) Math.floor(_count / membersCount);
			partyMembers = activeChar.getParty().getCommandChannel().getMembers();
		}
		else
		{
			membersCount = activeChar.getParty().getMemberCount();
			dividedCount = (long) Math.floor(_count / membersCount);
			partyMembers = activeChar.getParty().getPartyMembers();
		}

		activeChar.reduceAdena(membersCount * dividedCount, false);

		ExDivideAdenaDone p = new ExDivideAdenaDone(membersCount, _count, dividedCount, activeChar.getName());

		for(Player player : partyMembers)
		{
			player.addAdena(dividedCount, false);
			player.sendPacket(p);
		}
	}
}
