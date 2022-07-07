package l2s.gameserver.taskmanager.tasks;

import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeeklyTask extends AutomaticTask
{
	private static final Logger _log;
	private static final SchedulingPattern PATTERN;

	public WeeklyTask() {

	}

	@Override
	public void doTask() throws Exception
	{
		_log.info("Weekly Global Task: launched.");
		for(Player player : GameObjectsStorage.getPlayers())
			player.restartWeeklyCounters(false);
		_log.info("Weekly Global Task: completed.");
	}

	@Override
	public long reCalcTime(boolean start)
	{
		return PATTERN.next(System.currentTimeMillis());
	}

	static
	{
		_log = LoggerFactory.getLogger(WeeklyTask.class);
		PATTERN = new SchedulingPattern("30 6 * * 4");
	}
}
