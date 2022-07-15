package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.model.base.ShotType
import l2s.gameserver.skills.AbnormalType
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.stats.Formulas
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max

/**
 * Energy Attack effect implementation.
 * @author NosBit
 * @author Java-man
 */
class i_energy_attack(template: EffectTemplate) : i_abstract_effect(template) {

    private val _power = params.getDouble("i_energy_attack_param1")
    private val _pAtkMod = params.getDouble("pAtkMod", 1.0)
    private val _pDefMod = params.getDouble("pDefMod", 1.0)
    private val _criticalChance = params.getDouble("i_energy_attack_param2")
    // TODO check
    private val _overHit = params.getInteger("i_energy_attack_param3") == 0
    // TODO check
    private val _ignoreShieldDefence = params.getInteger("i_energy_attack_param4")
    // TODO find
    private val unk = params.getInteger("i_energy_attack_param5")

    private val _abnormals: Set<AbnormalType>
    private val _abnormalPowerMod: Double

    init {
        val abnormals = params.getString("abnormalType", null)
        _abnormals = if (abnormals != null && abnormals.isNotEmpty()) {
            abnormals.split(";")
                    .map { AbnormalType.valueOf(it) }
                    .toSet()
        } else {
            emptySet()
        }
        _abnormalPowerMod = params.getDouble("damageModifier", 1.0)
    }

    override fun calcSuccess(caster: Creature, target: Creature, skill: Skill): Boolean {
        if (!target.isCreature) {
            return false
        }

        if (Formulas.calcPhysicalSkillEvasion(caster, target.asCreature(), skill)) {
            return false
        }

        return true
    }

    override fun instantUse(caster: Creature, target: Creature, soulShotUsed: AtomicBoolean, reflected: Boolean, cubic: Cubic) {
        val casterPlayer = caster.player ?: return

        val targetCreature: Creature = target.asCreature() ?: return

        val sphericBarrier = targetCreature.stat.getValue(DoubleStat.SPHERIC_BARRIER_RANGE, Double.MAX_VALUE)
        if (casterPlayer.distance3d(target) > sphericBarrier) {
            return
        }

        if (_overHit && targetCreature.isMonster) {
            targetCreature.asMonster().overhitEnabled(true)
        }

        var defence = targetCreature.stat.getPDef() * _pDefMod
        if (_ignoreShieldDefence > 0) {
            when (Formulas.calcShldUse(casterPlayer, targetCreature)) {
                Formulas.SHIELD_DEFENSE_SUCCEED -> {
                    // TODO should work like this ?
                    defence += targetCreature.shldDef * _ignoreShieldDefence / 100.0
                }
                Formulas.SHIELD_DEFENSE_PERFECT_BLOCK -> {
                    defence = -1.0
                }
            }
        }

        var damage = 1.0
        val critical = Formulas.calcCrit(_criticalChance, caster, targetCreature, skill)

        if (defence != -1.0) {
            // Trait, elements
            val weaponTraitMod = Formulas.calcWeaponTraitBonus(casterPlayer, targetCreature)
            val generalTraitMod = Formulas.calcGeneralTraitBonus(casterPlayer, targetCreature, skill.traitType, true)
            val attributeMod = Formulas.calcAttributeBonus(casterPlayer, targetCreature, skill)
            val pvpPveMod = Formulas.calculatePvpPveBonus(casterPlayer, targetCreature, skill, true)

            // Skill specific mods.
            val energyChargesBoost = 1 + Math.min(skill.chargeConsume, casterPlayer.charges) * 0.1 // 10% bonus damage for each charge used.
            val critMod = when {
                critical -> 2.0 * Formulas.calcCritDamage(casterPlayer, targetCreature, skill)
                else -> 1.0
            }
            val ssmod = when {
                skill.useSoulShot() && casterPlayer.isChargedShot(ShotType.SOULSHOT) -> {
                    2.0 * casterPlayer.stat.getValue(DoubleStat.SOULSHOTS_BONUS)
                }
                else -> 1.0
            } // 2.04 for dual weapon?

            // ...................________Initial Damage_________...__Charges Additional Damage__...____________________________________
            // ATTACK CALCULATION ((77 * ((pAtk * lvlMod) + power) * (1 + (0.1 * chargesConsumed)) / pdef) * skillPower) + skillPowerAdd
            // ```````````````````^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^```^^^^^^^^^^^^^^^^^^^^^^^^^^^^^```^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
            val baseMod = 77 * (casterPlayer.stat.getPAtk() * casterPlayer.levelBonus + _power) / defence
            damage = baseMod * ssmod * critMod * weaponTraitMod * generalTraitMod * attributeMod * energyChargesBoost * pvpPveMod
            damage = casterPlayer.stat.getValue(DoubleStat.PHYSICAL_SKILL_POWER, damage)
        }

        damage = max(0.0, damage)

        if (skill.useSoulShot()) {
            soulShotUsed.set(true)
        }

        casterPlayer.doAttack(damage, targetCreature, skill, false, false, critical, false)
    }

}