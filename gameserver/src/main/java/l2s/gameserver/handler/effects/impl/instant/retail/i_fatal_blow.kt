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
 * Fatal Blow effect implementation.
 * @author Adry_85
 * @author Java-man
 */
class i_fatal_blow(template: EffectTemplate?) : i_abstract_effect(template) {

    private val _power = params.getDouble("i_fatal_blow_param1")
    private val _chanceBoost = params.getDouble("i_fatal_blow_param2")
    private val _criticalChance = params.getDouble("i_fatal_blow_param3")
    private val _overHit = params.getInteger("i_fatal_blow_param4", 0) == 1

    override fun calcSuccess(caster: Creature, target: Creature, skill: Skill): Boolean {
        if (!target.isCreature) {
            return false
        }

        if (Formulas.calcPhysicalSkillEvasion(caster, target.asCreature(), skill)) {
            return false
        }

        return Formulas.calcBlowSuccess(caster, target.asCreature(), skill, _chanceBoost)
    }

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val targetCreature = target.asCreature() ?: return

        if (caster.isAlikeDead) {
            return
        }

        if (_overHit && targetCreature.isMonster) {
            targetCreature.asMonster().overhitEnabled(true)
        }

        val ss = skill.useSoulShot() && caster.isChargedShot(ShotType.SOULSHOT)
        val shld = Formulas.calcShldUse(caster, targetCreature)
        var damage = Formulas.calcBlowDamage(caster, targetCreature, skill, false, _power, shld, ss)
        val crit = Formulas.calcCrit(_criticalChance, caster, targetCreature, skill)
        if (crit) {
            damage *= 2.0
        }

        if (skill.useSoulShot()) {
            soulShotUsed.set(true)
        }

        caster.doAttack(damage, targetCreature, skill, false, false, true, false)
    }

}