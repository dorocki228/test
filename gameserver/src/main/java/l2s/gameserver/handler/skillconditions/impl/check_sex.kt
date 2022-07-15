package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.base.Sex
import l2s.gameserver.templates.StatsSet

/**
 * @author Java-man
 */
class check_sex(params: StatsSet) : SkillCondition(params) {

    private val sex = when {
        params.getString("check_sex_param1") == "F" -> Sex.FEMALE
        else -> Sex.MALE
    }

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        return caster.isPlayer && caster.player.sex == sex
    }

}