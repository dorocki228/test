package l2s.gameserver.model.entity.events.impl.pvparena.listeners;

import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.impl.PvpArenaEvent;

public class PvpArenaZoneListener implements OnZoneEnterLeaveListener {
    private static final PvpArenaPlayerExitListener INSTANCE = new PvpArenaPlayerExitListener();

    public static PvpArenaPlayerExitListener getInstance() {
        return INSTANCE;
    }

    @Override
    public void onZoneEnter(Zone zone, Creature creature) {
        PvpArenaEvent event = EventHolder.getInstance().getEvent(EventType.CUSTOM_PVP_EVENT, 1002);
        if (event == null) {
            return;
        }
        if (!creature.isPlayer()) {
            return;
        }
        Player player = creature.getPlayer();
        event.onEnterZone(player);
    }

    @Override
    public void onZoneLeave(Zone zone, Creature creature) {
        PvpArenaEvent event = EventHolder.getInstance().getEvent(EventType.CUSTOM_PVP_EVENT, 1002);
        if (event == null) {
            return;
        }
        if (!creature.isPlayer()) {
            return;
        }
        Player player = creature.getPlayer();
        event.teleportPlayerToArena(player);
    }
}
