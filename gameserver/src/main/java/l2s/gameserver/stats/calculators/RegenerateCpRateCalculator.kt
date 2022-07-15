package l2s.gameserver.stats.calculators

import l2s.gameserver.model.Creature
import l2s.gameserver.model.Player
import l2s.gameserver.model.base.BaseStats
import l2s.gameserver.stats.DoubleStat

/**
 * @author UnAfraid
 * @author Java-man
 * @since 01.10.2019
 */
object RegenerateCpRateCalculator : StatCalculator {
    
    override fun calc(
            creature: Creature,
            stat: DoubleStat,
            calculationType: CalculationType,
            initValue: Double?
    ): Double {
        require(initValue == null)

        if (!creature.isPlayer) {
            return 0.0
        }

        val player = creature.player
        requireNotNull(player)

        var baseValue: Double = when {
            player.isTransformed -> player.transform.getBaseCpReg(player.level)
                .or { player.template.getBaseCpReg(player.level) }.orElse(0.0)
            else -> player.template.getBaseCpReg(player.level).orElse(0.0)
        }

        baseValue *= calcMovementModifier(player)

        baseValue *= creature.levelBonus
        if (creature.con > 0) {
            baseValue *= BaseStats.CON.calcBonus(creature)
        }

        return defaultValue(creature, stat, calculationType, baseValue)
    }

    private fun calcMovementModifier(player: Player): Double {
        return when {
            player.isSitting ->
                1.5 // Sitting
            !player.movement.isMoving ->
                1.1 // Staying
            player.isRunning ->
                0.7 // Running
            else ->
                1.0
        }
    }

}