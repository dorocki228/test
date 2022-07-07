package handler.dailymissions;

import l2s.gameserver.listener.CharListener;
import l2s.gameserver.listener.actor.player.OnArtifactCaptureListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.ArtifactInstance;

/**
 * @author Java-man
 * @since 12.01.2019
 */
public class ArtifactCaptureDailyMissionHandler extends ProgressDailyMissionHandler
{
    private class HandlerListeners implements OnArtifactCaptureListener
    {
        @Override
        public void onArtifactCapture(Player player, ArtifactInstance artifact)
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