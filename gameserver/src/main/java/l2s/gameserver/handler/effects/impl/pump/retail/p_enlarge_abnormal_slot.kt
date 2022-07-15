package l2s.gameserver.handler.effects.impl.pump.retail

import l2s.gameserver.handler.effects.EffectHandler
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.skills.SkillEntry
import l2s.gameserver.templates.skill.EffectTemplate

/**
 * Enlarge Abnormal Slot effect implementation.
 * @author Zoey76
 * @author Java-man
 *
 * @since 20.10.2019
 */
class p_enlarge_abnormal_slot(template: EffectTemplate) : EffectHandler(template) {

    private val slots = params.getInteger("p_enlarge_abnormal_slot_param1")

    override fun checkPumpCondition(abnormal: Abnormal?, caster: Creature, target: Creature): Boolean {
        return target.isPlayer
    }

    override fun pump(
            target: Creature,
            skillEntry: SkillEntry?
    ) {
        target.stat.mergeMaxBuffCount(slots)
    }

}