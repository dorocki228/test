package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.templates.dailymissions.DailyMissionTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class ExOneDayReceiveRewardList extends L2GameServerPacket
{
	private final Player _player;
	private final int _dayOfWeek;
	private final List<DailyMissionTemplate> _availableMissions;

	public ExOneDayReceiveRewardList(Player player)
	{
		_player = player;
		_dayOfWeek = Calendar.getInstance().get(7);
		_availableMissions = new ArrayList<>(player.getDailyMissionList().getAvailableMissions());
		Collections.sort(_availableMissions);
	}

	@Override
	protected void writeImpl()
	{
		writeC(23);
		writeD(_player.getBaseClassId());
		writeD(_dayOfWeek);
		writeD(_availableMissions.size());
		for(DailyMissionTemplate mission : _availableMissions)
		{
			writeH(mission.getId());
			writeC(_player.getDailyMissionList().getStatus(mission).ordinal());
			if(mission.getHandler().haveProgress(mission))
			{
				writeC(1);
				writeD(_player.getDailyMissionList().getProgress(mission));
				writeD(mission.getRepetitionCount());
			}
			else
			{
				writeC(0);
				writeD(0);
				writeD(0);
			}
		}
	}
}
