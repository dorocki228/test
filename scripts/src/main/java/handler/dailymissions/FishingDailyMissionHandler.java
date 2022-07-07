package handler.dailymissions;

import l2s.gameserver.listener.CharListener;
import l2s.gameserver.listener.actor.player.OnFishingListener;
import l2s.gameserver.model.Player;

import java.util.OptionalInt;

/**
 * @author Bonux
 **/
public class FishingDailyMissionHandler extends ProgressDailyMissionHandler
{
	private class HandlerListeners implements OnFishingListener
	{
		@Override
		public void onFishing(Player player, OptionalInt fish)
		{
			fish.ifPresent(id -> {
				progressMission(player, 1, dailyMissionTemplate -> id == Integer.parseInt(dailyMissionTemplate.getValue()));
			});
		}
	}

	private final HandlerListeners _handlerListeners = new HandlerListeners();

	@Override
	public CharListener getListener()
	{
		return _handlerListeners;
	}
}
