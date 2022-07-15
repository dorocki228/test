package l2s.gameserver.stats.calculators

import l2s.gameserver.model.Creature
import l2s.gameserver.model.Playable
import l2s.gameserver.model.base.BaseStats
import l2s.gameserver.model.instances.PetInstance
import l2s.gameserver.network.l2.s2c.updatetype.InventorySlot
import l2s.gameserver.stats.DoubleStat
import kotlin.math.max

/**
 * @author UnAfraid
 * @author Java-man
 *
 * @since 07.10.2019
 */
object MagicalDefenceCalculator : StatCalculator {

    private val SLOTS = arrayOf(
        InventorySlot.LFINGER,
        InventorySlot.RFINGER,
        InventorySlot.LEAR,
        InventorySlot.REAR,
        InventorySlot.NECK
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
                val player = requireNotNull(creature.player)

                when {
                    player.isTransformed -> player.transform.baseMDef
                        .or { player.template.baseMDef }
                    else -> player.template.baseMDef
                }
            }
            creature.isPet -> {
                val pet = creature as PetInstance
                pet.data.getMDef(creature.level)
                    .or { pet.template.baseMDef }
                    .map {
                        if (!pet.inventory.isPaperdollSlotEmpty(InventorySlot.NECK)) {
                            return@map it - 13
                        }
                        return@map it
                    }
            }
            else -> creature.template.baseMDef
        }.orElse(0.0)

        baseValue += calcEnchantedItemBonus(creature, stat)

        baseValue += calcInventoryModifier(creature, stat)

        baseValue *= BaseStats.MEN.calcBonus(creature)

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
            val template = player.transform ?: player.template
            val baseDef = template.getBaseDefBySlot(slot).orElse(0.0)
            if (inventory.isPaperdollSlotEmpty(slot)) {
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