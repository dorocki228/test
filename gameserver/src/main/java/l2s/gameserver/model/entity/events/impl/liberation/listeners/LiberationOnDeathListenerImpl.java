package l2s.gameserver.model.entity.events.impl.liberation.listeners;

import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.listener.actor.OnDeathListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.impl.LiberationFortressEvent;
import l2s.gameserver.model.entity.events.impl.liberation.LiberationRoomTeam;

public class LiberationOnDeathListenerImpl implements OnDeathListener {
    private static final LiberationOnDeathListenerImpl INSTANCE = new LiberationOnDeathListenerImpl();

    public static LiberationOnDeathListenerImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public void onDeath(Creature victim, Creature killer) {
        if (killer == null || victim == null) {
            return;
        }
        LiberationFortressEvent event = EventHolder.getInstance().getEvent(EventType.FUN_EVENT, 1004);
        if (event == null) {
            return;
        }
        if (!event.isInProgress()) {
            return;
        }
        Player player = killer.getPlayer();
        if (player == null) {
            return;
        }
        LiberationRoomTeam roomTeam = event.getRoomTeam(player.getFraction());
        if (roomTeam == null) {
            return;
        }
        roomTeam.setLastKillTimestamp(System.currentTimeMillis());
        roomTeam.incrementPoints();
    }
}
