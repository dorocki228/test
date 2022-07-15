package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Give SP effect implementation.
 * @author Adry_85
 * @author Java-man
 */
class i_sp(template: EffectTemplate) : i_abstract_effect(template) {

    private val sp = params.getLong("i_sp_param1")

    override fun instantUse(
            caster: Creature,
            target: Creature,
            soulShotUsed: AtomicBoolean,
            reflected: Boolean,
            cubic: Cubic
    ) {
        if (!caster.isPlayer || caster.isAlikeDead) {
            return
        }

        caster.addExpAndSp(0, sp)
    }

}