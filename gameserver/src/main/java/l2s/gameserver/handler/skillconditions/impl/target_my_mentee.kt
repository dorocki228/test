package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.templates.StatsSet

/**
 *
 */
class target_my_mentee(params: StatsSet) : SkillCondition(params) {

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        return false
    }

}