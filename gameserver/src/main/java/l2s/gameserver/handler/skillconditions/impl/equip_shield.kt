package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.templates.StatsSet
import l2s.gameserver.templates.item.ExItemType

/**
 * @author Java-man
 */
class equip_shield(params: StatsSet) : SkillCondition(params) {

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        val shield = caster.secondaryWeaponTemplate
        return shield != null && shield.exType == ExItemType.SHIELD
    }

}