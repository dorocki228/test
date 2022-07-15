package l2s.gameserver.stats.calculators

import l2s.commons.math.MathUtils
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Playable
import l2s.gameserver.model.instances.PetInstance
import l2s.gameserver.model.items.Inventory
import l2s.gameserver.model.items.ItemInstance
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.item.ItemGrade
import l2s.gameserver.templates.item.ItemQuality
import l2s.gameserver.templates.item.ItemTemplate
import l2s.gameserver.templates.item.isRanged
import java.util.function.Predicate
import kotlin.math.max

/**
 * @author UnAfraid
 * @author Java-man
 *
 * @since 01.10.2019
 */
interface StatCalculator {

    fun calc(
            creature: Creature,
            stat: DoubleStat,
            calculationType: CalculationType,
            initValue: Double?
    ): Double

    fun defaultValue(
            creature: Creature,
            stat: DoubleStat,
            calculationType: CalculationType,
            baseValue: Double?
    ): Double {
        return calculationType.calculate(creature, stat, baseValue)
    }

    fun validateValue(creature: Creature, value: Double, minValue: Double, maxValue: Double): Double {
        /* TODO
        if (value > maxValue && !creature.canOverrideCond(PcCondOverride.MAX_STATS_VALUE)) {
            return maxValue
        }*/

        return MathUtils.constrain(value, minValue, maxValue)
    }

    fun calcEnchantBodyPart(creature: Creature, vararg slots: Long): Double {
        if (!creature.isPlayer) {
            return 0.0
        }

        val player = requireNotNull(creature.player)

        var value = 0.0
        for (slot in slots) {
            val index = Inventory.getPaperdollIndex(slot)
            val item = player.inventory.getPaperdollItem(index)
            if (item != null && item.enchantLevel >= 4 && item.template.grade == ItemGrade.R) {
                value += calcEnchantBodyPartBonus(item.enchantLevel, item.template.quality == ItemQuality.BLESSED)
            }
        }
        return value
    }

    fun calcEnchantBodyPartBonus(enchantLevel: Int, isBlessed: Boolean): Double {
        throw UnsupportedOperationException()
    }

    fun calcWeaponBaseValue(creature: Creature, stat: DoubleStat): Double {
        return when {
            creature.isPlayer -> {
                val player = requireNotNull(creature.player)
                when {
                    player.isMounted -> {
                        when (stat) {
                            DoubleStat.PHYSICAL_ATTACK -> player.mount.pAtkOnRide
                            DoubleStat.MAGICAL_ATTACK -> player.mount.mAtkOnRide
                            DoubleStat.PHYSICAL_ATTACK_SPEED -> {
                                // Если маунт голоден на 50% и более, скорость атаки урезается в 2х раза.
                                if (player.mount.isHungry) {
                                    player.mount.atkSpdOnRide * 0.5
                                } else {
                                    player.mount.atkSpdOnRide
                                }
                            }
                            else -> 0.0
                        }
                    }
                    player.isTransformed -> player.transform.getBaseValue(stat)
                        .or { player.getWeaponStat(stat) }
                        .or { player.template.getBaseValue(stat) }
                        .orElse(0.0)
                    else -> {
                        player.getWeaponStat(stat)
                            .or { player.template.getBaseValue(stat) }
                            .orElse(0.0)
                    }
                }
            }
            creature.isPet -> {
                val pet = creature as PetInstance
                val baseVal =
                    when (stat) {
                        DoubleStat.PHYSICAL_ATTACK -> pet.data.getPAtk(pet.level)
                            .or { pet.template.getBaseValue(stat) }
                        DoubleStat.MAGICAL_ATTACK -> pet.data.getMAtk(pet.level)
                            .or { pet.template.getBaseValue(stat) }
                        else -> pet.template.getBaseValue(stat)
                    }
                val weaponStat = pet.getWeaponStat(stat)
                baseVal.orElse(0.0) + weaponStat.orElse(0.0)
            }
            else -> creature.template.getBaseValue(stat).orElse(0.0)
        }
    }

    fun calcEquippedItemsBaseValue(creature: Creature, stat: DoubleStat): Double {
        val baseValue =
            if (creature.isTransformed) {
                creature.transform.getBaseValue(stat)
                    .or { creature.template.getBaseValue(stat) }
                    .orElse(0.0)
            } else {
                creature.template.getBaseValue(stat).orElse(0.0)
            }

        if (creature.isPlayable) {
            val inv = (creature as Playable).inventory
            val bonus = when {
                inv != null -> inv.getPaperdollItems(Predicate { it.isEquipped })
                    .sumByDouble { it.template.getStat(stat).orElse(0.0) }
                else -> 0.0
            }
            return baseValue + bonus
        }

        return baseValue
    }

    fun calcEnchantedItemBonus(creature: Creature, stat: DoubleStat): Double {
        if (!creature.isPlayer) {
            return 0.0
        }

        val player = creature.player

        var value = 0.0
        player.inventory.getPaperdollItems(Predicate { it.isEquipped && it.enchantLevel > 0 })
            .forEach { item ->
                if (item.template.getStat(stat).isEmpty) {
                    return@forEach
                }

                val blessedBonus = if (item.template.quality == ItemQuality.BLESSED) 1.5 else 1.0
                var enchant = item.enchantLevel

                /* TODO
                if (player.isInOlympiadMode() && OlympiadConfig.ALT_OLY_ENCHANT_LIMIT >= 0 && enchant > OlympiadConfig.ALT_OLY_ENCHANT_LIMIT) {
                enchant = OlympiadConfig.ALT_OLY_ENCHANT_LIMIT
                  }*/

                when {
                    stat == DoubleStat.MAGICAL_DEFENCE || stat == DoubleStat.PHYSICAL_DEFENCE ->
                        value += calcEnchantDefBonus(item, blessedBonus, enchant)
                    stat == DoubleStat.MAGICAL_ATTACK ->
                        value += calcEnchantMatkBonus(item, blessedBonus, enchant)
                    stat == DoubleStat.PHYSICAL_ATTACK && item.isWeapon ->
                        value += calcEnchantedPAtkBonus(item, blessedBonus, enchant)
                }
            }

        return value
    }

    /**
     * @param item
     * @param blessedBonus
     * @param enchant
     * @return
     */
    fun calcEnchantDefBonus(item: ItemInstance, blessedBonus: Double, enchant: Int): Double {
        return when (item.template.grade) {
            ItemGrade.R -> {
                2.0 * blessedBonus * enchant + 6.0 * blessedBonus * max(0, enchant - 3)
            }
            else -> {
                (enchant + 3 * max(0, enchant - 3)).toDouble()
            }
        }
    }

    /**
     * @param item
     * @param blessedBonus
     * @param enchant
     * @return
     */
    fun calcEnchantMatkBonus(item: ItemInstance, blessedBonus: Double, enchant: Int): Double {
        when (item.template.grade) {
            ItemGrade.R -> {
                return 5.0 * blessedBonus * enchant + 10.0 * blessedBonus * max(0, enchant - 3)
            }
            ItemGrade.S -> {
                // M. Atk. increases by 4 for all weapons.
                // Starting at +4, M. Atk. bonus double.
                return (4 * enchant + 8 * max(0, enchant - 3)).toDouble()
            }
            ItemGrade.A, ItemGrade.B, ItemGrade.C -> {
                // M. Atk. increases by 3 for all weapons.
                // Starting at +4, M. Atk. bonus double.
                return (3 * enchant + 6 * max(0, enchant - 3)).toDouble()
            }
            else -> {
                // M. Atk. increases by 2 for all weapons. Starting at +4, M. Atk. bonus double.
                // Starting at +4, M. Atk. bonus double.
                return (2 * enchant + 4 * max(0, enchant - 3)).toDouble()
            }
        }
    }

    /**
     * @param item
     * @param blessedBonus
     * @param enchant
     * @return
     */
    fun calcEnchantedPAtkBonus(item: ItemInstance, blessedBonus: Double, enchant: Int): Double {
        when (item.template.grade) {
            ItemGrade.R -> {
                return if (item.template.bodyPart == ItemTemplate.SLOT_LR_HAND) {
                    if (item.template.itemType.isRanged()) {
                        12.0 * blessedBonus * enchant.toDouble()
                        +24.0 * blessedBonus * max(0, enchant - 3)
                    } else {
                        7.0 * blessedBonus * enchant.toDouble()
                        +14.0 * blessedBonus * max(0, enchant - 3)
                    }
                } else {
                    6.0 * blessedBonus * enchant.toDouble()
                    +12.0 * blessedBonus * max(0, enchant - 3)
                }
            }
            ItemGrade.S -> {
                return if (item.template.bodyPart == ItemTemplate.SLOT_LR_HAND) {
                    if (item.template.itemType.isRanged()) {
                        // P. Atk. increases by 10 for bows.
                        // Starting at +4, P. Atk. bonus double.
                        (10 * enchant + 20 * max(0, enchant - 3)).toDouble()
                    } else {
                        // P. Atk. increases by 6 for two-handed swords, two-handed blunts, dualswords, and two-handed combat weapons.
                        // Starting at +4, P. Atk. bonus double.
                        (6 * enchant + 12 * max(0, enchant - 3)).toDouble()
                    }
                } else {
                    // P. Atk. increases by 5 for one-handed swords, one-handed blunts, daggers, spears, and other weapons.
                    // Starting at +4, P. Atk. bonus double.
                    (5 * enchant + 10 * max(0, enchant - 3)).toDouble()
                }
            }
            ItemGrade.A -> {
                return if (item.template.bodyPart == ItemTemplate.SLOT_LR_HAND) {
                    if (item.template.itemType.isRanged()) {
                        // P. Atk. increases by 8 for bows.
                        // Starting at +4, P. Atk. bonus double.
                        (8 * enchant + 16 * max(0, enchant - 3)).toDouble()
                    } else {
                        // P. Atk. increases by 5 for two-handed swords, two-handed blunts, dualswords, and two-handed combat weapons.
                        // Starting at +4, P. Atk. bonus double.
                        (5 * enchant + 10 * max(0, enchant - 3)).toDouble()
                    }
                } else {
                    // P. Atk. increases by 4 for one-handed swords, one-handed blunts, daggers, spears, and other weapons.
                    // Starting at +4, P. Atk. bonus double.
                    (4 * enchant + 8 * max(0, enchant - 3)).toDouble()
                }
            }
            ItemGrade.B, ItemGrade.C -> {
                return if (item.template.bodyPart == ItemTemplate.SLOT_LR_HAND) {
                    if (item.template.itemType.isRanged()) {
                        // P. Atk. increases by 6 for bows.
                        // Starting at +4, P. Atk. bonus double.
                        (6 * enchant + 12 * max(0, enchant - 3)).toDouble()
                    } else {
                        // P. Atk. increases by 4 for two-handed swords, two-handed blunts, dualswords, and two-handed combat weapons.
                        // Starting at +4, P. Atk. bonus double.
                        (4 * enchant + 8 * max(0, enchant - 3)).toDouble()
                    }
                } else {
                    // P. Atk. increases by 3 for one-handed swords, one-handed blunts, daggers, spears, and other weapons.
                    // Starting at +4, P. Atk. bonus double.
                    (3 * enchant + 6 * max(0, enchant - 3)).toDouble()
                }
            }
            else -> {
                return if (item.template.itemType.isRanged()) {
                    // Bows increase by 4.
                    // Starting at +4, P. Atk. bonus double.
                    (4 * enchant + 8 * max(0, enchant - 3)).toDouble()
                } else {
                    // P. Atk. increases by 2 for all weapons with the exception of bows.
                    // Starting at +4, P. Atk. bonus double.
                    (2 * enchant + 4 * max(0, enchant - 3)).toDouble()
                }
            }
        }
    }

}