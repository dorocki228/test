package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.Zone
import l2s.gameserver.templates.StatsSet

/**
 * @author UnAfraid
 * @author Java-man
 */
class op_peacezone(params: StatsSet) : SkillCondition(params) {

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        return caster.isInZone(Zone.ZoneType.peace_zone)
    }

}