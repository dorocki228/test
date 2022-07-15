package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.instancemanager.ReflectionManager
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.model.base.RestartType
import l2s.gameserver.templates.skill.EffectTemplate
import l2s.gameserver.utils.TeleportUtils
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Escape effect implementation.
 * @author Adry_85
 * @author Java-man
 */
class i_escape(template: EffectTemplate?) : i_abstract_effect(template) {

    private val restartType = RestartType.find(params.getString("i_escape_param1"))

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val player = target.player ?: return

        if (player.cannotEscape()) {
            return
        }

        val closestTownLoc = TeleportUtils.getRestartPoint(player, restartType).loc
        if (closestTownLoc != null) {
            player.teleToLocation(closestTownLoc, ReflectionManager.MAIN)
        }
    }

}