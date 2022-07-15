package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.model.base.DispelSlotType
import l2s.gameserver.stats.Formulas
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Dispel By Category effect implementation.
 * @author DS, Adry_85
 * @author Java-man
 */
class i_dispel_by_category(template: EffectTemplate) : i_abstract_effect(template) {

    private val _slot = DispelSlotType.find(params.getString("i_dispel_by_category_param1"))
    private val _rate = params.getInteger("i_dispel_by_category_param2")
    private val _max = params.getInteger("i_dispel_by_category_param3")

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val targetCreature = target.asCreature() ?: return

        if (targetCreature.isDead) {
            return
        }

        val canceled = Formulas.calcCancelStealEffects(caster, targetCreature, skill, _slot, _rate, _max)
        for (can in canceled) {
            val skill = can.skill
            targetCreature.abnormalList.stop(skill.id, skill.level)
        }
    }

}