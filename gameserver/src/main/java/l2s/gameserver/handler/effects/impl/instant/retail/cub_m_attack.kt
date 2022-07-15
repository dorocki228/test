package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.stats.Formulas
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Cubic Magical Attack effect implementation.
 * @author Adry_85
 * @author Java-man
 */
class cub_m_attack(template: EffectTemplate) : i_abstract_effect(template) {

    private val _power = params.getDouble("cub_m_attack_param1")
    private val _debuffModifier = params.getDouble("debuffModifier", 1.0)

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val targetCreature = target.asCreature() ?: return

        if (caster.isAlikeDead) {
            return
        }

        if (targetCreature.isFakeDeath) {
            targetCreature.breakFakeDeath()
        }

        val mcrit = Formulas.calcCrit(skill.magicCriticalRate, caster, targetCreature, skill)
        val shld: Byte = Formulas.calcShldUse(caster, target)
        var damage = Formulas.calcMagicDam(
                cubic,
                targetCreature,
                skill,
                _power,
                targetCreature.stat.getMDef(),
                mcrit,
                shld
        )

        // Apply debuff mod
        if (targetCreature.abnormalList.debuffCount > 0) {
            damage *= _debuffModifier
        }

        caster.doAttack(damage, targetCreature, skill, false, false, mcrit, false)
    }
}