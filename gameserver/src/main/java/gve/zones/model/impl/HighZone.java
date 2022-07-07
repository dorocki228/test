package gve.zones.model.impl;

import gve.zones.model.GveZone;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.items.ItemInstance;

public class HighZone extends GveZone {
    public HighZone(Zone zone) {
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
    public void onZoneEnter(Creature actor) {
    }

    @Override
    public void onZoneLeave(Creature actor) {
    }
}
