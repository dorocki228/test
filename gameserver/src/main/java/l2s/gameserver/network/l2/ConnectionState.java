package l2s.gameserver.network.l2;

import l2s.commons.network.IConnectionState;

/**
 * @author Nos
 */
public enum ConnectionState implements IConnectionState {
    CONNECTED,
    DISCONNECTED,
    CLOSING,
    AUTHENTICATED,
    JOINING_GAME,
    IN_GAME
}
