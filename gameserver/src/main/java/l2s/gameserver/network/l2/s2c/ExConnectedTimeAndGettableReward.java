
package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.templates.dailymissions.DailyMissionStatus;
import l2s.gameserver.templates.dailymissions.DailyMissionTemplate;

public class ExConnectedTimeAndGettableReward extends L2GameServerPacket
{
	private int _size;

	public ExConnectedTimeAndGettableReward(Player player)
	{
		for(DailyMissionTemplate mission : player.getDailyMissionList().getAvailableMissions())
		{
			if(player.getDailyMissionList().getStatus(mission) == DailyMissionStatus.AVAILABLE)
				_size++;
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeD(0);
		writeD(_size);
		writeD(0);
		writeD(0);
		writeD(0);
		writeD(0);
		writeD(0);
		writeD(0);
		writeD(0);
		writeD(0);
		writeD(0);
		writeD(0);
	}
}
