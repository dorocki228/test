package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.templates.skill.EffectTemplate
import l2s.gameserver.utils.ItemFunctions
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Restoration effect implementation.
 * @author Zoey76
 * @author Java-man
 */
class i_restoration(template: EffectTemplate?) : i_abstract_effect(template) {

    private val itemId = params.getInteger("i_restoration_param1")
            .also { require(it > 0) }
    private val itemCount = params.getLong("i_restoration_param2")
            .also { require(it > 0) }

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val casterPlayer = caster.player ?: return

        ItemFunctions.addItem(casterPlayer, itemId, itemCount)
    }

}