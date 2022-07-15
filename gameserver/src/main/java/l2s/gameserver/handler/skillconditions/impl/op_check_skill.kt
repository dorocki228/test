package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.skill.SkillConditionAffectType
import l2s.gameserver.templates.StatsSet

/**
 * @author Sdw
 * @author Java-man
 */
class op_check_skill(params: StatsSet) : SkillCondition(params) {

    private val _skillId = params.getInteger("op_check_skill_param1")
    private val _affectType =
            SkillConditionAffectType.find(params.getString("op_check_skill_param2"))

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        when (_affectType) {
            SkillConditionAffectType.SELF -> {
                return caster.getSkillLevel(_skillId) > 0
            }
            SkillConditionAffectType.TARGET -> {
                if (target != null && target.isPlayer) {
                    return target.getSkillLevel(_skillId) > 0
                }
            }
        }

        return false
    }

}