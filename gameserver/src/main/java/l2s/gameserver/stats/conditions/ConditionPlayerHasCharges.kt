package l2s.gameserver.stats.conditions

import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.items.ItemInstance

class ConditionPlayerHasCharges(private val chargesNumber: Int) : Condition() {

    override fun testImpl(
        actor: Creature,
        target: Creature?,
        skill: Skill?,
        item: ItemInstance?,
        value: Double
    ): Boolean {
        return actor.charges >= chargesNumber
    }
    
}