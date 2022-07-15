package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.skill.SkillConditionAffectType
import l2s.gameserver.skills.AbnormalType
import l2s.gameserver.templates.StatsSet

/**
 * @author UnAfraid
 * @author Java-man
 */
class op_check_abnormal(params: StatsSet) : SkillCondition(params) {

    private val _type = params.getEnum(
            "op_check_abnormal_param1",
            AbnormalType::class.java,
            true
    )
    private val _level = params.getInteger("op_check_abnormal_param2")
    private val _hasAbnormal = params.getInteger("op_check_abnormal_param3") == 1
    private val _affectType = params.getEnum(
            "op_check_abnormal_param4",
            SkillConditionAffectType::class.java,
            SkillConditionAffectType.TARGET,
            true
    )

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        when (_affectType) {
            SkillConditionAffectType.SELF -> {
                return caster.abnormalList.contains(_type) {
                    it.skill.abnormalLvl >= _level
                } == _hasAbnormal
            }
            SkillConditionAffectType.TARGET -> {
                if (target != null && target.isCreature) {
                    return target.abnormalList.contains(_type) {
                        it.skill.abnormalLvl >= _level
                    } == _hasAbnormal
                }
            }
        }

        return false
    }

}