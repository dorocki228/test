package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;

/**
 * @author KRonst
 */
public class ConditionPlayerCurrentHp extends Condition {
    private final double hp;

    public ConditionPlayerCurrentHp(double hp) {
        this.hp = hp;
    }

    @Override
    protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value) {
        return creature.getCurrentHp() >= hp;
    }
}
