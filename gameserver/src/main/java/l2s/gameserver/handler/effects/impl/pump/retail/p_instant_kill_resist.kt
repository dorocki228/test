package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * @author Sdw
 * @author Java-man
 *
 * @since 13.10.2019
 */
class p_instant_kill_resist(template: EffectTemplate) : EffectHandler(template) {

    private val amount = params.getDouble("p_instant_kill_resist_param1")

    override fun pump(
            target: Creature,
            skillEntry: SkillEntry?
    ) {
        if (skillEntry != null) {
            target.stat.mergeAdd(DoubleStat.INSTANT_KILL_RESIST, 1.0 - amount / 100.0, skillEntry)
        } else {
            target.stat.mergeAdd(DoubleStat.INSTANT_KILL_RESIST, 1.0 - amount / 100.0, skill)
        }
    }

}