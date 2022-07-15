package l2s.gameserver.model.actor.stat

import l2s.gameserver.model.Servitor
import l2s.gameserver.model.base.AttributeType
import l2s.gameserver.model.base.ElementalElement

/**
 * @author Bonux
 */
class ServitorStat(owner: Servitor) : CreatureStat<Servitor>(owner) {

    override fun getAttackElementValue(attackAttribute: AttributeType): Int {
        val owner = owner.player
        return owner?.stat?.getAttackElementValue(attackAttribute) ?: super.getAttackElementValue(attackAttribute)
    }

    override fun getDefenseElementValue(defenseAttribute: AttributeType): Int {
        val owner = owner.player
        return owner?.stat?.getDefenseElementValue(defenseAttribute) ?: super.getDefenseElementValue(defenseAttribute)
    }

    override fun getElementalAttackPower(element: ElementalElement): Double {
        val owner = owner.player
        return owner?.stat?.getElementalAttackPower(element) ?: -1.0
    }

    override fun getElementalDefence(element: ElementalElement): Double {
        val owner = owner.player
        return owner?.stat?.getElementalDefence(element) ?: 0.0
    }

    override fun getElementalCritRate(element: ElementalElement): Double {
        val owner = owner.player
        return owner?.stat?.getElementalCritRate(element) ?: 0.0
    }

    override fun getElementalCritAttack(element: ElementalElement): Double {
        val owner = owner.player
        return owner?.stat?.getElementalCritAttack(element) ?: 0.0
    }

}