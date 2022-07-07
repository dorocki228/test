package gve.zones.model.impl;

import gve.zones.model.GveZone;
import l2s.commons.util.Rnd;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.utils.Location;

public class PvpZone extends GveZone {
    public PvpZone(Zone zone) {
        super(zone);
    }

    @Override
    public boolean canEnterZone(Player player) {
        return true;
    }

    @Override
    public boolean canEquipItem(Player player, ItemInstance item) {
        return true;
    }

    @Override
    public Location getRandomRespawnLoc(Player player) {
        return Rnd.get(getZone().getRestartPoints());
    }

    @Override
    public Location getClosestRespawnLoc(Player player) {
        return Rnd.get(getZone().getRestartPoints());
    }
}
