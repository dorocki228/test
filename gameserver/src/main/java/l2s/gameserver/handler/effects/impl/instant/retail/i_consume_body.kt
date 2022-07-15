package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Consume Body effect implementation.
 * @author Zoey76
 * @author Java-man
 */
class i_consume_body(template: EffectTemplate?) : i_abstract_effect(template) {

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val targetNpc = target.asNpc() ?: return

        if (!targetNpc.isDead) {
            return
        }

        targetNpc.endDecayTask()
    }

}