package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExOneDayRewardInfo;
import l2s.gameserver.network.l2.s2c.ExOneDayRewardList;
import l2s.gameserver.network.l2.s2c.ExTodoListInzone;

/**
 * @author Bonux
**/
public class RequestTodoList implements IClientIncomingPacket
{
	private int _tab;
	@SuppressWarnings("unused")
	private boolean _showAllLevels;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_tab = packet.readC(); // Daily Reward = 9, Event = 1, Instance Zone = 2
		_showAllLevels = packet.readC() > 0; // Disabled = 0, Enabled = 1
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		switch(_tab)
		{
			case 1:
			case 2:
			{
				client.sendPacket(new ExTodoListInzone());
				break;
			}
			case 9:
			{
				Player activeChar = client.getActiveChar();
				if(activeChar == null)
					client.sendPacket(new ExOneDayRewardList());
				else
				{
					client.sendPacket(new ExOneDayRewardInfo(activeChar));
					client.sendPacket(new ExOneDayRewardList(activeChar));
				}
				break;
			}
		}
	}
}