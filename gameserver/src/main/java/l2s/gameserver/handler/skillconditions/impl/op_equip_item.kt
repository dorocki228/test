package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.skill.SkillConditionAffectType
import l2s.gameserver.templates.StatsSet

/**
 * @author Sdw
 * @author Java-man
 */
class op_equip_item(params: StatsSet) : SkillCondition(params) {

    private val _itemId = params.getInteger("op_equip_item_param1")
    private val _affectType =
            SkillConditionAffectType.find(params.getString("op_equip_item_param2"))

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        when (_affectType) {
            SkillConditionAffectType.SELF -> {
                if (caster.isPlayer) {
                    return caster.player.inventory.items
                            .any { it.isEquipped && it.itemId == _itemId }
                }
            }
            SkillConditionAffectType.TARGET -> {
                if (target != null && target.isPlayer) {
                    return target.player.inventory.items
                            .any { it.isEquipped && it.itemId == _itemId }
                }
            }
            SkillConditionAffectType.BOTH -> {
                if (target != null && target.isPlayer) {
                    val casterOk = caster.player.inventory.items
                            .any { it.isEquipped && it.itemId == _itemId }
                    if (!casterOk) {
                        return false
                    }
                    val targetOk = target.player.inventory.items
                            .any { it.isEquipped && it.itemId == _itemId }
                    if (!targetOk) {
                        return false
                    }

                    return true
                }
            }
        }

        return false
    }

}