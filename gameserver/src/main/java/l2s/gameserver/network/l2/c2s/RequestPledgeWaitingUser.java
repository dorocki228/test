package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.instancemanager.clansearch.ClanSearchManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.clansearch.ClanSearchPlayer;
import l2s.gameserver.network.l2.s2c.ExPledgeWaitingList;
import l2s.gameserver.network.l2.s2c.ExPledgeWaitingUser;

/**
 * @author GodWorld
 * @reworked by Bonux
**/
public class RequestPledgeWaitingUser implements IClientIncomingPacket
{
	private int _clanId;
	private int _charId;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_clanId = packet.readD();
		_charId = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		ClanSearchPlayer csPlayer = ClanSearchManager.getInstance().getApplicant(_clanId, _charId);
		if(csPlayer == null)
			activeChar.sendPacket(new ExPledgeWaitingList(_clanId));
		else
			activeChar.sendPacket(new ExPledgeWaitingUser(_charId, csPlayer.getDesc()));
	}
}