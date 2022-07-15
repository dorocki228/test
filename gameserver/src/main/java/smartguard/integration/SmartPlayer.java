package smartguard.integration;

import l2s.gameserver.model.Player;
import smartguard.api.integration.AbstractSmartClient;
import smartguard.api.integration.ISmartPlayer;

public class SmartPlayer implements ISmartPlayer {

    private Player player;
    private SmartClient smartClient;

    public SmartPlayer(Player player, SmartClient smartClient) {
        this.player = player;
        this.smartClient = smartClient;
    }

    public SmartPlayer(Player player) {
        this.player = player;
        this.smartClient = new SmartClient(player.getNetConnection());
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public int getObjectId() {
        return player.getObjectId();
    }

    @Override
    public boolean isAdmin() {
        return player.isGM();
    }

    @Override
    public AbstractSmartClient getClient() {
        return smartClient;
    }
}
