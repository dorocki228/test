package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * Passive effect implementation.
 * TODO should work like this ?
 * @author Adry_85
 * @author Java-man
 */
class p_passive(template: EffectTemplate) : EffectHandler(template) {

    override fun checkPumpCondition(abnormal: Abnormal?, caster: Creature, target: Creature): Boolean {
        return target.isMonster
    }

    override fun pumpStart(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.stopActions()
        target.flags.disableAllSkills()
        target.flags.immobilized.start()
    }

    override fun pumpEnd(abnormal: Abnormal?, caster: Creature, target: Creature) {
        target.flags.enableAllSkills()
        target.flags.immobilized.stop()
    }

}