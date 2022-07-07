package l2s.gameserver.model.instances;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ActionFailPacket;
import l2s.gameserver.templates.npc.NpcTemplate;

/**
 * @author KRonst
 */
public class NoActionObjectInstance extends NpcInstance {

    public NoActionObjectInstance(int objectId, NpcTemplate template, MultiValueSet<String> set) {
        super(objectId, template, set);
    }

    @Override
    public boolean isTargetable(Creature creature) {
        return false;
    }

    @Override
    public boolean isAttackable(Creature attacker) {
        return false;
    }

    @Override
    public boolean isAutoAttackable(Creature attacker) {
        return false;
    }

    @Override
    public void onAction(Player player, boolean shift) {
        if (player != null) {
            player.sendPacket(ActionFailPacket.STATIC);
        }
    }
}
