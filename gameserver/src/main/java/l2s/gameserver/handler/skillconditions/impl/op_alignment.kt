package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.skill.SkillConditionAffectType
import l2s.gameserver.model.skill.SkillConditionAlignment
import l2s.gameserver.templates.StatsSet

/**
 * @author Sdw
 * @author Java-man
 */
class op_alignment(params: StatsSet) : SkillCondition(params) {

    private val _alignment = params.getEnum(
            "op_alignment_param1",
            SkillConditionAlignment::class.java,
            true
    )
    private val _affectType =
            SkillConditionAffectType.find(params.getString("op_alignment_param2"))

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        when (_affectType) {
            SkillConditionAffectType.SELF -> {
                return _alignment.test(caster.player)
            }
            SkillConditionAffectType.TARGET -> {
                if (target != null && target.isPlayer) {
                    return _alignment.test(target.player)
                }
            }
        }

        return false
    }

}