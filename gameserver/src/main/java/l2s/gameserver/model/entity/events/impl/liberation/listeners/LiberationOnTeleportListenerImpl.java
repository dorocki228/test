package l2s.gameserver.model.entity.events.impl.liberation.listeners;

import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.listener.actor.player.OnTeleportListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.impl.LiberationFortressEvent;

public class LiberationOnTeleportListenerImpl implements OnTeleportListener {
    private static final LiberationOnTeleportListenerImpl INSTANCE = new LiberationOnTeleportListenerImpl();

    public static LiberationOnTeleportListenerImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public void onTeleport(Player player, int x, int y, int z, Reflection reflection) {
        if (player == null) {
            return;
        }
        LiberationFortressEvent event = EventHolder.getInstance().getEvent(EventType.FUN_EVENT, 1004);
        if (event == null) {
            return;
        }
        if (!event.isInProgress()) {
            return;
        }
        Fraction fraction = player.getFraction();
        Reflection reflectionFromFaction = event.getReflectionFromFaction(fraction);
        if (reflectionFromFaction == null) {
            return;
        }
        if (reflectionFromFaction.getId() == reflection.getId()) {
            return;
        }
        event.logoutOrTeleportFromEvent(player, true);
    }

}
