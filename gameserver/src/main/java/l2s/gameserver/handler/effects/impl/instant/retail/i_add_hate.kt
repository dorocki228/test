package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Add Hate effect implementation.
 *
 * @author Adry_85
 * @author Java-man
 */
class i_add_hate(template: EffectTemplate?) : i_abstract_effect(template) {

    private val _power = params.getInteger("i_add_hate_param1")

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val targetAttackable = target.asMonster() ?: return

        val value = _power
        if (value > 0) {
            targetAttackable.aggroList.addDamageHate(caster, 0, value)
        } else if (value < 0) {
            targetAttackable.aggroList.reduceHate(caster, -value)
        }
    }

}