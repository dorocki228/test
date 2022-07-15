package l2s.gameserver.handler.effects.impl.instant.retail

import l2s.gameserver.handler.effects.impl.instant.i_abstract_effect
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Skill
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.model.base.ShotType
import l2s.gameserver.skills.AbnormalType
import l2s.gameserver.stats.DoubleStat
import l2s.gameserver.stats.Formulas
import l2s.gameserver.templates.item.isRanged
import l2s.gameserver.templates.skill.EffectTemplate
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Physical Attack effect implementation. <br>
 * Current formulas were tested to be the best matching retail, damage appears to be identical: <br>
 * For melee skills: 70 * graciaSkillBonus1.10113 * (patk * lvlmod + power) * crit * ss * skillpowerbonus / pdef <br>
 * For ranged skills: 70 * (patk * lvlmod + power + patk + power) * crit * ss * skillpower / pdef <br>
 *
 * {i_p_attack;Power;CritRate;IgnoreDefMode;IgnoreDefFactor}
 *
 * Power: Мощность умения
 * CritRate: Шанс крита умения (х2 урон)
 * IgnoreDefMode:
 * 0 - Защита цели не игнорируется
 * 1 - Игнорируется только Shield Def цели(любые бонусы и способности щита, в т.ч. полный блок щитом)
 * 2 - Полностью игнорируются Shield Def и P.Def цели(значение защиты цели в формуле становится 1.0)
 * Это точная инфа, получена из реверса GF PTS.
 *
 * IgnoreDefMode == 3 и IgnoreDefFactor - это нововведение с HF, поэтому далее инфа не точная, но максимально вероятная:
 * Судя по всему, режим 3 полностью игнорирует Shield Def цели
 * (т.к. в GF любое ненулевое значение означает игнор щита),
 * а P.Def цели игнорируется частично, соответственно параметру IgnoreDefFactor,
 * т.е. P.Def = P.Def * ((100 - IgnoreDefFactor) / 100)
 *
 * @author Nik
 * @author Java-man
 */
class i_p_attack_over_hit(template: EffectTemplate) : i_abstract_effect(template) {

    private val power = params.getDouble("i_p_attack_over_hit_param1")
    private val criticalChance = params.getDouble("i_p_attack_over_hit_param2")
    private val ignoreDefMode = params.getInteger("i_p_attack_over_hit_param3")
    private val ignoreDefFactor = params.getInteger("i_p_attack_over_hit_param4")

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
        val targetCreature: Creature = target.asCreature() ?: return

        if (caster.isAlikeDead) {
            return
        }

        if (targetCreature.isFakeDeath) {
            targetCreature.breakFakeDeath()
        }

        if (targetCreature.isMonster) {
            targetCreature.asMonster().overhitEnabled(true)
        }

        val attack = caster.stat.getPAtk().toDouble()
        var defence = targetCreature.stat.getPDef().toDouble()

        when (ignoreDefMode) {
            0 -> when (Formulas.calcShldUse(caster, targetCreature)) {
                Formulas.SHIELD_DEFENSE_SUCCEED -> defence += targetCreature.shldDef
                Formulas.SHIELD_DEFENSE_PERFECT_BLOCK -> defence = -1.0
            }
            2 -> defence = 1.0
            3 -> defence *= ((100 - ignoreDefFactor) / 100.0)
        }

        var damage = 1.0
        val critical = Formulas.calcCrit(criticalChance, caster, targetCreature, skill)

        if (defence != -1.0) {
            // Trait, elements
            val weaponTraitMod = Formulas.calcWeaponTraitBonus(caster, targetCreature)
            val generalTraitMod = Formulas.calcGeneralTraitBonus(caster, targetCreature, skill.traitType, true)
            val attributeMod = Formulas.calcAttributeBonus(caster, targetCreature, skill)
            val pvpPveMod = Formulas.calculatePvpPveBonus(caster, targetCreature, skill, true)
            val randomMod = caster.randomDamageMultiplier

            // Skill specific mods.
            val wpnMod = if (caster.attackType.isRanged()) 70.0 else 70 * 1.10113
            val rangedBonus = if (caster.attackType.isRanged()) attack + power else 0.0
            val abnormalMod = if (_abnormals.any { targetCreature.abnormalList.contains(it) }) {
                _abnormalPowerMod
            } else 1.0
            val critMod = when {
                critical -> 2 * Formulas.calcCritDamage(caster, targetCreature, skill)
                else -> 1.0
            }
            val ssmod = when {
                skill.useSoulShot() && caster.isChargedShot(ShotType.SOULSHOT) -> {
                    2 * caster.stat.getValue(DoubleStat.SOULSHOTS_BONUS)
                }
                else -> 1.0
            } // 2.04 for dual weapon?

            // ...................____________Melee Damage_____________......................................___________________Ranged Damage____________________
            // ATTACK CALCULATION 77 * ((pAtk * lvlMod) + power) / pdef            RANGED ATTACK CALCULATION 70 * ((pAtk * lvlMod) + power + patk + power) / pdef
            // ```````````````````^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^``````````````````````````````````````^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
            val baseMod = wpnMod * (attack * caster.levelBonus + power + rangedBonus) / defence
            damage = baseMod * abnormalMod * ssmod * critMod * weaponTraitMod * generalTraitMod * attributeMod * pvpPveMod * randomMod
            damage = caster.stat.getValue(DoubleStat.PHYSICAL_SKILL_POWER, damage)
        }

        if (skill.useSoulShot()) {
            soulShotUsed.set(true)
        }

        caster.doAttack(damage, targetCreature, skill, false, false, critical, false)
    }

}