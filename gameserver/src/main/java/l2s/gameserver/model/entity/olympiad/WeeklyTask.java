package l2s.gameserver.model.entity.olympiad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeeklyTask implements Runnable
{
	private static final Logger _log = LoggerFactory.getLogger(WeeklyTask.class);

	@Override
	public void run()
	{
		Olympiad.doWeekTasks();

		_log.info("Olympiad System: Added weekly points to nobles.");

		Olympiad.setWeekStartTime(System.currentTimeMillis());
	}
}
