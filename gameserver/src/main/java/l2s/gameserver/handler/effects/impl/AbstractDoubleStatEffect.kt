package l2s.gameserver.handler.effects.impl

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.stats.StatModifierType
import l2s.gameserver.stats.conditions.Condition
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 */
abstract class AbstractDoubleStatEffect(
        template: EffectTemplate,
        protected val mulStat: DoubleStat,
        protected val addStat: DoubleStat
) : EffectHandler(template) {

    protected val amount = params.getDouble(javaClass.simpleName + "_param1")
    protected val modifierType: StatModifierType =
            params.getEnum(
                    javaClass.simpleName + "_param2",
                    StatModifierType::class.java,
                    true
            )

    constructor(
            template: EffectTemplate,
            stat: DoubleStat
    ) : this(template, stat, stat)

    override fun getCondition(): Condition? {
        return null
    }

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
                    target.stat.mergeMul(mulStat, amount / 100.0 + 1.0, skillEntry)
                } else {
                    target.stat.mergeMul(mulStat, amount / 100.0 + 1.0, skill)
                }
            }
        }
    }

}