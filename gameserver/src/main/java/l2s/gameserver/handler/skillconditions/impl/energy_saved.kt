package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.templates.StatsSet

/**
 * @author Sdw
 */
class energy_saved(params: StatsSet) : SkillCondition(params) {

    private val _amount = params.getInteger("energy_saved_param1")

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        return caster.charges >= _amount
    }

}