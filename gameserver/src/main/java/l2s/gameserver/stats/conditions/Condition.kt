package l2s.gameserver.stats.conditions

import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.items.ItemInstance
import l2s.gameserver.network.l2.components.SystemMsg

abstract class Condition {

    var systemMsg: SystemMsg? = null
        private set

    fun setSystemMsg(msgId: Int) {
        systemMsg = SystemMsg.valueOf(msgId)
    }

    fun test(
        actor: Creature,
        target: Creature?,
        skill: Skill?,
        item: ItemInstance?,
        value: Double
    ): Boolean {
        for (event in actor.events) {
            if (!event.checkCondition(actor, javaClass))
                return false
        }

        return testImpl(actor, target, skill, item, value)
    }

    protected abstract fun testImpl(
        actor: Creature,
        target: Creature?,
        skill: Skill?,
        item: ItemInstance?,
        value: Double
    ): Boolean

    open fun init() {
        // Для заглушки отображения кондишона требующий эффект у цели.
    }

    companion object {
        val EMPTY_ARRAY = arrayOfNulls<Condition>(0)
    }
    
}
