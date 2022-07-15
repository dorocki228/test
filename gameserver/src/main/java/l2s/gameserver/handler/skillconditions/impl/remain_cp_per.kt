package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.skill.SkillConditionPercentType
import l2s.gameserver.templates.StatsSet
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * @author UnAfraid
 * @author Java-man
 */
class remain_cp_per(params: StatsSet) : SkillCondition(params) {

    private val _amount = params.getInteger("remain_cp_per_param1")
    private val _percentType = params.getEnum(
            "remain_cp_per_param2",
            SkillConditionPercentType::class.java,
            true
    )

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        return _percentType.test(floor(caster.currentCpPercents).roundToInt(), _amount)
    }

}