package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.model.base.ShotType
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.stats.Formulas
import l2s.gameserver.templates.item.isRanged
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max

/**
 * Physical Attack HP Link effect implementation.<br>
 * <b>Note</b>: Initial formula taken from PhysicalAttack.
 * @author Adry_85, Nik
 * @author Java-man
 */
class i_physical_attack_hp_link(template: EffectTemplate) : i_abstract_effect(template) {

    private val _power = params.getDouble("i_physical_attack_hp_link_param1")
    private val _pAtkMod = params.getDouble("pAtkMod", 1.0)
    private val _pDefMod = params.getDouble("pDefMod", 1.0)
    private val _criticalChance = params.getDouble("i_physical_attack_hp_link_param2")
    // TODO check
    private val _overHit = params.getInteger("i_physical_attack_hp_link_param3") == 1
    // TODO check
    private val _ignoreShieldDefence = params.getInteger("i_physical_attack_hp_link_param4")

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
        val targetCreature: Creature = target.asCreature() ?: return

        if (caster.isAlikeDead) {
            return
        }

        if (targetCreature.isFakeDeath) {
            targetCreature.breakFakeDeath()
        }

        if (_overHit && targetCreature.isMonster) {
            targetCreature.asMonster().overhitEnabled(true)
        }

        val attack = caster.stat.getPAtk().toDouble()
        var defence = targetCreature.stat.getPDef().toDouble()

        if (_ignoreShieldDefence > 0) {
            when (Formulas.calcShldUse(caster, targetCreature)) {
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
            val weaponTraitMod = Formulas.calcWeaponTraitBonus(caster, targetCreature)
            val generalTraitMod = Formulas.calcGeneralTraitBonus(caster, targetCreature, skill.traitType, true)
            val attributeMod = Formulas.calcAttributeBonus(caster, targetCreature, skill)
            val pvpPveMod = Formulas.calculatePvpPveBonus(caster, targetCreature, skill, true)
            val randomMod = caster.randomDamageMultiplier

            // Skill specific mods.
            val wpnMod = if (caster.attackType.isRanged()) 70.0 else 70.0 * 1.10113
            val rangedBonus = if (caster.attackType.isRanged()) attack + _power else 0.0
            val critMod = when {
                critical -> 2.0 * Formulas.calcCritDamage(caster, targetCreature, skill)
                else -> 1.0
            }
            val ssmod = when {
                skill.useSoulShot() && caster.isChargedShot(ShotType.SOULSHOT) -> {
                    2.0 * caster.stat.getValue(DoubleStat.SOULSHOTS_BONUS)
                }
                else -> 1.0
            } // 2.04 for dual weapon?

            // ...................____________Melee Damage_____________......................................___________________Ranged Damage____________________
            // ATTACK CALCULATION 77 * ((pAtk * lvlMod) + power) / pdef            RANGED ATTACK CALCULATION 70 * ((pAtk * lvlMod) + power + patk + power) / pdef
            // ```````````````````^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^``````````````````````````````````````^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
            val baseMod = wpnMod * (attack * caster.levelBonus + _power + rangedBonus) / defence
            damage = baseMod * ssmod * critMod * weaponTraitMod * generalTraitMod * attributeMod * pvpPveMod * randomMod
            damage = caster.stat.getValue(DoubleStat.PHYSICAL_SKILL_POWER, damage)

            //damage *= max(1.0, (100 - caster.currentHp / caster.maxHp * 100.0 - 40) * 2.0 / 100.0)
            damage *= max(1.0, 1.0 + (1.0 - caster.currentHp / caster.maxHp) * 2.0)
        }

        if (skill.useSoulShot()) {
            soulShotUsed.set(true)
        }

        caster.doAttack(damage, targetCreature, skill, false, false, critical, false)
    }

}