package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.model.base.ShotType
import l2s.gameserver.stats.Formulas
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Backstab effect implementation.
 *
 * @author Adry_85
 * @author Java-man
 */
class i_backstab(template: EffectTemplate?) : i_abstract_effect(template) {

    private val power = params.getDouble("i_backstab_param1")
    private val chanceBoost = params.getDouble("i_backstab_param2")
    private val criticalChance = params.getDouble("i_backstab_param3")
    private val overHit = params.getBool("overHit", true)

    override fun calcSuccess(caster: Creature, target: Creature, skill: Skill): Boolean {
        if (!target.isCreature) {
            return false
        }

        if (caster.isInFrontOf(target)) {
            return false
        }

        if (Formulas.calcPhysicalSkillEvasion(caster, target, skill)) {
            return false
        }

        return Formulas.calcBlowSuccess(caster, target, skill, chanceBoost)
    }

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        if (caster.isAlikeDead) {
            return
        }

        if (overHit && target.isMonster) {
            target.asMonster().overhitEnabled(true)
        }

        val ss = skill.useSoulShot() && caster.isChargedShot(ShotType.SOULSHOT)
        val shld: Byte = Formulas.calcShldUse(caster, target)
        var damage: Double = Formulas.calcBlowDamage(caster, target, skill, true, power, shld, ss)

        if (Formulas.calcCrit(criticalChance, caster, target, skill)) {
            damage *= 2.0
        }

        if (skill.useSoulShot()) {
            soulShotUsed.set(true)
        }

        caster.doAttack(damage, target, skill, false, true, true, false)
    }

}