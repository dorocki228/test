package l2s.gameserver.model.entity.events.impl.liberation.listeners;

import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.impl.LiberationFortressEvent;
import l2s.gameserver.model.entity.events.impl.liberation.LiberationStatusType;

public class LiberationZoneListener implements OnZoneEnterLeaveListener {
    private static final LiberationZoneListener INSTANCE = new LiberationZoneListener();

    public static LiberationZoneListener getInstance() {
        return INSTANCE;
    }

    @Override
    public void onZoneEnter(Zone zone, Creature creature) {
    }

    @Override
    public void onZoneLeave(Zone zone, Creature creature) {
        LiberationFortressEvent event = EventHolder.getInstance().getEvent(EventType.FUN_EVENT, 1004);
        if (event == null || !zone.getName().equalsIgnoreCase("[liberation_fortress]")) {
            return;
        }
        if (!creature.isPlayer() || event.getStatus() != LiberationStatusType.BATTLE) {
            return;
        }
        Player player = creature.getPlayer();
        event.teleportPlayerToEventZone(player);
    }
}
