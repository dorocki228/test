package l2s.gameserver.handler.dailymissions;

import l2s.gameserver.listener.CharListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.DailyMission;
import l2s.gameserver.templates.dailymissions.DailyMissionStatus;
import l2s.gameserver.templates.dailymissions.DailyMissionTemplate;

import java.time.ZonedDateTime;

public interface IDailyMissionHandler
{
	CharListener getListener();

	void onRestoreMission(Player p0, DailyMission p1);

	DailyMissionStatus getStatus(Player p0, DailyMission p1, DailyMissionTemplate p2);

	boolean haveProgress(DailyMissionTemplate p0);

	void onComplete(DailyMission mission, Player player);

	void reset(DailyMission mission, ZonedDateTime nextExecution, ZonedDateTime lastExecution);
}
