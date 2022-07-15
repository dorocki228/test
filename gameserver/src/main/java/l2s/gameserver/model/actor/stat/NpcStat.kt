package l2s.gameserver.model.actor.stat

import l2s.gameserver.model.base.ElementalElement
import l2s.gameserver.model.instances.NpcInstance

/**
 * @author Bonux
 */
class NpcStat(owner: NpcInstance) : CreatureStat<NpcInstance>(owner) {

    override fun getElementalAttackPower(element: ElementalElement): Double {
        //TODO: Realese.
        return -1.0
    }

    override fun getElementalDefence(element: ElementalElement): Double {
        //TODO: Realese.
        return 0.0
    }

    override fun getElementalCritRate(element: ElementalElement): Double {
        //TODO: Realese.
        return 0.0
    }

    override fun getElementalCritAttack(element: ElementalElement): Double {
        //TODO: Realese.
        return 0.0
    }
}