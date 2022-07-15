package l2s.gameserver.handler.effects.impl.instant

import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Java-man
 */
class i_get_aggro_of_monster(template: EffectTemplate) : i_abstract_effect(template) {

    /*override fun checkPumpCondition(effector: Creature, effected: Creature): Boolean {
        if (effected.isPlayable)
            return false
        return effected != effector
    }*/

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        target.ai.Attack(caster, false, false)
    }

}