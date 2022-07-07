package l2s.gameserver.model.entity.events.impl.pvparena.listeners;

import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.listener.actor.OnDeathFromUndyingListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.impl.PvpArenaEvent;

public class PvpArenaOnDeathFromUndyingListenerImpl implements OnDeathFromUndyingListener {
    private static final PvpArenaOnDeathFromUndyingListenerImpl INSTANCE = new PvpArenaOnDeathFromUndyingListenerImpl();

    public static PvpArenaOnDeathFromUndyingListenerImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public void onDeathFromUndying(Creature actor, Creature killer) {
        Player player = actor.getPlayer();
        if (player == null) {
            return;
        }
        PvpArenaEvent event = EventHolder.getInstance().getEvent(EventType.CUSTOM_PVP_EVENT, 1002);
        if (event == null) {
            return;
        }
        event.onDie(player, killer);
    }
}
