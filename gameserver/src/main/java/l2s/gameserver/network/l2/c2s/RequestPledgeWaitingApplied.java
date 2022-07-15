package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.instancemanager.clansearch.ClanSearchManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.clansearch.ClanSearchPlayer;
import l2s.gameserver.network.l2.s2c.ExPledgeWaitingListApplied;

/**
 * @author GodWorld
 * @reworked by Bonux
**/
public class RequestPledgeWaitingApplied implements IClientIncomingPacket
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

		ClanSearchPlayer csPlayer = ClanSearchManager.getInstance().findAnyApplicant(activeChar.getObjectId());
		if(csPlayer != null)
			activeChar.sendPacket(new ExPledgeWaitingListApplied(csPlayer));
	}
}