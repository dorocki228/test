package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.templates.StatsSet

/**
 * @author Sdw
 * @author Java-man
 */
class op_check_residence(params: StatsSet) : SkillCondition(params) {

    private val _residencesIds = params.getIntegerArray("op_check_residence_param1")
    private val result = params.getInteger("op_check_residence_param2") == 1

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        if (caster.isPlayer) {
            val clanHall = caster.player.clanHall
            if (clanHall != null) {
                return _residencesIds.contains(clanHall.id) == result
            }
        }

        return !result
    }

}