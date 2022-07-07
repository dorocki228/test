package l2s.gameserver.model.entity.events.impl.liberation.listeners;

import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.listener.actor.player.OnPlayerExitListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.impl.LiberationFortressEvent;

public class LiberationPlayerExitListener implements OnPlayerExitListener {
    private static final LiberationPlayerExitListener INSTANCE = new LiberationPlayerExitListener();

    public static LiberationPlayerExitListener getInstance() {
        return INSTANCE;
    }

    @Override
    public void onPlayerExit(Player p0) {
        LiberationFortressEvent event = EventHolder.getInstance().getEvent(EventType.FUN_EVENT, 1004);
        if (event == null) {
            return;
        }
        event.logoutOrTeleportFromEvent(p0, true);
    }
}
