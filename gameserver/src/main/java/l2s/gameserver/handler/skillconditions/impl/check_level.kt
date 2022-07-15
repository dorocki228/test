package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.skill.SkillConditionAffectType
import l2s.gameserver.templates.StatsSet

/**
 * @author Sdw
 */
class check_level(params: StatsSet) : SkillCondition(params) {

    private val _minLevel = params.getInteger("check_level_param1")
    private val _maxLevel = params.getInteger("check_level_param2")
    private val _affectType = SkillConditionAffectType.find(params.getString("check_level_param3"))

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        when (_affectType) {
            SkillConditionAffectType.SELF -> {
                return caster.level in _minLevel.._maxLevel
            }
            SkillConditionAffectType.TARGET -> {
                if (target != null) {
                    return target.level in _minLevel.._maxLevel
                }
            }
        }

        return false
    }

}