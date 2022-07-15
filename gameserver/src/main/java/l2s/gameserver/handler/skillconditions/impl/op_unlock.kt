package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.instances.ChestInstance
import l2s.gameserver.templates.StatsSet

/**
 * @author Sdw
 * @author Java-man
 */
class op_unlock(params: StatsSet) : SkillCondition(params) {

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        return target != null && (target.isDoor || target is ChestInstance)
    }

}