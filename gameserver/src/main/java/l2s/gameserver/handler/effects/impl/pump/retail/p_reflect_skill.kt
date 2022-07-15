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
class p_reflect_skill(template: EffectTemplate) : EffectHandler(template) {

    private val amount1 = params.getDouble("p_reflect_skill_param1")
    private val amount2 = params.getDouble("p_reflect_skill_param2")
    private val amount3 = params.getDouble("p_reflect_skill_param3")
    private val amount4 = params.getDouble("p_reflect_skill_param4")
    private val amount5 = params.getDouble("p_reflect_skill_param5")
    private val amount6 = params.getDouble("p_reflect_skill_param6")

    override fun pump(
            target: Creature,
            skillEntry: SkillEntry?
    ) {
        if (skillEntry != null) {
            target.stat.mergeAdd(DoubleStat.REFLECT_SKILL_PHYSIC, amount1, skillEntry)
        } else {
            target.stat.mergeAdd(DoubleStat.REFLECT_SKILL_PHYSIC, amount1, skill)
        }
        if (skillEntry != null) {
            target.stat.mergeAdd(DoubleStat.REFLECT_SKILL_MAGIC, amount2, skillEntry)
        } else {
            target.stat.mergeAdd(DoubleStat.REFLECT_SKILL_MAGIC, amount2, skill)
        }
        // TODO magical target.stat.mergeAdd(DoubleStat.RESIST_ABNORMAL_DEBUFF, amount3)
        // TODO physical target.stat.mergeAdd(DoubleStat.RESIST_ABNORMAL_DEBUFF, amount4)
        // TODO target.stat.mergeAdd(DoubleStat.REFLECT_SKILL_MAGIC, amount5)
        // TODO target.stat.mergeAdd(DoubleStat.REFLECT_SKILL_MAGIC, amount6)
    }

}