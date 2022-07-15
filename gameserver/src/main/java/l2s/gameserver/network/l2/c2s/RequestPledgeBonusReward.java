package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExPledgeBonusUIOpen;
import l2s.gameserver.utils.PledgeBonusUtils;

/**
 Obi-Wan
 12.08.2016
 */
public class RequestPledgeBonusReward implements IClientIncomingPacket
{
	private int _type;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_type = packet.readC();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		if(!Config.EX_USE_PLEDGE_BONUS)
			return;

		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		if(PledgeBonusUtils.tryReceiveReward(_type, activeChar))
			activeChar.sendPacket(new ExPledgeBonusUIOpen(activeChar));
	}
}