package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.templates.StatsSet

/**
 * @author Sdw
 * @author Java-man
 */
class op_skill_acquire(params: StatsSet) : SkillCondition(params) {

    private val _skillId = params.getInteger("op_skill_acquire_param1")
    private val _hasLearned = params.getInteger("op_skill_acquire_param2") == 1

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        if (target != null) {
            val skillLevel: Int = target.getSkillLevel(_skillId, 0)
            return when {
                _hasLearned -> skillLevel != 0
                else -> skillLevel == 0
            }
        }

        return false
    }

}