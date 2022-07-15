package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.skill.SkillConditionAffectType
import l2s.gameserver.templates.StatsSet

/**
 * @author UnAfraid
 * @author Java-man
 */
class op_pkcount(params: StatsSet) : SkillCondition(params) {

    private val _affectType =
            SkillConditionAffectType.find(params.getString("op_pkcount_param1"))

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        when (_affectType) {
            SkillConditionAffectType.SELF -> {
                return caster.isPlayer && caster.player.pkKills > 0
            }
            SkillConditionAffectType.TARGET -> {
                if (target != null && target.isPlayer) {
                    return target.player.pkKills > 0
                }
            }
        }

        return false
    }

}