package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.templates.StatsSet

/**
 * @author UnAfraid
 * @author Java-man
 */
class op_energy_max(params: StatsSet) : SkillCondition(params) {

    private val _amount = params.getInteger("op_energy_max_param1")

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        if (caster.charges >= _amount) {
            caster.sendPacket(SystemMsg.YOUR_FORCE_HAS_REACHED_MAXIMUM_CAPACITY)
            return false
        }

        return true
    }

}