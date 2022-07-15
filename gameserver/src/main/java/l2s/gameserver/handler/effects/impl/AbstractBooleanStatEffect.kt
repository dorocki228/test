package l2s.gameserver.handler.effects.impl

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.stats.BooleanStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 */
abstract class AbstractBooleanStatEffect(
        template: EffectTemplate,
        private val stat: BooleanStat
) : EffectHandler(template) {

    override fun pump(
            target: Creature,
            skillEntry: SkillEntry?
    ) {
        target.stat.set(stat)
    }

}