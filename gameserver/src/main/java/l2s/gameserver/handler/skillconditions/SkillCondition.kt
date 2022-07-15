package l2s.gameserver.handler.skillconditions

import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.templates.StatsSet

/**
 * @author NosBit
 */
abstract class SkillCondition(params: StatsSet) {

    abstract fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean

}