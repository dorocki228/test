package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * Talisman Slot effect implementation.
 * @author Adry_85
 * @author Java-man
 */
class p_expand_deco_slot(template: EffectTemplate) : EffectHandler(template) {

    private val slots = params.getInteger("p_expand_deco_slot_param1")

    override fun checkPumpCondition(abnormal: Abnormal?, caster: Creature, target: Creature): Boolean {
        return target.isPlayer
    }

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.player.stat.addTalismanSlots(slots)
    }

    override fun pumpEnd(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.player.stat.addTalismanSlots(-slots)
    }

}