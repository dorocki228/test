package handler.dailymissions;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.DailyMission;
import l2s.gameserver.templates.dailymissions.DailyMissionStatus;
import l2s.gameserver.templates.dailymissions.DailyMissionTemplate;

/**
 * @author Bonux
 **/
public class ClanDailyMissionHandler extends BasicDailyMissionHandler
{
	@Override
	public DailyMissionStatus getStatus(Player player, DailyMission mission, DailyMissionTemplate missionTemplate)
	{
		if(mission != null && mission.isCompleted())
			return DailyMissionStatus.COMPLETED;

		if(player.getLevel() < 70) {
			return DailyMissionStatus.NOT_AVAILABLE;
		}

		if (player.isInClan())
			return DailyMissionStatus.AVAILABLE;

		return DailyMissionStatus.NOT_AVAILABLE;
	}

}
