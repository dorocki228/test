package l2s.gameserver.service;

import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.impl.SingleMatchEvent;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.snapshot.SnapshotPlayer;

import java.util.List;

/**
 * @author mangol
 */
public class PlayerService {
    private static final PlayerService instance = new PlayerService();

    private PlayerService() {
    }

    public static PlayerService getInstance() {
        return instance;
    }

    public boolean isParticipatesEvent(Player player) {
        return player.containsEvent(Event.class);
    }

    public boolean isParticipatesOlympiad(Player player) {
        return player.isInOlympiadMode() || Olympiad.isRegisteredInComp(player);
    }

    public boolean isPlayerRegisteredEvent(Player player) {
        List<SingleMatchEvent> events = EventHolder.getInstance().getEvents(SingleMatchEvent.class);
        for (SingleMatchEvent event : events) {
            if (event.isPlayerRegistered(player)) {
                return true;
            }
        }
        return false;
    }

    public boolean isPlayerRegisteredEvent(Player player, Class<? extends SingleMatchEvent>... ignores) {
        List<SingleMatchEvent> events = EventHolder.getInstance().getEvents(SingleMatchEvent.class);
        for (SingleMatchEvent event : events) {
            if (isEventAssignableFrom(event, ignores)) {
                continue;
            }
            if (event.isPlayerRegistered(player)) {
                return true;
            }
        }
        return false;
    }

    private boolean isEventAssignableFrom(Event event, Class<? extends Event>... classes) {
        for (Class<? extends Event> aClass : classes) {
            if (isEventAssignableFrom(event, aClass)) {
                return true;
            }
        }
        return false;
    }

    private boolean isEventAssignableFrom(Event event, Class<? extends Event> clazz) {
        return clazz.isInstance(event) || clazz.isAssignableFrom(event.getClass());
    }

    public SnapshotPlayer createSnapshot(Player player) {
        return new SnapshotPlayer(player);
    }

    public void recoverFromSnapshotCpHpMp(Player player, SnapshotPlayer snapshotPlayer) {
        if (player == null || player.isLogoutStarted()) {
            return;
        }
        player.setCurrentCp(snapshotPlayer.getCurrentCp());
        player.setCurrentHp(snapshotPlayer.getCurrentHp(), false);
        player.setCurrentMp(snapshotPlayer.getCurrentMp());
    }

    public void recoverFromSnapshotEffect(Player player, SnapshotPlayer snapshotPlayer) {
        if (player == null || player.isLogoutStarted()) {
            return;
        }
        snapshotPlayer.getAbnormals().forEach(player.getAbnormalList()::addEffect);
    }
}
