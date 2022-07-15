package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Unsummon Agathion effect implementation.
 * @author Zoey76
 * @author Bonux
 * @author Java-man
 */
class i_unsummon_agathion(template: EffectTemplate) : i_abstract_effect(template) {

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val player = caster.player ?: return
        player.deleteAgathion()
    }

}