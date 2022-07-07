package l2s.gameserver.stats.conditions.link;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.service.FactionLeaderService;
import l2s.gameserver.stats.conditions.Condition;

/**
 * @author KRonst
 */
public class FactionLeaderTimeState extends Condition {

    private boolean inRange;

    @Override
    protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value) {
        return FactionLeaderService.getInstance().isLeaderTime() == inRange;
    }
}
