package l2s.gameserver.model.entity.events.impl.brevent.listeners;

import l2s.gameserver.listener.actor.player.OnPlayerExitListener;
import l2s.gameserver.listener.actor.player.OnTeleportListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.impl.brevent.BREvent;
import l2s.gameserver.model.entity.events.impl.brevent.enums.EBREventState;

import java.util.Objects;

/**
 * @author : Nami
 * @author Java-man
 * @date : 19.06.2018
 * @time : 21:25
 * <p/>
 */
public class BREventPlayerLeaveListener implements OnPlayerExitListener, OnTeleportListener
{
    private final BREvent event;

    public BREventPlayerLeaveListener(BREvent event)
    {
        this.event = event;
    }

    @Override
    public void onPlayerExit(Player player) {
        var playerObject = event.getEventPlayerObject(player);
        playerObject.ifPresent(temp -> event.getEventHandler(EBREventState.END).invoke(event, temp));
    }

    @Override
    public void onTeleport(Player player, int x, int y, int z, Reflection reflection)
    {
        if(Objects.equals(event.getReflection(), reflection))
            return;

        var playerObject = event.getEventPlayerObject(player);
        playerObject.ifPresent(temp -> event.getEventHandler(EBREventState.END).invoke(event, temp));
    }
}
