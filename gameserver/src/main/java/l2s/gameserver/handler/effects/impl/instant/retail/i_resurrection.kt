package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.listener.actor.player.impl.ReviveAnswerListener
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.model.instances.PetInstance
import l2s.gameserver.stats.Formulas
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Resurrection effect implementation.
 * @author Adry_85
 * @author Java-man
 */
class i_resurrection(template: EffectTemplate) : i_abstract_effect(template) {

    private val power = params.getDouble("i_resurrection_param1")

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        if (!caster.isPlayer) {
            return
        }

        val targetPlayer = target.player ?: return

        val percent = Formulas.calculateSkillResurrectRestorePercent(power, caster)

        if (target.isPet) {
            if (targetPlayer == caster)
                (target as PetInstance).doRevive(percent)
            else
                targetPlayer.reviveRequest(caster.player, percent, true)
        } else if (target.isPlayer) {
            val ask = targetPlayer.getAskListener(false)
            val reviveAsk = if (ask != null && ask.value is ReviveAnswerListener) ask.value else null
            if (reviveAsk != null) {
                return
            }

            targetPlayer.reviveRequest(caster.player, percent, false)
        }
    }

}
