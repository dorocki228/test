package l2s.gameserver.model.entity.events.impl;

import l2s.gameserver.model.Player;

public interface IRegistrationEvent {
    default boolean registerPlayer(Player player) {
        return true;
    }

    default boolean unregisterPlayer(Player player) {
        return true;
    }

    default boolean isRegistrationOver() {
        return true;
    }

    default boolean isPlayerRegistered(Player player) {
        return true;
    }
}
