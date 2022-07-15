package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.entity.residence.ResidenceType
import l2s.gameserver.templates.StatsSet

/**
 * @author Sdw
 * @author Java-man
 */
class op_home(params: StatsSet) : SkillCondition(params) {

    private val _type = params.getEnum(
            "op_home_param1",
            ResidenceType::class.java,
            true
    )

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        if (caster.isPlayer) {
            val player = caster.player
            return when (_type) {
                ResidenceType.CASTLE -> player.castle != null
                ResidenceType.AGIT -> player.clanHall != null
            }
        }

        return false
    }

}