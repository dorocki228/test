package l2s.gameserver.taskmanager.tasks;

import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.model.GameObjectsStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DailyTask extends AutomaticTask
{
	private static final Logger _log = LoggerFactory.getLogger(DailyTask.class);
	private static final SchedulingPattern PATTERN = new SchedulingPattern("00 0 * * *");

	public DailyTask() {
	}

	@Override
	public void doTask()
	{
		_log.info("Daily Global Task: launched.");
		GameObjectsStorage.getPlayers().forEach(player ->
		{
			player.restartDailyCounters(false);
		});

		_log.info("Daily Global Task: completed.");
	}

	@Override
	public long reCalcTime(boolean start)
	{
		return PATTERN.next(System.currentTimeMillis());
	}
}
