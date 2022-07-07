package handler.dailymissions;

import l2s.gameserver.listener.CharListener;
import l2s.gameserver.listener.actor.player.OnOlympiadFinishBattleListener;
import l2s.gameserver.model.Player;

/**
 * @author Bonux
 **/
public class OlympiadBattleDailyMissionHandler extends ProgressDailyMissionHandler
{
	private class HandlerListeners implements OnOlympiadFinishBattleListener
	{
		@Override
		public void onOlympiadFinishBattle(Player player, boolean winner)
		{
			progressMission(player, 1);
		}
	}

	private final HandlerListeners _handlerListeners = new HandlerListeners();

	@Override
	public CharListener getListener()
	{
		return _handlerListeners;
	}
}
