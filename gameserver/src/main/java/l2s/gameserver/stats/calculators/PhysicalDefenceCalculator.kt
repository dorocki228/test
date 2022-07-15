package l2s.gameserver.stats.calculators

import l2s.gameserver.model.Creature
import l2s.gameserver.model.Playable
import l2s.gameserver.model.instances.PetInstance
import l2s.gameserver.network.l2.s2c.updatetype.InventorySlot
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.item.ItemTemplate
import kotlin.math.max

/**
 * @author UnAfraid
 * @author Java-man
 *
 * @since 07.10.2019
 */
object PhysicalDefenceCalculator : StatCalculator {

    private val SLOTS = arrayOf(
        InventorySlot.CHEST,
        InventorySlot.LEGS,
        InventorySlot.HEAD,
        InventorySlot.FEET,
        InventorySlot.GLOVES,
        InventorySlot.PENDANT,
        InventorySlot.CLOAK
    )

    override fun calc(
            creature: Creature,
            stat: DoubleStat,
            calculationType: CalculationType,
            initValue: Double?
    ): Double {
        require(initValue == null)

        var baseValue = when {
            creature.isPlayer -> {
                val player = creature.player
                requireNotNull(player)

                val temp = when {
                    player.isTransformed -> player.transform.basePDef
                        .or { player.template.basePDef }
                    else -> player.template.basePDef
                }

                temp
            }
            creature.isPet -> {
                val pet = creature as PetInstance
                pet.data.getPDef(creature.level)
                    .or { pet.template.basePDef }
            }
            else -> creature.template.basePDef
        }.orElse(0.0)

        baseValue += calcEnchantedItemBonus(creature, stat)

        baseValue += calcInventoryModifier(creature, stat)

        baseValue *= creature.levelBonus

        return getValue(creature, stat, calculationType, baseValue)
    }

    private fun calcInventoryModifier(creature: Creature, stat: DoubleStat): Double {
        if (!creature.isPlayable) {
            return 0.0
        }

        val inventory = (creature as Playable).inventory ?: return 0.0

        var result: Double = inventory.paperdollItems
            .filterNotNull()
            .sumByDouble { it.template.getStat(stat).orElse(0.0) }

        if (!creature.isPlayer) {
            return result
        }

        val player = creature.player

        for (slot in SLOTS) {
            if (inventory.isPaperdollSlotEmpty(slot)) {
                if (slot == InventorySlot.LEGS) {
                    val chest = inventory.getPaperdollItem(InventorySlot.CHEST)
                    if (chest != null
                            && chest.template.bodyPart == ItemTemplate.SLOT_FULL_ARMOR) {
                        continue
                    }
                }

                val template = player.transform ?: player.template
                val baseDef = template.getBaseDefBySlot(slot).orElse(0.0)
                result += baseDef
            }
        }

        return result
    }

    fun getValue(
            creature: Creature,
            stat: DoubleStat,
            calculationType: CalculationType,
            baseValue: Double
    ): Double {
        val passiveMul = max(creature.stat.getPassiveMul(stat), 0.5)
        val passiveAdd = creature.stat.getPassiveAdd(stat)

        return if (calculationType == CalculationType.FULL_VALUE) {
            val mul = max(creature.stat.getMul(stat), 0.5)
            val add = creature.stat.getAdd(stat)
            val moveTypeValue = creature.stat.getMoveTypeValue(stat, creature.moveType)
            (baseValue * passiveMul + passiveAdd) * mul + add + moveTypeValue
        } else {
            baseValue * passiveMul + passiveAdd
        }
    }

}