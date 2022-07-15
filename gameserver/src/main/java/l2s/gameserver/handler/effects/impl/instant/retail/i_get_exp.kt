package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Get EXP effect implementation.
 * @author Kazumi
 */
class i_get_exp(template: EffectTemplate) : i_abstract_effect(template) {

    private val _exp = params.getLong("exp")
    private val _minLevel = params.getInteger("minLevel", 0)
    private val _underMinLevelExpPercent = params.getInteger("underMinLevelExpPercent", 100)

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val casterPlayer = caster.player ?: return

        if (casterPlayer.isAlikeDead) {
            return
        }

        if (casterPlayer.level >= _minLevel) {
            casterPlayer.addExpAndSp(_exp, 0)
        } else {
            val exp = _exp * _underMinLevelExpPercent / 100L
            casterPlayer.addExpAndSp(exp, 0)
        }
    }
}
