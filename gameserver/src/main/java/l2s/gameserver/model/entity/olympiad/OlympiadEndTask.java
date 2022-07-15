package l2s.gameserver.model.entity.olympiad;

import com.google.common.flogger.FluentLogger;
import l2s.gameserver.Announcements;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.instancemanager.OlympiadHistoryManager;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class OlympiadEndTask implements Runnable
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	

	@Override
	public void run()
	{
		if(Olympiad._inCompPeriod) // Если бои еще не закончились, откладываем окончание олимпиады на минуту
		{
			Olympiad.startOlympiadEndTask(60000);
			return;
		}

		Announcements.announceToAll(new SystemMessage(SystemMessage.OLYMPIAD_PERIOD_S1_HAS_ENDED).addNumber(Olympiad._currentCycle));
		//Announcements.announceToAll("Olympiad Validation Period has began");

		Olympiad._isOlympiadEnd = true;
		if(Olympiad._scheduledManagerTask != null)
			Olympiad._scheduledManagerTask.cancel(false);
		if(Olympiad._scheduledWeeklyTask != null)
			Olympiad._scheduledWeeklyTask.cancel(false);

		Olympiad.setValidationStartTime(Olympiad.getOlympiadPeriodEndTime());
		Olympiad._period = 1;

		Hero.getInstance().clearHeroes();
		OlympiadHistoryManager.getInstance().switchData();

		for(Player player : GameObjectsStorage.getPlayers(false, true))
			player.checkAndDeleteOlympiadItems();

		Hero.getInstance().computeNewHeroes(OlympiadDatabase.computeHeroesToBe());

		OlympiadDatabase.cleanupParticipants();
		OlympiadDatabase.loadParticipantsRank();

		try
		{
			OlympiadDatabase.save();
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "Olympiad System: Failed to save Olympiad configuration!" );
		}

		_log.atInfo().log( "Olympiad System: Starting Validation period. Time to end validation: %s minutes", Olympiad.getMillisToValidationEnd() / (60 * 1000) );

		if(Olympiad._scheduledValdationTask != null)
			Olympiad._scheduledValdationTask.cancel(false);
		Olympiad._scheduledValdationTask = ThreadPoolManager.getInstance().schedule(new ValidationTask(), Olympiad.getMillisToValidationEnd());
	}
}