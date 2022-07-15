package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.items.ItemInstance
import l2s.gameserver.templates.StatsSet

/**
 * @author Sdw
 * @author Java-man
 */
class op_enchant_range(params: StatsSet) : SkillCondition(params) {

    enum class OpEnchantRangeType {
        NONE, NORMAL, MAGIC
    }

    private val _minEnchant = params.getInteger("op_enchant_range_param1")
    private val _maxEnchant = params.getInteger("op_enchant_range_param2")
    private val _type = params.getEnum(
            "op_enchant_range_param3",
            OpEnchantRangeType::class.java,
            true
    )

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        if (target != null && target.isItem) {
            val item: ItemInstance = target as ItemInstance
            return when (_type) {
                OpEnchantRangeType.NONE -> {
                    item.enchantLevel in _minEnchant.._maxEnchant
                }
                OpEnchantRangeType.NORMAL -> {
                    item.isWeapon && (item.template.isMagicWeapon || item.enchantLevel in _minEnchant.._maxEnchant)
                }
                OpEnchantRangeType.MAGIC -> {
                    item.isWeapon && (!item.template.isMagicWeapon || item.enchantLevel in _minEnchant.._maxEnchant)
                }
            }
        }

        return false
    }

}