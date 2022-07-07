package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;

public final class RequestSkillList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		Player cha = getClient().getActiveChar();
		if(cha != null)
			cha.sendSkillList();
	}
}
