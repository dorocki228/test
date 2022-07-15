package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.skill.SkillConditionCompanionType
import l2s.gameserver.templates.StatsSet

/**
 * @author Sdw
 * @author Java-man
 */
class op_companion(params: StatsSet) : SkillCondition(params) {

    private val _type = params.getEnum(
            "op_companion_param1",
            SkillConditionCompanionType::class.java,
            true
    )

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        if (target != null) {
            return when (_type) {
                SkillConditionCompanionType.PET -> {
                    target.isPet
                }
                SkillConditionCompanionType.MY_SUMMON -> {
                    target.isSummon && caster.isMyServitor(target.objectId)
                }
            }
        }

        return false
    }

}