package l2s.gameserver.taskmanager.tasks;

import com.google.common.flogger.FluentLogger;
import l2s.gameserver.instancemanager.TrainingCampManager;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.utils.TimeUtils;

/**
 * @author Bonux
**/
public class DailyTask extends AutomaticTask
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	

	public DailyTask()
	{
		super();
	}

	@Override
	public void doTask() throws Exception
	{
		_log.atInfo().log( "Daily Global Task: launched." );
		for(Player player : GameObjectsStorage.getPlayers(true, true))
			player.restartDailyCounters(false);
		ClanTable.getInstance().refreshClanAttendanceInfo();
		TrainingCampManager.getInstance().refreshTrainingCamp();
		_log.atInfo().log( "Daily Global Task: completed." );
	}

	@Override
	public long reCalcTime(boolean start)
	{
		return TimeUtils.DAILY_DATE_PATTERN.next(System.currentTimeMillis());
	}
}