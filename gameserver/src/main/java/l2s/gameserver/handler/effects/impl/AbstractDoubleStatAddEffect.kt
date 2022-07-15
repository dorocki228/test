package l2s.gameserver.handler.effects.impl

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 */
abstract class AbstractDoubleStatAddEffect(
        template: EffectTemplate,
        private val stat: DoubleStat
) : EffectHandler(template) {

    private val amount = params.getDouble(javaClass.simpleName + "_param1")

    override fun pump(
            target: Creature,
            skillEntry: SkillEntry?
    ) {
        if (skillEntry != null) {
            target.stat.mergeAdd(stat, amount, skillEntry)
        } else {
            target.stat.mergeAdd(stat, amount, skill)
        }
    }

}