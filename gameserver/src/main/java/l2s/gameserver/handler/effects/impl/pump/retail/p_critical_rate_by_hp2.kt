package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.impl.AbstractDoubleStatConditionalHpEffect
import l2s.gameserver.model.Creature
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.stats.StatModifierType
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 *
 * @since 16.10.2019
 */
class p_critical_rate_by_hp2(template: EffectTemplate) :
        AbstractDoubleStatConditionalHpEffect(template, DoubleStat.CRITICAL_RATE, 60) {

    override fun pump(
            target: Creature,
            skillEntry: SkillEntry?
    ) {
        when (modifierType) {
            StatModifierType.DIFF -> {
                if (skillEntry != null) {
                    target.stat.mergeAdd(addStat, amount, skillEntry)
                } else {
                    target.stat.mergeAdd(addStat, amount, skill)
                }
            }
            StatModifierType.PER -> {
                if (skillEntry != null) {
                    target.stat.mergeMul(mulStat, amount / 100.0, skillEntry)
                } else {
                    target.stat.mergeMul(mulStat, amount / 100.0, skill)
                }
            }
        }
    }

}