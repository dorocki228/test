package l2s.gameserver.model.actor.stat

import l2s.gameserver.model.Player
import l2s.gameserver.model.base.ElementalElement
import l2s.gameserver.skills.AbnormalType
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.templates.item.ItemGrade
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Bonux
 */
class PlayerStat(owner: Player) : CreatureStat<Player>(owner) {

    /** Player's maximum talisman count.  */
    private val talismanSlots = AtomicInteger()

    //the maximum dwarven recipe level this character can craft.
    var createItemLevel = 0

    var crystallizeGrade: ItemGrade = ItemGrade.NONE

    override fun onRecalculateStats(broadcast: Boolean) {
        super.onRecalculateStats(broadcast)

        if (owner.hasServitor() && owner.abnormalList.contains(AbnormalType.ABILITY_CHANGE)) {
            owner.servitors.forEach {
                it.stat.recalculateStats(broadcast)
            }
        }
    }

    /**
     * Gets the maximum talisman count.
     * @return the maximum talisman count
     */
    fun getTalismanSlots(): Int {
        return talismanSlots.get()
    }

    fun addTalismanSlots(count: Int) {
        talismanSlots.addAndGet(count)
    }

    override fun getElementalAttackPower(element: ElementalElement): Double {
        val elemental = owner.elementalList.get(element) ?: return -1.0

        var attack: Double = elemental.levelData.attack.toDouble()
        attack += elemental.attackPoints * 5

        val stat: DoubleStat? = when (element) {
            ElementalElement.FIRE -> DoubleStat.FIRE_ELEMENTAL_ATTACK
            ElementalElement.WATER -> DoubleStat.WATER_ELEMENTAL_ATTACK
            ElementalElement.WIND -> DoubleStat.WIND_ELEMENTAL_ATTACK
            ElementalElement.EARTH -> DoubleStat.EARTH_ELEMENTAL_ATTACK
            else -> null
        }
        if (stat != null) {
            attack = getValue(stat, attack)
        }

        return attack
    }

    override fun getElementalDefence(element: ElementalElement): Double {
        val elemental = owner.elementalList.get(element) ?: return 0.0

        var defence: Double = elemental.levelData.defence.toDouble()
        defence += elemental.defencePoints * 5

        val stat: DoubleStat? = when (element) {
            ElementalElement.FIRE -> DoubleStat.FIRE_ELEMENTAL_DEFENCE
            ElementalElement.WATER -> DoubleStat.WATER_ELEMENTAL_DEFENCE
            ElementalElement.WIND -> DoubleStat.WIND_ELEMENTAL_DEFENCE
            ElementalElement.EARTH -> DoubleStat.EARTH_ELEMENTAL_DEFENCE
            else -> null
        }
        if (stat != null) {
            defence = getValue(stat, defence)
        }

        return defence
    }

    override fun getElementalCritRate(element: ElementalElement): Double {
        val elemental = owner.elementalList.get(element) ?: return 0.0

        var critRate: Double = elemental.levelData.critRate.toDouble()
        critRate += elemental.critRatePoints

        val stat: DoubleStat? = when (element) {
            ElementalElement.FIRE -> DoubleStat.FIRE_ELEMENTAL_CRIT_RATE
            ElementalElement.WATER -> DoubleStat.WATER_ELEMENTAL_CRIT_RATE
            ElementalElement.WIND -> DoubleStat.WIND_ELEMENTAL_CRIT_RATE
            ElementalElement.EARTH -> DoubleStat.EARTH_ELEMENTAL_CRIT_RATE
            else -> null
        }
        if (stat != null) {
            critRate = getValue(stat, critRate)
        }

        return critRate
    }

    override fun getElementalCritAttack(element: ElementalElement): Double {
        val elemental = owner.elementalList.get(element) ?: return 0.0

        var critAttack: Double = elemental.levelData.critAttack.toDouble()
        critAttack += elemental.critAttackPoints

        val stat: DoubleStat? = when (element) {
            ElementalElement.FIRE -> DoubleStat.FIRE_ELEMENTAL_CRIT_ATTACK
            ElementalElement.WATER -> DoubleStat.WATER_ELEMENTAL_CRIT_ATTACK
            ElementalElement.WIND -> DoubleStat.WIND_ELEMENTAL_CRIT_ATTACK
            ElementalElement.EARTH -> DoubleStat.EARTH_ELEMENTAL_CRIT_ATTACK
            else -> null
        }
        if (stat != null) {
            critAttack = getValue(stat, critAttack)
        }

        return critAttack
    }

}