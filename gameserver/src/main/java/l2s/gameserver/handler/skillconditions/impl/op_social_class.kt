package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.base.PledgeRank
import l2s.gameserver.templates.StatsSet

/**
 * @author UnAfraid
 * @author Java-man
 */
class op_social_class(params: StatsSet) : SkillCondition(params) {

    private val _socialClass = PledgeRank.values()[params.getInteger("op_social_class_param1")]

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        val player = caster.player
        return player != null && player.pledgeRank.ordinal >= _socialClass.ordinal
    }

}