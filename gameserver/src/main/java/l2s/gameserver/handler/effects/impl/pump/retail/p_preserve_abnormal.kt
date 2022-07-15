package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.impl.AbstractBooleanStatEffect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.stats.BooleanStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * Noblesse Blessing effect implementation.
 * @author earendil
 * @author Java-man
 */
class p_preserve_abnormal(template: EffectTemplate) :
        AbstractBooleanStatEffect(template, BooleanStat.PRESERVE_ABNORMAL) {

    override fun checkPumpCondition(abnormal: Abnormal?, caster: Creature, target: Creature): Boolean {
        return target.isPlayable
    }

}