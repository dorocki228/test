package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.model.base.ShotType
import l2s.gameserver.stats.Formulas
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * HP Drain effect implementation.
 * @author Adry_85
 * @author Java-man
 */
class i_hp_drain(template: EffectTemplate) : i_abstract_effect(template) {

    private val _power = params.getDouble("i_hp_drain_param1")
    private val _percentage = params.getDouble("i_hp_drain_param2")

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val targetCreature = target.asCreature() ?: return

        if (caster.isAlikeDead) {
            return
        }

        val sps = skill.useSpiritShot() && caster.isChargedShot(ShotType.SPIRITSHOT)
        val bss = skill.useSpiritShot() && caster.isChargedShot(ShotType.BLESSED_SPIRITSHOT)
        val mcrit = Formulas.calcCrit(skill.magicCriticalRate, caster, targetCreature, skill)
        val damage = Formulas.calcMagicDam(
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

        val cp = targetCreature.currentCp.toInt()
        val hp = targetCreature.currentHp.toInt()

        val drain = when {
            cp > 0 -> if (damage < cp) 0.0 else damage - cp
            damage > hp -> hp.toDouble()
            else -> damage
        }

        val hpAdd = _percentage / 100.0 * drain
        val hpFinal = when {
            caster.currentHp + hpAdd > caster.maxHp -> caster.maxHp.toDouble()
            else -> caster.currentHp + hpAdd
        }
        caster.setCurrentHp(hpFinal, false)

        caster.doAttack(damage, targetCreature, skill, false, false, mcrit, false)
    }

}