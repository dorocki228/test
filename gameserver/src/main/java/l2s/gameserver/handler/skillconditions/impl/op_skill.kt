package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.templates.StatsSet

/**
 * @author UnAfraid
 * @author Java-man
 */
class op_skill(params: StatsSet) : SkillCondition(params) {

    private val _skillId = params.getInteger("op_skill_acquire_param1")
    private val _skillLevel = params.getInteger("op_skill_acquire_param2")
    private val _hasLearned = params.getInteger("op_skill_acquire_param3") == 1

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        val skillLevel = caster.getSkillLevel(_skillId)
        return when {
            _hasLearned -> skillLevel == _skillLevel
            else -> skillLevel != _skillLevel
        }
    }

}