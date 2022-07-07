package l2s.gameserver.handler.dailymissions.impl;

import l2s.gameserver.handler.dailymissions.IDailyMissionHandler;
import l2s.gameserver.listener.CharListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.DailyMission;
import l2s.gameserver.templates.dailymissions.DailyMissionStatus;
import l2s.gameserver.templates.dailymissions.DailyMissionTemplate;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultDailyMissionHandler implements IDailyMissionHandler
{
	private Map<Integer, ZonedDateTime> lastResetTime = new ConcurrentHashMap<>();

	@Override
	public CharListener getListener()
	{
		return null;
	}

	@Override
	public void onRestoreMission(Player player, DailyMission mission)
	{}

	@Override
	public DailyMissionStatus getStatus(Player player, DailyMission mission, DailyMissionTemplate missionTemplate)
	{
		return DailyMissionStatus.NOT_AVAILABLE;
	}

	@Override
	public boolean haveProgress(DailyMissionTemplate missionTemplate)
	{
		return false;
	}

	@Override
	public void onComplete(DailyMission mission, Player player)
	{
	}

	public void reset(DailyMission mission, ZonedDateTime nextExecution, ZonedDateTime lastExecution)
	{
		var time = lastResetTime.get(mission.getId());
		if(time == null)
		{
			lastResetTime.put(mission.getId(), lastExecution);
			return;
		}

		if(!time.isAfter(nextExecution))
			return;

		lastResetTime.put(mission.getId(), nextExecution);

		onReset(mission);
	}

	protected void onReset(DailyMission mission)
	{
	}
}
