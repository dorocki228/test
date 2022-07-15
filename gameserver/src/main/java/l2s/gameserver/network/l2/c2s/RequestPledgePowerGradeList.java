package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.RankPrivs;
import l2s.gameserver.network.l2.s2c.ExPledgePowerGradeList;

public class RequestPledgePowerGradeList implements IClientIncomingPacket
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
		Clan clan = activeChar.getClan();
		if(clan != null)
		{
			RankPrivs[] privs = clan.getAllRankPrivs();
			activeChar.sendPacket(new ExPledgePowerGradeList(privs));
		}
	}
}