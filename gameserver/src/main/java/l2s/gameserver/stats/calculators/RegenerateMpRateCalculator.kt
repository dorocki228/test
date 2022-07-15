package l2s.gameserver.stats.calculators

import l2s.gameserver.model.Creature
import l2s.gameserver.model.Player
import l2s.gameserver.model.base.BaseStats
import l2s.gameserver.model.base.ResidenceFunctionType
import l2s.gameserver.model.entity.residence.Residence
import l2s.gameserver.model.entity.residence.ResidenceFunction
import l2s.gameserver.model.instances.PetInstance
import l2s.gameserver.stats.DoubleStat

/**
 * @author UnAfraid
 * @author Java-man
 * @since 01.10.2019
 */
object RegenerateMpRateCalculator : StatCalculator {
    
    override fun calc(
            creature: Creature,
            stat: DoubleStat,
            calculationType: CalculationType,
            initValue: Double?
    ): Double {
        require(initValue == null)

        var baseValue: Double = when {
            creature.isPlayer -> {
                val player = creature.player
                requireNotNull(player)

                var temp = when {
                    player.isTransformed -> player.transform.getBaseMpReg(player.level)
                        .or { player.template.getBaseMpReg(player.level) }.orElse(0.0)
                    else -> player.template.getBaseMpReg(player.level).orElse(0.0)
                }

                temp = creature.stat.getValue(DoubleStat.MP_REGEN_ADD, temp)

                temp *= calcResidenceModifier(player)
                temp += calcZoneRegenModifier(player)

                temp *= calcMovementModifier(player)

                temp
            }
            creature.isPet -> {
                val pet = creature as PetInstance
                pet.data.getMPRegen(creature.level)
                    .map { creature.stat.getValue(DoubleStat.MP_REGEN_ADD, it) }
                    .orElse(0.0)
            }
            else -> {
                creature.template.getBaseMpReg(creature.level)
                    .map { creature.stat.getValue(DoubleStat.MP_REGEN_ADD, it) }
                    .orElse(0.0)
            }
        }

        baseValue *= creature.levelBonus
        if (creature.men > 0) {
            baseValue *= BaseStats.MEN.calcBonus(creature)
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

    private fun calcZoneRegenModifier(player: Player): Double {
        return player.zones.sumByDouble { it.regenBonusMP }
    }

    private fun calcResidenceModifier(player: Player): Double {
        val clanId = player.clanId
        if (clanId == 0) {
            return 1.0
        }

        return player.zones.asSequence()
            .mapNotNull { it.params["residence"] as? Residence }
            .filter { it.isOwner(clanId) }
            .fold(1.0) { acc, residence ->
                val function: ResidenceFunction? = residence.getActiveFunction(ResidenceFunctionType.RESTORE_MP)
                if (function != null) {
                    val value = function.template.mpRegen
                    if (value > 0)
                        return@fold acc * value
                }

                return@fold acc
            }
    }

}