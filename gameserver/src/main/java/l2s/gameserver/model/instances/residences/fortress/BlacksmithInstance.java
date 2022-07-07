package l2s.gameserver.model.instances.residences.fortress;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.templates.npc.NpcTemplate;

public class BlacksmithInstance extends UpgradeFortressInstance {
    public BlacksmithInstance(int objectId, NpcTemplate template, MultiValueSet<String> set) {
        super(objectId, template, set);
    }

    @Override
    protected int getCond(Player player) {
        Residence residence = getResidence();
        Fraction fraction = residence.getFraction();
        if (fraction != null && player.getFraction() == fraction) {
            if (residence.getSiegeEvent().isInProgress())
                return COND_SIEGE;
            else {
                PcInventory inventory = player.getInventory();
                if (inventory.getCountOf(57) > 0)
                    return COND_OWNER;
                else
                    return COND_FAIL;
            }
        } else
            return COND_FAIL;
    }
}