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
 * @since 12.10.2019
 */
class p_avoid_agro(template: EffectTemplate) : EffectHandler(template) {

    private val chance = params.getDouble("p_avoid_agro_param1")

    override fun pump(
            target: Creature,
            skillEntry: SkillEntry?
    ) {
        if (skillEntry != null) {
            target.stat.mergeAdd(DoubleStat.AVOID_AGGRO, chance, skillEntry)
        } else {
            target.stat.mergeAdd(DoubleStat.AVOID_AGGRO, chance, skill)
        }
    }

}