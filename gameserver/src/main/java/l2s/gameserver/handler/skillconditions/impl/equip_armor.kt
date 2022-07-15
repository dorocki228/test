package l2s.gameserver.handler.skillconditions.impl

import l2s.gameserver.handler.skillconditions.SkillCondition
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.items.Inventory
import l2s.gameserver.network.l2.s2c.updatetype.InventorySlot
import l2s.gameserver.templates.StatsSet
import l2s.gameserver.templates.item.ArmorTemplate.ArmorType

/**
 * @author Java-man
 */
class equip_armor(params: StatsSet) : SkillCondition(params) {

    private val armorTypes: List<ArmorType>

    init {
        val conditions = params.getString("equip_armor_param1")
        armorTypes = conditions.split(";").map {
            runCatching<ArmorType> {
                val name = it.replace("armor_", "")
                ArmorType.valueOf(name.toUpperCase())
            }
                    .onFailure { error("Can't find item type value: $it") }
                    .getOrThrow()
        }

        require(armorTypes.isNotEmpty())
    }

    override fun canUse(caster: Creature, skill: Skill, target: Creature?): Boolean {
        if (!caster.isPlayer) {
            return true
        }

        val inv: Inventory = caster.player.inventory

        for (type in armorTypes) {
            when (type) {
                ArmorType.NONE -> {
                    val chest = inv.getPaperdollItem(InventorySlot.CHEST)
                    if (chest == null) {
                        return true
                    }
                }
                ArmorType.LIGHT, ArmorType.HEAVY, ArmorType.MAGIC -> {
                    val chest = inv.getPaperdollItem(InventorySlot.CHEST)
                    if (chest != null) {
                        if (type.mask() and chest.template.itemMask != 0L) {
                            return true
                        }
                    }
                }
                ArmorType.SIGIL -> {
                    val item = caster.secondaryWeaponInstance
                    if (item != null && item.itemType == ArmorType.SIGIL) {
                        return true
                    }
                }
            }
        }

        return false
    }

}