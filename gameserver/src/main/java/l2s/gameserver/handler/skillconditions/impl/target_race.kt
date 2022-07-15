package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.base.NpcRace
import l2s.gameserver.templates.StatsSet

/**
 * @author UnAfraid
 * @author Java-man
 */
class target_race(params: StatsSet) : SkillCondition(params) {

    private val race = NpcRace.find(params.getString("target_race_param1"))

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        if (target == null || !target.isNpc) {
            return false
        }

        return target.asNpc().template.race == race.ordinal
    }

}