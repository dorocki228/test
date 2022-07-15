package l2s.gameserver.model.entity.olympiad;

import com.google.common.flogger.FluentLogger;
import l2s.gameserver.Config;

public class WeeklyTask implements Runnable
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	

	@Override
	public void run()
	{
		Olympiad.doWeekTasks();
		_log.atInfo().log( "Olympiad System: Added weekly points to nobles." );
		Olympiad.setWeekStartTime(System.currentTimeMillis());
	}
}