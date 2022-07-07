package handler.dailymissions;

import l2s.gameserver.listener.CharListener;
import l2s.gameserver.listener.actor.player.OnPlayerReflectionListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;

/**
 * @author Java-man
 * @since 13.01.2019
 */
public class KamalokaEnterDailyMissionHandler extends ProgressDailyMissionHandler
{
    private class HandlerListeners implements OnPlayerReflectionListener
    {
        @Override
        public void onPlayerEnterReflection(Player player, Reflection reflection)
        {
            if(reflection.getInstancedZoneId() == 79)
                progressMission(player, 1);
        }

        @Override
        public void onPlayerExitReflection(Player player, Reflection reflection)
        {
        }
    }

    private final HandlerListeners listeners = new HandlerListeners();

    @Override
    public CharListener getListener()
    {
        return listeners;
    }
}