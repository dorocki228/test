package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.templates.StatsSet

/**
 * @author UnAfraid
 * @author Java-man
 */
class op_instantzone(params: StatsSet) : SkillCondition(params) {

    private val _instanceId = params.getInteger("op_instantzone_param1")

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        return caster.reflection.instancedZoneId == _instanceId
    }

}