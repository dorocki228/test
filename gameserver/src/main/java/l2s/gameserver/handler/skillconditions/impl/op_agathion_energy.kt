package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.templates.StatsSet

/**
 * @author Java-man
 */
class op_agathion_energy(params: StatsSet) : SkillCondition(params) {

    private val agathionEnergyNeed = params.getInteger("op_agathion_energy_param1")

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        return caster.agathionEnergy >= agathionEnergyNeed
    }

}