package gve.zones.model.impl;

import gve.zones.model.GveZone;
import l2s.gameserver.Config;
import l2s.gameserver.model.Zone;
import l2s.gameserver.templates.item.ItemGrade;

public class LowZone extends GveZone {
    public LowZone(Zone zone) {
        super(zone);
        setMaxLevel(70);
        setMaxGrade(ItemGrade.C);
        setEnchantLevel(5);
        setRestrictHeroItems(true);
        setRestrictedItems(Config.GVE_LOW_ZONE_RESTRICTED_ITEMS);
    }
}
