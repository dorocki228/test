package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.Zone
import l2s.gameserver.templates.StatsSet

/**
 * @author Java-man
 */
class can_use_in_battlefield(params: StatsSet) : SkillCondition(params) {

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        return caster.isInZone(Zone.ZoneType.battle_zone)
    }

}