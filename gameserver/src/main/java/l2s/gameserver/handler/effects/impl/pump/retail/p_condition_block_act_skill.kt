package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.skills.AbnormalType
import l2s.gameserver.stats.BooleanStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * Block Actions except item id effect implementation.
 *
 * @author Sdw
 * @author Java-man
 */
class p_condition_block_act_skill(template: EffectTemplate) : EffectHandler(template) {

    private val allowedSkills: List<Int> = params.getString("p_condition_block_act_skill_param1")
            .split(";")
            .map { it.toInt() }
            .distinct()

    override fun checkPumpCondition(abnormal: Abnormal?, caster: Creature, target: Creature): Boolean {
        if (skill.abnormalTypeList.contains(AbnormalType.PARALYZE)) {
            if (target.isParalyzeImmune) {
                return false
            }

            // paralyze нельзя наложить на осадных саммонов
            val npc = target.asNpc()
            if (npc != null && npc.template.race == 21) {
                return false
            }
        }

        return true
    }

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.stopActions()
    }

    override fun pump(target: Creature, skillEntry: SkillEntry?) {
        target.stat.set(BooleanStat.BLOCK_ACTIONS)
        allowedSkills.forEach { target.stat.addBlockActionsAllowedSkill(it) }
    }

}