package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.impl.AbstractBooleanStatEffect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.stats.BooleanStat
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * An effect that blocks the player (NPC?) control. <br>
 * It prevents moving, casting, social actions, etc.
 * @author Nik
 * @author Java-man
 */
class p_block_controll(template: EffectTemplate) :
        AbstractBooleanStatEffect(template, BooleanStat.BLOCK_CONTROL) {

    override fun checkPumpCondition(abnormal: Abnormal?, caster: Creature, target: Creature): Boolean {
        if (target.isFearImmune) {
            return false
        }

        // Fear нельзя наложить на осадных саммонов
        val npc = target.asNpc()
        if (npc != null && npc.template.race == 21) {
            return false
        }

        /* need ?
        if (target.isInPeaceZone) {
            return false
        }*/

        return true
    }

}