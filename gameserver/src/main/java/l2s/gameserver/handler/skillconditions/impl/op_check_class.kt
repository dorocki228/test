package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.base.ClassId
import l2s.gameserver.model.skill.SkillConditionAffectType
import l2s.gameserver.templates.StatsSet

/**
 * @author UnAfraid
 * @author Java-man
 */
class op_check_class(params: StatsSet) : SkillCondition(params) {

    private val _classId = params.getInteger("op_check_class_param1")
            .let { ClassId.valueOf(it) }
    private val _affectType =
            SkillConditionAffectType.find(params.getString("op_check_class_param2"))
    private val result = params.getInteger("op_check_class_param3") == 1

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        when (_affectType) {
            SkillConditionAffectType.SELF -> {
                return caster.isPlayer && (caster.player.classId == _classId) == result
            }
            SkillConditionAffectType.TARGET -> {
                if (target != null && target.isPlayer) {
                    return (target.player.classId == _classId) == result
                }
            }
        }

        return false
    }

}