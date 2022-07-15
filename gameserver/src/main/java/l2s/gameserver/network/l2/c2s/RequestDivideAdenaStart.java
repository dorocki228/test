package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExDivideAdenaStart;

/**
 * @author Erlandys
 */
public class RequestDivideAdenaStart implements IClientIncomingPacket
{
	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
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

		activeChar.sendPacket(SystemMsg.ADENA_DISTRIBUTION_HAS_STARTED);
		activeChar.sendPacket(ExDivideAdenaStart.STATIC);
	}
}