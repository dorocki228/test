package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExPledgeBonusUIOpen;
import l2s.gameserver.network.l2.s2c.ExPledgeClassicRaidInfo;

/**
 * @author Bonux
**/
public class RequestPledgeBonusOpen implements IClientIncomingPacket
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

		if(activeChar.getClan() == null)
			return;

		activeChar.sendPacket(new ExPledgeBonusUIOpen(activeChar));
		activeChar.sendPacket(new ExPledgeClassicRaidInfo(activeChar));
	}
}