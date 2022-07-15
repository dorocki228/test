package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.commons.math.constrain
import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.model.base.ShotType
import l2s.gameserver.stats.Formulas
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Magical Attack effect implementation.
 * @author Adry_85
 * @author Java-man
 */
class i_m_attack_by_dist(template: EffectTemplate) : i_abstract_effect(template) {

    private val _power = params.getDouble("i_m_attack_by_dist_param1")
    private val _debuffModifier = params.getDouble("debuffModifier", 1.0)

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val targetCreature = target.asCreature() ?: return

        if (caster.isAlikeDead) {
            return
        }

        if (targetCreature.isFakeDeath) {
            targetCreature.breakFakeDeath()
        }

        if (targetCreature.isMonster) {
            targetCreature.asMonster().overhitEnabled(true)
        }

        val sps = skill.useSpiritShot() && caster.isChargedShot(ShotType.SPIRITSHOT)
        val bss = skill.useSpiritShot() && caster.isChargedShot(ShotType.BLESSED_SPIRITSHOT)
        val mcrit = Formulas.calcCrit(skill.magicCriticalRate, caster, targetCreature, skill)
        var damage = Formulas.calcMagicDam(
                caster,
                targetCreature,
                skill,
                caster.stat.getMAtk(),
                _power,
                targetCreature.stat.getMDef(),
                sps,
                bss,
                mcrit
        )

        val distance3d = caster.distance3d(targetCreature) / skill.castRange + 0.1
        val distance = distance3d.constrain(0.0, 1.0)

        damage *= 1.0 - distance * 0.7

        // Apply debuff mod
        if (targetCreature.abnormalList.debuffCount > 0) {
            damage *= _debuffModifier
        }

        caster.doAttack(damage, targetCreature, skill, false, false, mcrit, false)
    }
}