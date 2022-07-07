package l2s.gameserver.model.entity.events.impl.liberation.listeners;

import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.listener.actor.OnDeathFromUndyingListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.impl.LiberationFortressEvent;

public class LiberationOnDeathFromUndyingListenerImpl implements OnDeathFromUndyingListener {
    private static final LiberationOnDeathFromUndyingListenerImpl INSTANCE = new LiberationOnDeathFromUndyingListenerImpl();

    public static LiberationOnDeathFromUndyingListenerImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public void onDeathFromUndying(Creature actor, Creature killer) {
        Player player = actor.getPlayer();
        if (player == null) {
            return;
        }
        LiberationFortressEvent event = EventHolder.getInstance().getEvent(EventType.FUN_EVENT, 1004);
        if (event == null) {
            return;
        }
        event.onDie(player, killer);
    }
}
