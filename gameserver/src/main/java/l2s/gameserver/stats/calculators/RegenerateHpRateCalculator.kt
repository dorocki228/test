package l2s.gameserver.stats.calculators

import l2s.gameserver.model.Creature
import l2s.gameserver.model.Player
import l2s.gameserver.model.base.BaseStats
import l2s.gameserver.model.base.ClassLevel
import l2s.gameserver.model.base.ResidenceFunctionType
import l2s.gameserver.model.entity.events.impl.SiegeEvent
import l2s.gameserver.model.entity.events.objects.ZoneObject
import l2s.gameserver.model.entity.residence.Residence
import l2s.gameserver.model.entity.residence.ResidenceFunction
import l2s.gameserver.model.instances.PetInstance
import l2s.gameserver.stats.DoubleStat
import java.util.*

/**
 * @author UnAfraid
 * @author Java-man
 * @since 01.10.2019
 */
object RegenerateHpRateCalculator : StatCalculator {
    
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
                    player.isTransformed -> player.transform.getBaseHpReg(player.level)
                        .or { player.template.getBaseHpReg(player.level) }.orElse(0.0)
                    else -> player.template.getBaseHpReg(player.level).orElse(0.0)
                }

                temp *= calcSiegeFlagRegenModifier(player)

                temp *= calcResidenceModifier(player)
                temp += calcZoneRegenModifier(player)

                temp *= calcMovementModifier(player)

                temp
            }
            creature.isPet -> {
                val pet = creature as PetInstance
                pet.data.getHPRegen(creature.level).orElse(0.0)
            }
            else -> creature.template.getBaseHpReg(creature.level).orElse(0.0)
        }

        baseValue *= creature.levelBonus
        if (creature.con > 0) {
            baseValue *= BaseStats.CON.calcBonus(creature)
        }

        return defaultValue(creature, stat, calculationType, baseValue)
    }

    private fun calcMovementModifier(player: Player): Double {
        return when {
            player.level <= 40 && player.classLevel.ordinal < ClassLevel.SECOND.ordinal ->
                6.0
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

    private fun calcZoneRegenModifier(creature: Creature): Double {
        return creature.zones.sumByDouble { it.regenBonusHP }
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
                val function: ResidenceFunction? = residence.getActiveFunction(ResidenceFunctionType.RESTORE_HP)
                if (function != null) {
                    val value = function.template.hpRegen
                    if (value > 0)
                        return@fold acc * value
                }

                return@fold acc
            }
    }

    private fun calcSiegeFlagRegenModifier(player: Player): Double {
        val siegeEvents = ArrayList<SiegeEvent<*, *>>(1)

        for (siegeEvent in player.getEvents(SiegeEvent::class.java)) {
            val zones: List<ZoneObject> = siegeEvent.getObjects(SiegeEvent.FLAG_ZONES)
            for (zone in zones) {
                if (player.isInZone(zone.zone)) {
                    siegeEvents.add(siegeEvent)
                    break
                }
            }
        }

        for (siegeEvent in siegeEvents) {
            val siegeClan = siegeEvent.getSiegeClan(SiegeEvent.ATTACKERS, player.clan)
            if (siegeClan != null) {
                val flag = siegeClan.flag
                if (flag != null) {
                    if (player.getRealDistance3D(flag) <= 200)
                        return 1.5
                }
            }
        }

        return 1.0
    }

}