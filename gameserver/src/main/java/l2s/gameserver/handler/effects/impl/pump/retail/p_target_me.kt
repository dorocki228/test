package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * Target Me effect implementation.
 * @author -Nemesiss-
 * @author Java-man
 */
class p_target_me(template: EffectTemplate) : EffectHandler(template) {

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        if (!target.isPlayable) {
            return
        }

        target.aggressionTarget = caster
        target.target = caster

        target.abortAttack(true, true)

        target.ai.clearNextAction()
    }

    override fun pumpEnd(abnormal: Abnormal?, caster: Creature, target: Creature) {
        if (!target.isPlayable) {
            return
        }

        target.aggressionTarget = null
    }

}