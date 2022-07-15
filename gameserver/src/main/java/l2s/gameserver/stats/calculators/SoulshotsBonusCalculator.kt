package l2s.gameserver.stats.calculators

import l2s.commons.math.constrain
import l2s.gameserver.model.Creature
import l2s.gameserver.stats.DoubleStat

/**
 * @author UnAfraid
 * @author Java-man
 * @since 26.10.2019
 */
object SoulshotsBonusCalculator : StatCalculator {

    override fun calc(
            creature: Creature,
            stat: DoubleStat,
            calculationType: CalculationType,
            initValue: Double?
    ): Double {
        require(initValue == null)

        var baseValue = 1.0

        val power = creature.stat.getValue(DoubleStat.SOULSHOT_POWER) / 100.0

        baseValue += power

        val player = creature.player
        if (player != null) {
            val weapon = player.activeWeaponInstance
            if (weapon != null && weapon.enchantLevel > 0) {
                baseValue += weapon.enchantLevel * 0.7 / 100
            }
        }

        return defaultValue(creature, stat, calculationType, baseValue.constrain(1.0, 1.21))
    }

}