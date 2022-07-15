package l2s.gameserver.handler.effects.impl.instant.retail

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
class i_m_attack_range(template: EffectTemplate) : i_abstract_effect(template) {

    private val _power = params.getDouble("i_m_attack_range_param1")
    private val _shieldDefPercent = params.getDouble("i_m_attack_range_param2")

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val targetCreature = target.asCreature() ?: return

        if (targetCreature.isFakeDeath) {
            targetCreature.breakFakeDeath()
        }

        var mDef = targetCreature.stat.getMDef()
        when (Formulas.calcShldUse(caster, targetCreature)) {
            Formulas.SHIELD_DEFENSE_SUCCEED -> {
                mDef += targetCreature.shldDef * _shieldDefPercent / 100.0
            }
            Formulas.SHIELD_DEFENSE_PERFECT_BLOCK -> {
                mDef = -1.0
            }
        }

        var damage = 1.0
        val mcrit = Formulas.calcCrit(skill.magicCriticalRate, caster, targetCreature, skill)
        if (mDef != -1.0) {
            val sps = skill.useSpiritShot() && caster.isChargedShot(ShotType.SPIRITSHOT)
            val bss = skill.useSpiritShot() && caster.isChargedShot(ShotType.BLESSED_SPIRITSHOT)
            damage = Formulas.calcMagicDam(caster, targetCreature, skill, caster.stat.getMAtk(), _power, mDef, sps, bss, mcrit)
        }

        caster.doAttack(damage, targetCreature, skill, false, false, mcrit, false)
    }
}