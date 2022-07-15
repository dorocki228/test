package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.templates.StatsSet

/**
 * @author Sdw
 */
class cannot_use_in_transform(params: StatsSet) : SkillCondition(params) {

    private val _transformId = params.getInteger("cannot_use_in_transform_param1")

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        return when {
            _transformId > 0 -> caster.transformId != _transformId
            else -> !caster.isTransformed
        }
    }

}