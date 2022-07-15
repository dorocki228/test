package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.ai.CtrlIntention
import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Get Agro effect implementation.
 * @author Adry_85
 */
class i_get_agro(template: EffectTemplate) : i_abstract_effect(template) {

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        if (target.isMonster) {
            target.ai.setIntention(CtrlIntention.AI_INTENTION_ATTACK, caster)
        }
    }

}