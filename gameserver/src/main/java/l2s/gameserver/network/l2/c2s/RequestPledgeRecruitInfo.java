package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.ExPledgeRecruitInfo;
import l2s.gameserver.tables.ClanTable;

/**
 * @author GodWorld
 * @reworked by Bonux
**/
public class RequestPledgeRecruitInfo implements IClientIncomingPacket
{
	private int _clanId;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_clanId = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		Clan clan = ClanTable.getInstance().getClan(_clanId);
		if(clan == null)
			return;

		activeChar.sendPacket(new ExPledgeRecruitInfo(clan));
	}
}