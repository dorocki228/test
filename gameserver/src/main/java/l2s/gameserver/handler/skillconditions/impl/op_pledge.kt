package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.templates.StatsSet

/**
 * @author UnAfraid
 * @author Java-man
 */
class op_pledge(params: StatsSet) : SkillCondition(params) {

    private val _level = params.getInteger("op_pledge_param1")

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        val clan = caster.clan
        return clan != null && clan.level >= _level
    }

}