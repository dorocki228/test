package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExOlympiadMatchList;
import l2s.gameserver.utils.NpcUtils;

public class RequestOlympiadMatchList extends L2GameClientPacket
{
	@Override
	protected void readImpl() throws Exception
	{}

	@Override
	protected void runImpl() throws Exception
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(NpcUtils.canPassPacket(player, this) != null)
			return;

		player.sendPacket(new ExOlympiadMatchList(player));
	}
}
