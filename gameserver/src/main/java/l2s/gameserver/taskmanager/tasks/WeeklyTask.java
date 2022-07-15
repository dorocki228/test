package l2s.gameserver.taskmanager.tasks;

import com.google.common.flogger.FluentLogger;
import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;

/**
 * @author Bonux
**/
public class WeeklyTask extends AutomaticTask
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	

	private static final SchedulingPattern PATTERN = new SchedulingPattern("30 6 * * 4");

	public WeeklyTask()
	{
		super();
	}

	@Override
	public void doTask() throws Exception
	{
		_log.atInfo().log( "Weekly Global Task: launched." );
		for(Player player : GameObjectsStorage.getPlayers(true, true))
			player.restartWeeklyCounters(false);
		_log.atInfo().log( "Weekly Global Task: completed." );
	}

	@Override
	public long reCalcTime(boolean start)
	{
		return PATTERN.next(System.currentTimeMillis());
	}
}