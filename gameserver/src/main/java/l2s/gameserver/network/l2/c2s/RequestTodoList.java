package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExConnectedTimeAndGettableReward;
import l2s.gameserver.network.l2.s2c.ExOneDayReceiveRewardList;

public class RequestTodoList extends L2GameClientPacket
{
	private int _tab;
	private boolean _showAllLevels;

	@Override
	protected void readImpl()
	{
		_tab = readC();
		_showAllLevels = readC() > 0;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		switch(_tab)
		{
			case 9:
			{
				activeChar.sendPacket(new ExOneDayReceiveRewardList(activeChar));
				activeChar.sendPacket(new ExConnectedTimeAndGettableReward(activeChar));
				break;
			}
		}
	}
}
