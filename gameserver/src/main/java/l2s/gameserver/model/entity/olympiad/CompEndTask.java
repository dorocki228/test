package l2s.gameserver.model.entity.olympiad;

import com.google.common.flogger.FluentLogger;
import l2s.gameserver.Announcements;
import l2s.gameserver.network.l2.components.SystemMsg;

class CompEndTask implements Runnable
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	

	@Override
	public void run()
	{
		if(Olympiad.isOlympiadEnd())
			return;

		OlympiadManager manager = Olympiad._manager;
		if(manager != null && !manager.getOlympiadGames().isEmpty()) // Если остались игры, ждем их завершения еще одну минуту
		{
			Olympiad.startCompEndTask(60000);
			return;
		}

		Olympiad._inCompPeriod = false;

		Announcements.announceToAll(SystemMsg.MUCH_CARNAGE_HAS_BEEN_LEFT_FOR_THE_CLEANUP_CREW_OF_THE_OLYMPIAD_STADIUM);

		_log.atInfo().log( "Olympiad System: Olympiad Game Ended" );

		try
		{
			OlympiadDatabase.save();
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "Olympiad System: Failed to save Olympiad configuration:" );
		}
		Olympiad.init();
	}
}