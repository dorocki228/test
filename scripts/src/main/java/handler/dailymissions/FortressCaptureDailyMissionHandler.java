package handler.dailymissions;

import l2s.gameserver.listener.CharListener;
import l2s.gameserver.listener.actor.player.OnFortressCaptureListener;
import l2s.gameserver.model.Player;

/**
 * @author Java-man
 * @since 12.01.2019
 */
public class FortressCaptureDailyMissionHandler extends ProgressDailyMissionHandler
{
    private class HandlerListeners implements OnFortressCaptureListener
    {
        @Override
        public void onFortressCapture(Player player)
        {
            progressMission(player, 1);
        }
    }

    private final HandlerListeners listeners = new HandlerListeners();

    @Override
    public CharListener getListener()
    {
        return listeners;
    }
}