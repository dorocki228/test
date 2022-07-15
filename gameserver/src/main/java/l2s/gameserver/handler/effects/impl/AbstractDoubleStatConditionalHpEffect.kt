package l2s.gameserver.handler.effects.impl

import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 */
abstract class AbstractDoubleStatConditionalHpEffect(
        template: EffectTemplate,
        stat: DoubleStat,
        private val hpPercent: Int
) : AbstractDoubleStatConditionalItemTypeEffect(template, stat) {

    init {
        require(hpPercent >= 0)
    }

    override fun checkPumpCondition(abnormal: Abnormal?, caster: Creature, target: Creature): Boolean {
        return super.checkPumpCondition(abnormal, caster, target) && target.currentHpPercents <= hpPercent
    }

}