package l2s.gameserver.model.entity.events.impl.pvparena.listeners;

import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.listener.actor.player.OnPlayerExitListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.impl.PvpArenaEvent;

public class PvpArenaListenRegistered implements OnPlayerExitListener {
    private static final PvpArenaListenRegistered INSTANCE = new PvpArenaListenRegistered();

    public static PvpArenaListenRegistered getInstance() {
        return INSTANCE;
    }

    private PvpArenaListenRegistered() {
    }

    @Override
    public void onPlayerExit(Player p0) {
        PvpArenaEvent event = EventHolder.getInstance().getEvent(EventType.CUSTOM_PVP_EVENT, 1002);
        if (event == null) {
            return;
        }
        event.unregisterPlayer(p0);
    }
}
