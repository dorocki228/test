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
 * @since 14.10.2019
 */
class p_counter_skill(template: EffectTemplate) : EffectHandler(template) {

    // TODO use unk. maybe it is min percent ?
    private val unk = params.getDouble("p_counter_skill_param1")
    private val amount = params.getDouble("p_counter_skill_param2")

    override fun pump(
            target: Creature,
            skillEntry: SkillEntry?
    ) {
        if (skillEntry != null) {
            target.stat.mergeAdd(DoubleStat.VENGEANCE_SKILL_PHYSICAL_DAMAGE, amount, skillEntry)
        } else {
            target.stat.mergeAdd(DoubleStat.VENGEANCE_SKILL_PHYSICAL_DAMAGE, amount, skill)
        }
    }

}