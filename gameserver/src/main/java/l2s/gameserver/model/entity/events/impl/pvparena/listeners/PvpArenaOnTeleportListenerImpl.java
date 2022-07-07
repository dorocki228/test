package l2s.gameserver.model.entity.events.impl.pvparena.listeners;

import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.listener.actor.player.OnTeleportListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.impl.PvpArenaEvent;

public class PvpArenaOnTeleportListenerImpl implements OnTeleportListener {
    private static final PvpArenaOnTeleportListenerImpl INSTANCE = new PvpArenaOnTeleportListenerImpl();

    public static PvpArenaOnTeleportListenerImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public void onTeleport(Player player, int x, int y, int z, Reflection reflection) {
        if (player == null) {
            return;
        }
        PvpArenaEvent event = EventHolder.getInstance().getEvent(EventType.CUSTOM_PVP_EVENT, 1002);
        if (event == null) {
            return;
        }
        if (!event.isInProgress()) {
            return;
        }
        if (event.getReflection() == null) {
            return;
        }
        if (event.getReflection().getId() == reflection.getId()) {
            return;
        }
        event.logoutOrTeleportFromBattle(player, true);
    }

}
