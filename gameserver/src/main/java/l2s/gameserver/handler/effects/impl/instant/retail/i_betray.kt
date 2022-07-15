package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Betray effect implementation.
 *
 * @author Java-man
 */
class i_betray(template: EffectTemplate?) : i_abstract_effect(template) {

    // TODO use this values
    private val unk1 = params.getInteger("i_betray_param1")
    private val unk2 = params.getInteger("i_betray_param2")

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val targetSummon = target.asServitor() ?: return

        val owner = targetSummon.player ?: return
        targetSummon.ai.Attack(owner, true, false)
    }

}