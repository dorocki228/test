package l2s.gameserver.stats.calculators

import l2s.gameserver.model.Creature
import l2s.gameserver.model.Playable
import l2s.gameserver.model.base.AttributeType
import l2s.gameserver.model.base.ClassType2
import l2s.gameserver.model.items.ItemInstance
import l2s.gameserver.stats.DoubleStat
import java.util.function.Predicate

/**
 * @author UnAfraid
 * @author Java-man
 *
 * @since 13.10.2019
 */
class AttributeCalculator(
        private val attributeType: AttributeType,
        private val isWeapon: Boolean
) : StatCalculator {

    override fun calc(
            creature: Creature,
            stat: DoubleStat,
            calculationType: CalculationType,
            initValue: Double?
    ): Double {
        require(initValue == null)

        // FIXME attributeType field don't work for some reason
        val element = AttributeType.getElementByStat(stat)
        var baseValue: Double = getBaseValue(creature, stat, element)

        if (creature.isPlayable) {
            if (isWeapon) {
                val weapon: ItemInstance? = creature.activeWeaponInstance
                if (weapon != null) {
                    baseValue += weapon.getAttributeElementValue(element, true)
                }
            } else {
                val playable = creature as Playable
                val inventory = playable.inventory
                if (inventory != null) {
                    baseValue += inventory.getPaperdollItems(Predicate { it.isArmor })
                        .sumBy { it.getAttributeElementValue(element, true) }
                }
            }
        }

        return defaultValue(creature, stat, calculationType, baseValue)
    }

    private fun getBaseValue(
            creature: Creature,
            stat: DoubleStat,
            attributeType: AttributeType
    ): Double {
        when {
            isWeapon -> {
                if (creature.isSummon) {
                    val player = creature.player
                    if (player.classId.type2 == ClassType2.SUMMONER) {
                        return player.stat.getValue(stat, 0.0)
                    }
                }

                return creature.template.baseAttributeAttack[attributeType.id].toDouble()
            }
            else -> {
                if (creature.isSummon) {
                    val player = creature.player
                    if (player.classId.type2 == ClassType2.SUMMONER) {
                        return player.stat.getValue(stat, 0.0)
                    }
                }

                return creature.template.baseAttributeDefence[attributeType.id].toDouble()
            }
        }
    }

}