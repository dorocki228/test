package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.templates.StatsSet

/**
 * @author UnAfraid
 * @author Java-man
 */
class target_my_party(params: StatsSet) : SkillCondition(params) {

    private val _includeMe = params.getString("target_my_party_param1", "") == "include_me"

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        if (target == null || !target.isPlayer) {
            return false
        }

        val party = caster.party
        val targetParty = target.player.party
        return when {
            party == null -> _includeMe && caster == target
            _includeMe -> party == targetParty
            else -> party == targetParty && caster != target
        }
    }

}