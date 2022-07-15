package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.model.base.ShotType
import l2s.gameserver.stats.Formulas
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Death Link effect implementation.
 * @author Adry_85
 * @author Java-man
 */
class i_death_link(template: EffectTemplate) : i_abstract_effect(template) {

    private val _power: Double = params.getDouble("i_death_link_param1")

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val targetCreature = target.asCreature() ?: return

        if (caster.isAlikeDead) {
            return
        }

        val sps = skill.useSpiritShot() && caster.isChargedShot(ShotType.SPIRITSHOT)
        val bss = skill.useSpiritShot() && caster.isChargedShot(ShotType.BLESSED_SPIRITSHOT)

        if (targetCreature.isFakeDeath) {
            targetCreature.breakFakeDeath()
        }

        val mcrit = Formulas.calcCrit(skill.magicCriticalRate, caster, targetCreature, skill)
        val power = _power * (-(caster.currentHp * 2 / caster.maxHp) + 2)
        val damage = Formulas.calcMagicDam(caster, targetCreature, skill,
                caster.stat.getMAtk(), power, targetCreature.stat.getMDef(),
                sps, bss, mcrit)
        caster.doAttack(damage, targetCreature, skill, false, false, mcrit, false)
    }
}