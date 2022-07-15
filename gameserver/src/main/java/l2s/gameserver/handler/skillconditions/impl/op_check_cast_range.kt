package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.templates.StatsSet

/**
 * @author Sdw
 */
class op_check_cast_range(params: StatsSet) : SkillCondition(params) {

    private val _distance = params.getInteger("op_check_cast_range_param1");

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        return target != null && caster.distance3d(target) >= _distance
    }

}