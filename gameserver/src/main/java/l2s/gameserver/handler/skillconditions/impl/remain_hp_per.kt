package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.skill.SkillConditionAffectType
import l2s.gameserver.model.skill.SkillConditionPercentType
import l2s.gameserver.templates.StatsSet
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * @author UnAfraid
 * @author Java-man
 */
class remain_hp_per(params: StatsSet) : SkillCondition(params) {

    private val _affectType = SkillConditionAffectType.find(params.getString("remain_hp_per_param1"))
    private val _amount = params.getInteger("remain_hp_per_param2")
    private val _percentType = params.getEnum(
            "remain_hp_per_param3",
            SkillConditionPercentType::class.java,
            true
    )

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        when (_affectType) {
            SkillConditionAffectType.SELF -> {
                return _percentType.test(floor(caster.currentHpPercents).roundToInt(), _amount)
            }
            SkillConditionAffectType.TARGET -> {
                if (target != null && target.isCreature) {
                    return _percentType.test(floor(target.currentHpPercents).roundToInt(), _amount)
                }
            }
        }

        return false
    }

}