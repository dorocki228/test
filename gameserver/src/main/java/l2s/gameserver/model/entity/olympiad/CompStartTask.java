package l2s.gameserver.model.entity.olympiad;

import com.google.common.flogger.FluentLogger;
import l2s.gameserver.Announcements;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.network.l2.components.SystemMsg;

class CompStartTask implements Runnable
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	

	@Override
	public void run()
	{
		if(Olympiad.isOlympiadEnd())
			return;

		Olympiad._manager = new OlympiadManager();
		Olympiad._inCompPeriod = true;

		new Thread(Olympiad._manager).start();

		Olympiad.startCompEndTask(Olympiad.getMillisToCompEnd());

		Announcements.announceToAll(SystemMsg.SHARPEN_YOUR_SWORDS_TIGHTEN_THE_STITCHING_IN_YOUR_ARMOR_AND_MAKE_HASTE_TO_A_GRAND_OLYMPIAD_MANAGER__BATTLES_IN_THE_GRAND_OLYMPIAD_GAMES_ARE_NOW_TAKING_PLACE);
		_log.atInfo().log( "Olympiad System: Olympiad Game Started" );
	}
}