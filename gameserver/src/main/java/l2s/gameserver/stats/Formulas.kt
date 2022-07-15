package l2s.gameserver.stats

import l2s.commons.math.MathUtils
import l2s.commons.math.constrain
import l2s.commons.util.Rnd
import l2s.gameserver.Config
import l2s.gameserver.GameTimeController
import l2s.gameserver.data.xml.holder.HitCondBonusHolder
import l2s.gameserver.data.xml.holder.KarmaIncreaseDataHolder
import l2s.gameserver.geometry.ILocation
import l2s.gameserver.handler.effects.impl.instant.retail.cub_hp_drain
import l2s.gameserver.handler.effects.impl.instant.retail.i_hp_drain
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Player
import l2s.gameserver.model.Skill
import l2s.gameserver.model.actor.instances.creature.Abnormal
import l2s.gameserver.model.actor.instances.player.Cubic
import l2s.gameserver.model.base.*
import l2s.gameserver.model.instances.StaticObjectInstance
import l2s.gameserver.model.instances.residences.SiegeFlagInstance
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo
import l2s.gameserver.network.l2.s2c.SystemMessagePacket
import l2s.gameserver.skills.AbnormalType
import l2s.gameserver.skills.BasicProperty
import l2s.gameserver.skills.TraitType
import l2s.gameserver.templates.StatsSet
import l2s.gameserver.templates.item.ArmorTemplate
import l2s.gameserver.templates.item.ArmorTemplate.ArmorType
import l2s.gameserver.templates.item.WeaponTemplate.WeaponType
import l2s.gameserver.templates.item.isRanged
import l2s.gameserver.utils.Debug
import l2s.gameserver.utils.PositionUtils
import l2s.gameserver.utils.PositionUtils.Position
import kotlin.math.*

/**
 * Global calculations.
 *
 * @since 10.10.2019
 */
object Formulas {

    /** Regeneration Task period.  */
    private const val HP_REGENERATE_PERIOD = 3000 // 3 secs

    const val SHIELD_DEFENSE_FAILED: Byte = 0 // no shield defense
    const val SHIELD_DEFENSE_SUCCEED: Byte = 1 // normal shield defense
    const val SHIELD_DEFENSE_PERFECT_BLOCK: Byte = 2 // perfect block

    private const val MELEE_ATTACK_RANGE = 40

    private const val MIN_NPC_LVL_DMG_PENALTY = 78
    private val NPC_DMG_PENALTY: Map<Int, Double> = mapOf(
        0 to 0.8,
        1 to 0.6,
        2 to 0.5,
        3 to 0.42,
        4 to 0.36,
        5 to 0.32,
        6 to 0.28,
        7 to 0.25
    )
    private val NPC_CRIT_DMG_PENALTY: Map<Int, Double> = mapOf(
        0 to 0.8,
        1 to 0.6,
        2 to 0.5,
        3 to 0.42,
        4 to 0.36,
        5 to 0.32,
        6 to 0.28,
        7 to 0.25
    )
    private val NPC_SKILL_DMG_PENALTY: Map<Int, Double> = mapOf(
        0 to 0.8,
        1 to 0.6,
        2 to 0.5,
        3 to 0.42,
        4 to 0.36,
        5 to 0.32,
        6 to 0.28,
        7 to 0.25
    )

    private const val MIN_NPC_LVL_MAGIC_PENALTY: Int = 78
    private val NPC_SKILL_CHANCE_PENALTY: Map<Int, Double> = mapOf(
            0 to 2.5,
            1 to 3.0,
            2 to 3.25,
            3 to 3.5
    ).withDefault { 3.5 }

    /**
     * Return the period between 2 regeneration task (3s for L2Character, 5 min for L2DoorInstance).
     * @param cha
     * @return
     */
    fun getRegeneratePeriod(cha: Creature): Int {
        return when {
            cha.isDoor -> HP_REGENERATE_PERIOD * 100
            else -> HP_REGENERATE_PERIOD
        }
    }

    fun calcBlowDamage(
        attacker: Creature,
        target: Creature,
        skill: Skill,
        backstab: Boolean,
        power: Double,
        shld: Byte,
        ss: Boolean
    ): Double {
        val barrierRange = target.stat.getValue(DoubleStat.SPHERIC_BARRIER_RANGE, Double.MAX_VALUE)
        if (!attacker.isInRadius3d(target, barrierRange)) {
            return 0.0
        }

        var defence = target.stat.getPDef().toDouble()

        when (shld) {
            SHIELD_DEFENSE_SUCCEED -> {
                defence += target.shldDef
            }
            SHIELD_DEFENSE_PERFECT_BLOCK -> {
                return 1.0
            }
        }

        // Critical
        val criticalMod = attacker.stat.getValue(DoubleStat.CRITICAL_DAMAGE, 1.0)
        val criticalPositionMod =
            attacker.stat.getPositionTypeValue(DoubleStat.CRITICAL_DAMAGE, PositionUtils2.getPosition(attacker, target))
        val criticalVulnMod = target.stat.getValue(DoubleStat.DEFENCE_CRITICAL_DAMAGE, 1.0)
        val criticalAddMod = attacker.stat.getValue(DoubleStat.CRITICAL_DAMAGE_ADD, 0.0)
        val criticalAddVuln = target.stat.getValue(DoubleStat.DEFENCE_CRITICAL_DAMAGE_ADD, 0.0)
        // Trait, elements
        val weaponTraitMod = calcWeaponTraitBonus(attacker, target)
        val generalTraitMod = calcGeneralTraitBonus(attacker, target, skill.traitType, true)
        val attributeMod = calcAttributeBonus(attacker, target, skill)
        val randomMod = attacker.randomDamageMultiplier
        val pvpPveMod: Double = calculatePvpPveBonus(attacker, target, skill, true)

        // Initial damage
        val ssmod =
            if (ss) 2 * attacker.stat.getValue(DoubleStat.SOULSHOTS_BONUS) else 1.toDouble() // 2.04 for dual weapon?
        val cdMult = criticalMod * ((criticalPositionMod - 1) / 2 + 1) * ((criticalVulnMod - 1) / 2 + 1)
        val cdPatk = criticalAddMod + criticalAddVuln
        val position: Position = PositionUtils2.getPosition(attacker, target)
        val isPosition: Double = when (position) {
            Position.BACK -> 0.2
            Position.SIDE -> 0.05
            else -> 0.0
        }

        // ........................_____________________________Initial Damage____________________________...___________Position Additional Damage___________..._CriticalAdd_
        // ATTACK CALCULATION 77 * [(skillpower+patk) * 0.666 * cdbonus * cdPosBonusHalf * cdVulnHalf * ss + isBack0.2Side0.05 * (skillpower+patk*ss) * random + 6 * cd_patk] / pdef
        // ````````````````````````^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^```^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^```^^^^^^^^^^^^
        val baseMod =
            77 * ((power + attacker.stat.getPAtk()) * 0.666 * ssmod * cdMult + isPosition * (power + attacker.stat.getPAtk() * ssmod) * randomMod + 6 * cdPatk) / defence
        val damage = baseMod * weaponTraitMod * generalTraitMod * attributeMod * randomMod * pvpPveMod

        if (attacker.isDebug) {
            val set = StatsSet()
            set.set("skillPower", power)
            set.set("ssboost", ssmod)
            set.set("isPosition", isPosition)
            set.set("baseMod", baseMod)
            set.set("criticalMod", criticalMod)
            set.set("criticalVulnMod", criticalVulnMod)
            set.set("criticalAddMod", criticalAddMod)
            set.set("criticalAddVuln", criticalAddVuln)
            set.set("weaponTraitMod", weaponTraitMod)
            set.set("generalTraitMod", generalTraitMod)
            set.set("attributeMod", attributeMod)
            set.set("weaponMod", randomMod)
            set.set("penaltyMod", pvpPveMod)
            set.set("damage", damage.toInt())

            Debug.sendSkillDebug(attacker, target, skill, set)
        }

        return damage
    }

    fun calcMagicDam(
            attacker: Creature,
            target: Creature,
            skill: Skill,
            mAtk: Double,
            power: Double,
            mDef: Double,
            sps: Boolean,
            bss: Boolean,
            mcrit: Boolean
    ): Double {
        if (!attacker.isInRadius3d(target, target.stat.getValue(DoubleStat.SPHERIC_BARRIER_RANGE, Double.MAX_VALUE))) {
            return 0.0
        }

        // Bonus Spirit shot
        val shotsBonus = when {
            bss -> 4.0 * attacker.stat.getValue(DoubleStat.SPIRITSHOTS_BONUS)
            sps -> 2.0 * attacker.stat.getValue(DoubleStat.SPIRITSHOTS_BONUS)
            else -> 1.0
        }
        val critMod = if (mcrit) {
            2.0 * calcCritDamage(attacker, target, skill) // Trait, elements
        } else 1.0// TODO not really a proper way... find how it works then implement. // damage += attacker.getStat().getValue(Stats.MAGIC_CRIT_DMG_ADD, 0);

        val generalTraitMod = calcGeneralTraitBonus(attacker, target, skill.traitType, true)
        val attributeMod = calcAttributeBonus(attacker, target, skill)
        val randomMod = attacker.randomDamageMultiplier
        val pvpPveMod = calculatePvpPveBonus(attacker, target, skill, mcrit)

        // MDAM Formula.
        var damage = 91.0 * power * sqrt(mAtk * shotsBonus) / mDef

        // Failure calculation
        if (/*PlayerConfig.ALT_GAME_MAGICFAILURES &&*/ !calcMagicSuccess(attacker, target, skill)) {
            if (attacker.isPlayer) {
                if (calcMagicSuccess(attacker, target, skill) && target.level - attacker.level <= 9) {
                    if (skill.hasEffect(i_hp_drain::class.java)) {
                        attacker.sendPacket(SystemMsg.DRAIN_WAS_ONLY_50_PERCENT_SUCCESSFUL)
                    } else {
                        attacker.sendPacket(SystemMsg.YOUR_ATTACK_HAS_FAILED)
                    }
                    damage /= 2.0
                } else {
                    val sm = SystemMessagePacket(SystemMsg.C1_HAS_RESISTED_YOUR_S2)
                    sm.addName(target)
                    sm.addSkillName(skill)
                    attacker.sendPacket(sm)
                    damage = 0.0
                }
            }

            if (target.isPlayer) {
                val sm = when {
                    skill.hasEffect(i_hp_drain::class.java) -> {
                        SystemMessagePacket(SystemMsg.YOU_RESISTED_C1S_DRAIN)
                    }
                    else -> {
                        SystemMessagePacket(SystemMsg.YOU_RESISTED_C1S_MAGIC)
                    }
                }
                sm.addName(attacker)
                target.sendPacket(sm)
            }
        }

        damage *= critMod * generalTraitMod * attributeMod * randomMod * pvpPveMod
        damage = attacker.stat.getValue(DoubleStat.MAGICAL_SKILL_POWER, damage)

        return damage
    }

    fun calcMagicDam(
            attacker: Cubic,
            target: Creature,
            skill: Skill,
            power: Double,
            mDef: Double,
            mcrit: Boolean,
            shld: Byte
    ): Double {
        val mAtk = attacker.template.power

        val owner = attacker.owner

        // MDAM Formula.
        var damage = 70.0 * (mAtk + power) / mDef

        // Failure calculation
        if (/*PlayerConfig.ALT_GAME_MAGICFAILURES &&*/ !calcCubicSkillSuccess(attacker, target, skill, shld)) {
            if (owner.isPlayer) {
                if (calcCubicSkillSuccess(attacker, target, skill, shld) && target.level - owner.level <= 9) {
                    if (skill.hasEffect(cub_hp_drain::class.java)) {
                        owner.sendPacket(SystemMsg.DRAIN_WAS_ONLY_50_PERCENT_SUCCESSFUL)
                    } else {
                        owner.sendPacket(SystemMsg.YOUR_ATTACK_HAS_FAILED)
                    }
                    damage /= 2.0
                } else {
                    val sm = SystemMessagePacket(SystemMsg.C1_HAS_RESISTED_YOUR_S2)
                    sm.addName(target)
                    sm.addSkillName(skill)
                    owner.sendPacket(sm)
                    damage = 1.0
                }
            }

            if (target.isPlayer) {
                val sm = when {
                    skill.hasEffect(cub_hp_drain::class.java) -> {
                        SystemMessagePacket(SystemMsg.YOU_RESISTED_C1S_DRAIN)
                    }
                    else -> {
                        SystemMessagePacket(SystemMsg.YOU_RESISTED_C1S_MAGIC)
                    }
                }
                sm.addName(owner)
                target.sendPacket(sm)
            }
        }

        val critMod = if (mcrit) {
            2.0 * calcCritDamage(owner, target, skill) // Trait, elements
        } else 1.0// TODO not really a proper way... find how it works then implement. // damage += attacker.getStat().getValue(Stats.MAGIC_CRIT_DMG_ADD, 0);

        /*val generalTraitMod = calcGeneralTraitBonus(attacker, target, skill.traitType, true)
        val attributeMod = calcAttributeBonus(attacker, target, skill)
        val pvpPveMod = calculatePvpPveBonus(attacker, target, skill, mcrit)*/

        damage *= critMod /** generalTraitMod * attributeMod * pvpPveMod*/
        damage = owner.stat.getValue(DoubleStat.MAGICAL_SKILL_POWER, damage)

        return damage
    }

    /**
     * Returns true in case of critical hit
     * @param rate
     * @param skill
     * @param activeChar
     * @param target
     * @return
     */
    fun calcCrit(
        rate: Double,
        activeChar: Creature,
        target: Creature?,
        skill: Skill?
    ): Boolean {
        // Skill critical rate is calculated up to the first decimal, thats why multiply by 10 and compare to 1000.
        var rate = rate
        val level = activeChar.level
        if (skill != null) {
            // Magic Critical Rate
            if (skill.isMagic) {
                rate = activeChar.stat.getValue(DoubleStat.MAGIC_CRITICAL_RATE)
                if (target == null || !skill.isBad) {
                    return min(rate, 320.0) > Rnd.get(1000)
                }

                val defenceMagicCritRate = target.stat.getValue(DoubleStat.DEFENCE_MAGIC_CRITICAL_RATE, rate)
                val defenceMagicCritRateAdd = target.stat.getValue(DoubleStat.DEFENCE_MAGIC_CRITICAL_RATE_ADD, 0.0)
                var finalRate = defenceMagicCritRate + defenceMagicCritRateAdd

                if (level >= 78 && target.level >= 78) {
                    finalRate += (sqrt(level.toDouble()) + (level - target.level) / 25.0) * 10
                    return min(finalRate, 320.0) > Rnd.get(1000)
                }

                return min(finalRate, 200.0) > Rnd.get(1000)
            }

            // Physical skill critical rate
            val statBonus: Double

            // There is a chance that activeChar has altered base stat for skill critical.
            val skillCritRateStat = activeChar.stat.getValue(DoubleStat.STAT_BONUS_SKILL_CRITICAL).toByte()
            statBonus = if (skillCritRateStat >= 0 && skillCritRateStat < BaseStats.values().size) { // Best tested
                BaseStats.values()[skillCritRateStat.toInt()].calcBonus(activeChar)
            } else { // Default base stat used for skill critical formula is STR.
                BaseStats.STR.calcBonus(activeChar)
            }

            val rateBonus = activeChar.stat.getValue(DoubleStat.CRITICAL_RATE_SKILL, 1.0)
            val finalRate = rate * statBonus * rateBonus * 10
            return finalRate > Rnd.get(1000)
        }

        requireNotNull(target)

        // Autoattack critical rate.
        // Even though, visible critical rate is capped to 500, you can reach higher than 50% chance with position and level modifiers.
        // TODO: Find retail-like calculation for criticalRateMod.
        val defenceCritRate = target.stat.getValue(DoubleStat.DEFENCE_CRITICAL_RATE, rate)
        val defenceCritRateAdd = target.stat.getValue(DoubleStat.DEFENCE_CRITICAL_RATE_ADD, 0.0)
        val criticalRateMod = (defenceCritRate + defenceCritRateAdd) / 10.0
        val criticalLocBonus: Double = calcCriticalPositionBonus(activeChar, target)
        val criticalHeightBonus = calcCriticalHeightBonus(activeChar, target)
        rate = criticalLocBonus * criticalRateMod * criticalHeightBonus

        // Autoattack critical depends on level difference at high levels as well.
        if (level >= 78 || target.level >= 78) {
            rate += sqrt(level.toDouble()) * (level - target.level) * 0.125
        }

        // Autoattack critical rate is limited between 3%-97%.
        rate = MathUtils.constrain(rate, 3.0, 97.0)
        return rate > Rnd.get(100)
    }

    fun calcCriticalHeightBonus(from: ILocation, target: ILocation): Double {
        val diff = MathUtils.constrain(from.z - target.z, -25, 25).toDouble()
        return (diff * 4 / 5 + 10) / 100 + 1
    }

    /**
     * Gets the default (10% for side, 30% for back) positional critical rate bonus and multiplies it by any buffs that give positional critical rate bonus.
     * @param activeChar the attacker.
     * @param target the target.
     * @return a multiplier representing the positional critical rate bonus. Autoattacks for example get this bonus on top of the already capped critical rate of 500.
     */
    fun calcCriticalPositionBonus(activeChar: Creature, target: Creature): Double {
        val position: Position =
            if (activeChar.stat.has(BooleanStat.ATTACK_BEHIND)) {
                Position.BACK
            } else {
                PositionUtils2.getPosition(activeChar, target)
            }
        return when (position) {
            Position.SIDE -> {
                1.1 * activeChar.stat.getPositionTypeValue(DoubleStat.CRITICAL_RATE, Position.SIDE)
            }
            Position.BACK -> {
                1.3 * activeChar.stat.getPositionTypeValue(DoubleStat.CRITICAL_RATE, Position.BACK)
            }
            else -> {
                activeChar.stat.getPositionTypeValue(DoubleStat.CRITICAL_RATE, Position.FRONT)
            }
        }
    }

    /**
     * @param attacker
     * @param target
     * @param skill `skill` to be used in the calculation, else calculation will result for autoattack.
     * @return regular critical damage bonus. Positional bonus is excluded!
     */
    fun calcCritDamage(attacker: Creature, target: Creature, skill: Skill?): Double {
        val criticalDamage: Double
        val defenceCriticalDamage: Double

        if (skill != null) {
            if (skill.isMagic) {
                // Magic critical damage.
                criticalDamage = attacker.stat.getValue(DoubleStat.MAGIC_CRITICAL_DAMAGE, 1.0)
                defenceCriticalDamage = target.stat.getValue(DoubleStat.DEFENCE_MAGIC_CRITICAL_DAMAGE, 1.0)
            } else {
                criticalDamage = attacker.stat.getValue(DoubleStat.CRITICAL_DAMAGE_SKILL, 1.0)
                defenceCriticalDamage = target.stat.getValue(DoubleStat.DEFENCE_CRITICAL_DAMAGE_SKILL, 1.0)
            }
        } else {
            // Autoattack critical damage.
            val positionTypeValue = attacker.stat.getPositionTypeValue(DoubleStat.CRITICAL_DAMAGE, PositionUtils2.getPosition(attacker, target))
            criticalDamage = attacker.stat.getValue(DoubleStat.CRITICAL_DAMAGE, 1.0) * positionTypeValue
            defenceCriticalDamage = target.stat.getValue(DoubleStat.DEFENCE_CRITICAL_DAMAGE, 1.0)
        }

        return criticalDamage * defenceCriticalDamage
    }

    /**
     * @param attacker
     * @param target
     * @param skill `skill` to be used in the calculation, else calculation will result for autoattack.
     * @return critical damage additional bonus, not multiplier!
     */
    fun calcCritDamageAdd(attacker: Creature, target: Creature, skill: Skill?): Double {
        val criticalDamageAdd: Double
        val defenceCriticalDamageAdd: Double

        if (skill != null) {
            if (skill.isMagic) {
                // Magic critical damage.
                criticalDamageAdd = attacker.stat.getValue(DoubleStat.MAGIC_CRITICAL_DAMAGE_ADD, 0.0)
                defenceCriticalDamageAdd = target.stat.getValue(DoubleStat.DEFENCE_MAGIC_CRITICAL_DAMAGE_ADD, 0.0)
            } else {
                criticalDamageAdd = attacker.stat.getValue(DoubleStat.CRITICAL_DAMAGE_SKILL_ADD, 0.0)
                defenceCriticalDamageAdd = target.stat.getValue(DoubleStat.DEFENCE_CRITICAL_DAMAGE_SKILL_ADD, 0.0)
            }
        } else {
            // Autoattack critical damage.
            criticalDamageAdd = attacker.stat.getValue(DoubleStat.CRITICAL_DAMAGE_ADD, 0.0)
            defenceCriticalDamageAdd = target.stat.getValue(DoubleStat.DEFENCE_CRITICAL_DAMAGE_ADD, 0.0)
        }

        return criticalDamageAdd + defenceCriticalDamageAdd
    }

    fun calcAtkSpdMultiplier(creature: Creature): Double {
        val armorBonus = 1.0 // EquipedArmorSpeedByCrystal TODO: Implement me!
        val dexBonus: Double = BaseStats.DEX.calcBonus(creature)
        val weaponAttackSpeed: Double =
            DoubleStat.weaponBaseValue(creature, DoubleStat.PHYSICAL_ATTACK_SPEED) / armorBonus // unk868
        val attackSpeedPerBonus: Double = creature.stat.getMul(DoubleStat.PHYSICAL_ATTACK_SPEED)
        val attackSpeedDiffBonus: Double = creature.stat.getAdd(DoubleStat.PHYSICAL_ATTACK_SPEED)
        return dexBonus * (weaponAttackSpeed / 333.0) * attackSpeedPerBonus + attackSpeedDiffBonus / 333.0
    }

    fun calcMAtkSpdMultiplier(creature: Creature): Double {
        val armorBonus = 1.0 // TODO: Implement me!
        val witBonus: Double = BaseStats.WIT.calcBonus(creature)
        val castingSpeedPerBonus: Double = creature.stat.getMul(DoubleStat.MAGICAL_ATTACK_SPEED)
        val castingSpeedDiffBonus: Double = creature.stat.getAdd(DoubleStat.MAGICAL_ATTACK_SPEED)
        return 1.0 / armorBonus * witBonus * castingSpeedPerBonus + castingSpeedDiffBonus / 333.0
    }

    /**
     * Formula based on http://l2p.l2wh.com/nonskillattacks.html
     * @param attacker
     * @param target
     * @return `true` if hit missed (target evaded), `false` otherwise.
     */
    fun calcHitMiss(attacker: Creature, target: Creature): Boolean {
        val diff = attacker.stat.getAccuracy() - target.stat.getEvasionRate()
        var chance: Double = (80 + 2.0 * diff) * 10.0

        // Get additional bonus from the conditions when you are attacking
        chance *= getConditionBonus(attacker, target)

        chance = chance.constrain(200.0, 980.0)

        return chance < Rnd.get(1000)
    }

    /**
     * Returns:<br></br>
     * 0 = shield defense doesn't succeed<br></br>
     * 1 = shield defense succeed<br></br>
     * 2 = perfect block<br></br>
     * @param attacker
     * @param target
     * @param sendSysMsg
     * @return
     */
    fun calcShldUse(attacker: Creature, target: Creature, sendSysMsg: Boolean = true): Byte {
        val item = target.secondaryWeaponInstance
        if (item == null || item.template !is ArmorTemplate
                || item.template.itemType == ArmorType.SIGIL
        ) {
            return 0
        }

        val shldRate =
            if (attacker.attackType.isRanged()) {
                // if attacker use bow and target wear shield, shield block rate is multiplied by 1.3 (30%)
                target.stat.getValue(DoubleStat.SHIELD_DEFENCE_RATE) * 1.3
            } else {
                target.stat.getValue(DoubleStat.SHIELD_DEFENCE_RATE)
            }
        val degreeside =
            if (target.stat.has(BooleanStat.PHYSICAL_SHIELD_DEFENCE_ANGLE_ALL)) 360 else 120
        if (degreeside < 360) {
            val degree = abs(target.calculateAngleTo(attacker) - PositionUtils.convertHeadingToDegree(target.heading))
            if (degree > degreeside / 2) {
                return 0
            }
        }

        // Check shield success
        val shldSuccess: Byte = if (shldRate > Rnd.get(100)) {
            // If shield succeed, check perfect block.
            if (100 - 2 * BaseStats.DEX.calcBonus(target) < Rnd.get(100)) {
                SHIELD_DEFENSE_PERFECT_BLOCK
            } else {
                SHIELD_DEFENSE_SUCCEED
            }
        } else {
            SHIELD_DEFENSE_FAILED
        }

        if (sendSysMsg && target.isPlayer) {
            when (shldSuccess) {
                SHIELD_DEFENSE_SUCCEED -> {
                    target.sendPacket(SystemMsg.YOUR_SHIELD_DEFENSE_HAS_SUCCEEDED)
                }
                SHIELD_DEFENSE_PERFECT_BLOCK -> {
                    target.sendPacket(SystemMsg.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS)
                }
            }
        }

        return shldSuccess
    }

    fun calcMagicAffected(actor: Creature, target: Creature, skill: Skill): Boolean {
        // TODO: CHECK/FIX THIS FORMULA UP!!
        val defence = when {
            skill.isActive && skill.isBad -> target.stat.getMDef()
            else -> 0.0
        }

        val traitBonus = calcGeneralTraitBonus(actor, target, skill.traitType, false)
        val attack = 2 * actor.stat.getMAtk() * traitBonus
        var d = (attack - defence) / (attack + defence)

        if (skill.isDebuff) {
            if (target.stat.getAbnormalShieldBlocks() > 0) {
                if (target.stat.decrementAbnormalShieldBlocks() == 0) {
                    // target.stopEffects(EffectFlag.ABNORMAL_SHIELD);TODO Does it stop abnormal shield skill once charges are gone?
                }
                return false
            }
        }

        d += 0.5 * Rnd.nextGaussian()
        return d > 0
    }

    fun calcLvlBonusMod(attacker: Creature, target: Creature, skill: Skill): Double {
        val attackerLvl = if (skill.magicLevel > 0) skill.magicLevel else attacker.level
        val skillLvlBonusRateMod = 1 + skill.levelBonusRate / 100.0
        val lvlMod = 1 + (attackerLvl - target.level) / 100.0
        return skillLvlBonusRateMod * lvlMod
    }

    /**
     * Calculates the effect landing success.<br></br>
     * @param attacker the attacker
     * @param target the target
     * @param skill the skill
     * @return `true` if the effect lands
     */
    fun calcEffectSuccess(attacker: Creature, target: Creature, skill: Skill): Boolean {
        // StaticObjects can not receive continuous effects.
        if (target.isDoor || target is SiegeFlagInstance || target is StaticObjectInstance) {
            return false
        }

        if (skill.isDebuff) {
            var resisted = target.isCastingNow {
                val skillEntry = it.skillEntry
                skillEntry != null && skill.abnormalTypeList.containsAnyOf(skillEntry.template.abnormalResists)
            }
            if (!resisted) {
                if (target.stat.getAbnormalShieldBlocks() > 0) {
                    if (target.stat.decrementAbnormalShieldBlocks() == 0) {
                        // TODO Does it stop abnormal shield skill once charges are gone?
                        // target.abnormalList.stop(EffectFlag.ABNORMAL_SHIELD);
                    }
                    resisted = true
                }
            }

            if (!resisted) {
                if (!attacker.isInRadius3d(target, target.stat.getValue(DoubleStat.SPHERIC_BARRIER_RANGE, java.lang.Double.MAX_VALUE))) {
                    resisted = true
                }
            }

            if (resisted) {
                val sm = SystemMessagePacket(SystemMsg.C1_HAS_RESISTED_YOUR_S2)
                sm.addName(target)
                sm.addSkillName(skill)
                attacker.sendPacket(sm)

                attacker.sendPacket(ExMagicAttackInfo(attacker.objectId, target.objectId, ExMagicAttackInfo.RESISTED))

                return false
            }
        }

        val activateRate = skill.activateRate
        if (activateRate == -1) {
            return true
        }

        var magicLevel = skill.magicLevel
        if (magicLevel <= -1) {
            magicLevel = target.level + 3
        }

        val targetBasicProperty = getAbnormalResist(skill.basicProperty, target)
        val baseMod = (magicLevel - target.level + 3) * skill.levelBonusRate + activateRate + 30.0 - targetBasicProperty
        val elementMod = calcAttributeBonus(attacker, target, skill)
        val traitMod = calcGeneralTraitBonus(attacker, target, skill.traitType, false)
        val basicPropertyResist = getBasicPropertyResistBonus(skill.basicProperty, target)
        val buffDebuffMod = when {
            skill.isDebuff -> {
                target.stat.getValue(DoubleStat.RESIST_ABNORMAL_DEBUFF, 1.0)
            }
            !skill.isDebuff -> {
                target.stat.getValue(DoubleStat.RESIST_ABNORMAL_BUFF, 1.0)
            }
            else -> 0.0
        }
        val multiBuffMod = when {
            skill.abnormalTypeList.contains(AbnormalType.MULTI_BUFF) -> {
                target.stat.getValue(DoubleStat.RESIST_ABNORMAL_MULTI_BUFF, 1.0)
            }
            else -> 1.0
        }
        val rate = baseMod * elementMod * traitMod * buffDebuffMod * multiBuffMod
        val finalRate = when {
            traitMod > 0 -> rate.constrain(skill.minChance, skill.maxChance) * basicPropertyResist
            else -> 0.0
        }

        if (attacker.isDebug) {
            val set = StatsSet()
            set.set("baseMod", baseMod)
            set.set("elementMod", elementMod)
            set.set("traitMod", traitMod)
            set.set("basicPropertyResist", basicPropertyResist)
            set.set("buffDebuffMod", buffDebuffMod)
            set.set("rate", rate)
            set.set("finalRate", finalRate)
            Debug.sendSkillDebug(attacker, target, skill, set)
        }

        if (finalRate <= Rnd.get(100)) {
            val sm = SystemMessagePacket(SystemMsg.C1_HAS_RESISTED_YOUR_S2)
            sm.addName(target)
            sm.addSkillName(skill)
            attacker.sendPacket(sm)

            attacker.sendPacket(ExMagicAttackInfo(attacker.objectId, target.objectId, ExMagicAttackInfo.RESISTED))

            return false
        }
        return true
    }

    fun calcCubicSkillSuccess(
            attacker: Cubic,
            target: Creature,
            skill: Skill,
            shld: Byte
    ): Boolean {
        if (skill.isDebuff) {
            if (skill.activateRate == -1) {
                return true
            }

            if (target.stat.getAbnormalShieldBlocks() > 0) {
                if (target.stat.decrementAbnormalShieldBlocks() == 0) {
                    // target.stopEffects(EffectFlag.ABNORMAL_SHIELD);TODO Does it stop abnormal shield skill once charges are gone?
                }
                return false
            }
        }

        // Perfect Shield Block.
        if (shld == SHIELD_DEFENSE_PERFECT_BLOCK) {
            return false
        }

        // if target reflect this skill then the effect will fail
        if (calcBuffDebuffReflection(target, skill)) {
            return false
        }

        val owner = attacker.owner

        val targetBasicProperty = getAbnormalResist(skill.basicProperty, target)

        // Calculate BaseRate.
        val baseRate = skill.activateRate
        val statMod = 1 + targetBasicProperty / 100
        var rate = baseRate / statMod

        // Resist Modifier.

        val resMod = calcGeneralTraitBonus(owner, target, skill.traitType, false)
        rate *= resMod

        // Lvl Bonus Modifier.
        val lvlBonusMod = calcLvlBonusMod(owner, target, skill)
        rate *= lvlBonusMod

        // AttributeType Modifier.
        val elementMod = calcAttributeBonus(owner, target, skill)
        rate *= elementMod

        val basicPropertyResist = getBasicPropertyResistBonus(skill.basicProperty, target)

        // Add Matk/Mdef Bonus (TODO: Pending)

        // Check the Rate Limits.
        val finalRate = rate.constrain(skill.minChance, skill.maxChance) * basicPropertyResist

        if (owner.isDebug) {
            val set = StatsSet()
            set.set("baseMod", baseRate)
            set.set("resMod", resMod)
            set.set("statMod", statMod)
            set.set("elementMod", elementMod)
            set.set("lvlBonusMod", lvlBonusMod)
            set.set("rate", rate)
            set.set("finalRate", finalRate)
            Debug.sendSkillDebug(owner, target, skill, set)
        }

        return Rnd.get(100) < finalRate
    }

    /**
     * @param target
     * @param dmg
     * @return true in case when ATTACK is canceled due to hit
     */
    fun calcAtkBreak(target: Creature, dmg: Double): Boolean {
        if (target.isChanneling) {
            return false
        }

        var init = 0.0

        if (target.isCastingNow) {
            init = 15.0
        }

        /* should work like this ?
        if (PlayerConfig.ALT_GAME_CANCEL_BOW && target.isAttackingNow) {
            val wpn = target.activeWeaponInstance
            if (wpn != null && wpn.itemType == WeaponTemplate.WeaponType.BOW) {
                init = 15.0
            }
        }*/

        if (target.isRaid || target.isHpBlocked || init <= 0) {
            return false // No attack break
        }

        // Chance of break is higher with higher dmg
        init += sqrt(13 * dmg)
        // Chance is affected by target MEN
        init -= BaseStats.MEN.calcBonus(target) * 100.0 - 100
        // Calculate all modifiers for ATTACK_CANCEL
        var rate = target.stat.getValue(DoubleStat.ATTACK_CANCEL, init)
        // Adjust the rate to be between 1 and 99
        rate = rate.constrain(1.0, 99.0)

        return Rnd.chance(rate)
    }

    /** Calculate delay (in milliseconds) for skills cast  */
    fun calcSkillCastSpd(attacker: Creature, skill: Skill, skillTime: Double): Int {
        if (skill.isMagic)
            return (skillTime * 333.0 / max(attacker.stat.getMAtkSpd(), 1)).toInt()
        return if (skill.isPhysic) (skillTime * 333.0 / max(attacker.stat.getPAtkSpd(), 1)).toInt() else skillTime.toInt()
    }

    /**
     * Calculates the attribute bonus with the following formula: <BR></BR>
     * diff > 0, so AttBonus = 1,025 + sqrt[(diff^3) / 2] * 0,0001, cannot be above 1,25! <BR></BR>
     * diff < 0, so AttBonus = 0,975 - sqrt[(diff^3) / 2] * 0,0001, cannot be below 0,75! <BR></BR>
     * diff == 0, so AttBonus = 1<br></br>
     * It has been tested that physical skills do get affected by attack attribute even<br></br>
     * if they don't have any attribute. In that case only the biggest attack attribute is taken.
     * @param attacker
     * @param target
     * @param skill Can be `null` if there is no skill used for the attack.
     * @return The attribute bonus
     */
    fun calcAttributeBonus(attacker: Creature, target: Creature, skill: Skill?): Double {
        val attackAttribute: Int
        val defenceAttribute: Int
        if (skill != null && skill.attributeType != AttributeType.NONE) {
            attackAttribute = attacker.stat.getAttackElementValue(skill.attributeType) + skill.attributePower
            defenceAttribute = target.stat.getDefenseElementValue(skill.attributeType)
        } else {
            attackAttribute = attacker.stat.getAttackElementValue(attacker.stat.getAttackElement())
            defenceAttribute = target.stat.getDefenseElementValue(attacker.stat.getAttackElement())
        }

        val diff = attackAttribute - defenceAttribute
        if (diff > 0) {
            return min(
                1.025 + sqrt(
                    diff.toDouble().pow(3.0) / 2.0
                ) * 0.0001, 1.25
            )
        } else if (diff < 0) {
            return max(
                0.975 - sqrt(
                    (-diff.toDouble()).pow(3.0) / 2.0
                ) * 0.0001, 0.75
            )
        }

        return 1.0
    }

    fun calcCounterAttack(
        attacker: Creature,
        target: Creature,
        skill: Skill,
        crit: Boolean
    ) {
        // Only melee skills can be reflected
        if (skill.isMagic || skill.castRange > MELEE_ATTACK_RANGE) {
            return
        }

        /* need ?
        if (skill.hasEffects(EffectUseType.NORMAL) || !skill.isDebuff) {
            return
        }*/

        val chance = target.stat.getValue(DoubleStat.VENGEANCE_SKILL_PHYSICAL_DAMAGE, 0.0)
        if (Rnd.get(100) < chance) {
            if (target.isPlayer) {
                val msg = SystemMessagePacket(SystemMsg.YOU_COUNTERED_C1S_ATTACK)
                target.sendPacket(msg.addName(attacker))
            }
            if (attacker.isPlayer) {
                val msg = SystemMessagePacket(SystemMsg.C1_IS_PERFORMING_A_COUNTERATTACK)
                attacker.sendPacket(msg.addName(target))
            }

            var counterdmg =
                (target.stat.getPAtk() * 873 / attacker.stat.getPDef()).toDouble() // Old: (((target.getPAtk(attacker) * 10.0) * 70.0) / attacker.getPDef(target));
            counterdmg *= calcWeaponTraitBonus(attacker, target)
            counterdmg *= calcGeneralTraitBonus(attacker, target, skill.traitType, true)
            counterdmg *= calcAttributeBonus(attacker, target, skill)

            attacker.reduceCurrentHp(
                counterdmg, target, skill, true,
                true, false, false,
                false, false, true
            )
        }
    }

    /**
     * Calculate buff/debuff reflection.
     * @param target
     * @param skill
     * @return `true` if reflect, `false` otherwise.
     */
    fun calcBuffDebuffReflection(target: Creature, skill: Skill): Boolean {
        return when {
            !skill.isDebuff || skill.activateRate == -1 -> false
            else -> {
                val stat = when {
                    skill.isMagic -> DoubleStat.REFLECT_SKILL_MAGIC
                    else -> DoubleStat.REFLECT_SKILL_PHYSIC
                }
                target.stat.getValue(stat, 0.0) > Rnd.get(100)
            }
        }
    }

    /**
     * Calculate damage caused by falling
     * @param cha
     * @param fallHeight
     * @return damage
     */
    fun calcFallDam(cha: Creature, fallHeight: Double): Double {
        return if (fallHeight < 0) {
            0.0
        } else {
            cha.stat.getValue(DoubleStat.FALL, fallHeight * cha.maxHp / 1000.0)
        }
    }

    fun calcPhysicalSkillEvasion(activeChar: Creature, target: Creature, skill: Skill): Boolean {
        val chanceToEvade = target.stat.getSkillEvasionTypeValue(skill.magicType).toInt()
        if (Rnd.chance(chanceToEvade)) {
            if (activeChar.isPlayer) {
                val sm = SystemMessagePacket(SystemMsg.C1_DODGED_THE_ATTACK)
                sm.addName(target)
                activeChar.sendPacket(sm)
            }
            if (target.isPlayer) {
                val sm = SystemMessagePacket(SystemMsg.YOU_HAVE_DODGED_C1S_ATTACK)
                sm.addName(activeChar)
                target.sendPacket(sm)
            }

            return true
        }

        return false
    }

    /**
     * Basic chance formula:<br></br>
     *
     *  * chance = weapon_critical * dex_bonus * crit_height_bonus * crit_pos_bonus * effect_bonus * fatal_blow_rate
     *  * weapon_critical = (12 for daggers)
     *  * dex_bonus = dex modifier bonus for current dex (Seems unused in GOD, so its not used in formula).
     *  * crit_height_bonus = (z_diff * 4 / 5 + 10) / 100 + 1 or alternatively (z_diff * 0.008) + 1.1. Be aware of z_diff constraint of -25 to 25.
     *  * crit_pos_bonus = crit_pos(front = 1, side = 1.1, back = 1.3) * p_critical_rate_position_bonus
     *  * effect_bonus = (p2 + 100) / 100, p2 - 2nd param of effect. Blow chance of effect.
     *
     * Chance cannot be higher than 80%.
     * @param activeChar
     * @param target
     * @param skill
     * @param chanceBoost
     * @return
     */
    fun calcBlowSuccess(
        activeChar: Creature,
        target: Creature,
        skill: Skill,
        chanceBoost: Double
    ): Boolean {
        val weapon = activeChar.activeWeaponTemplate
        val weaponCritical: Double = if (weapon != null) {
            weapon.getStat(DoubleStat.CRITICAL_RATE)
        } else {
            activeChar.template.basePCritRate
        }.orElse(0.0)

        // double dexBonus = BaseStats.DEX.calcBonus(activeChar); Not used in GOD
        val critHeightBonus = calcCriticalHeightBonus(activeChar, target)
        // 30% chance from back, 10% chance from side. Include buffs that give positional crit rate.
        val criticalPosition: Double = calcCriticalPositionBonus(activeChar, target)
        val chanceBoostMod = (100 + chanceBoost) / 100.0
        val blowRateMod = activeChar.stat.getValue(DoubleStat.BLOW_RATE, 1.0)

        val rate = criticalPosition * critHeightBonus * weaponCritical * chanceBoostMod * blowRateMod

        // Debug
        if (activeChar.isDebug) {
            val set = StatsSet()
            set.set("criticalPosition", criticalPosition)
            set.set("critHeightBonus", critHeightBonus)
            set.set("weaponCritical", weaponCritical)
            set.set("blowChance", chanceBoost)
            set.set("blowRate", blowRateMod)
            set.set("rate(max 80 of 100%)", rate)
            Debug.sendSkillDebug(activeChar, target, skill, set)
        }

        // Blow rate is capped at 80%
        return Rnd.get(100) < min(rate, 80.0)
    }

    fun calcCancelStealEffects(
            activeChar: Creature,
            target: Creature,
            skill: Skill,
            slot: DispelSlotType,
            rate: Int,
            max: Int
    ): List<Abnormal> {
        val canceled = ArrayList<Abnormal>(max)
        when (slot) {
            DispelSlotType.SLOT_BUFF -> {
                // Resist Modifier.
                val cancelMagicLvl = skill.magicLevel

                if (activeChar.isDebug) {
                    val set = StatsSet()
                    set.set("baseMod", rate)
                    set.set("magicLevel", cancelMagicLvl)
                    set.set("resMod", target.stat.getValue(DoubleStat.RESIST_DISPEL_BUFF, 1.0))
                    set.set("rate", rate)
                    Debug.sendSkillDebug(activeChar, target, skill, set)
                }

                // Prevent initialization. reverse order
                val buffs = target.abnormalList
                        .filter { it.skill.buffType == SkillBuffType.BUFF }
                        .reversed()

                for (buff in buffs)
                {
                    if (!buff.skill.canBeStolen()) {
                        continue
                    }
                    if (rate < 100 && !calcCancelSuccess(buff, cancelMagicLvl, rate, skill, target)) {
                        continue
                    }

                    canceled.add(buff)
                    if (canceled.size >= max) {
                        break
                    }
                }
            }
            DispelSlotType.SLOT_DEBUFF -> {
                val debuffs = target.abnormalList
                        .filter { it.skill.isDebuff }
                        .reversed()
                for (debuff in debuffs) {
                    if (debuff.skill.canBeDispelled() && Rnd.get(100) <= rate) {
                        canceled.add(debuff)
                        if (canceled.size >= max) {
                            break
                        }
                    }
                }
            }
        }

        return canceled
    }

    fun calcCancelSuccess(info: Abnormal, cancelMagicLvl: Int, rate: Int, skill: Skill, target: Creature): Boolean {
        val resistMod = when {
            info.skill.isDebuff -> {
                target.stat.getValue(DoubleStat.RESIST_DISPEL_DEBUFF, 1.0)
            }
            !info.skill.isDebuff -> {
                target.stat.getValue(DoubleStat.RESIST_DISPEL_BUFF, 1.0)
            }
            else -> 1.0
        }
        val resistModAll = target.stat.getValue(DoubleStat.RESIST_DISPEL_ALL, 1.0)
        val chance = (rate + (cancelMagicLvl - info.skill.magicLevel) * 2 + info.duration / 120 * resistMod * resistModAll).toInt()
        return Rnd.get(100) < chance.constrain(25, 75) // TODO: i_dispel_by_slot_probability min = 40, max = 95.
    }

    /**
     * Calculates the abnormal time for an effect.<br></br>
     * The abnormal time is taken from the skill definition, and it's global for all effects present in the skills.
     * @param caster the caster
     * @param target the target
     * @param skill the skill
     * @return the time that the effect will last
     */
    fun calcEffectAbnormalTime(skill: Skill?): Int {
        return if (skill == null || skill.isPassive || skill.isToggle) {
            -1
        } else {
            skill.abnormalTime
        }
    }

    fun calcGeneralTraitBonus(
        attacker: Creature,
        target: Creature,
        traitType: TraitType,
        ignoreResistance: Boolean
    ): Double {
        if (traitType == TraitType.NONE) {
            return 1.0
        }

        if (target.stat.isInvulnerableTrait(traitType)) {
            return 0.0
        }

        when (traitType.type) {
            2 -> {
                if (!attacker.stat.hasAttackTrait(traitType) || !target.stat.hasDefenceTrait(traitType)) {
                    return 1.0
                }
            }
            3 -> {
                if (ignoreResistance) {
                    return 1.0
                }
            }
            else -> {
                return 1.0
            }
        }

        val result = attacker.stat.getAttackTrait(traitType) - target.stat.getDefenceTrait(traitType) + 1.0
        return MathUtils.constrain(result, 0.05, 2.0)
    }

    fun calcWeaponTraitBonus(attacker: Creature, target: Creature): Double {
        val result = target.stat.getDefenceTrait(attacker.attackType.trait) - 1.0
        return 1.0 - result
    }

    fun calcAttackTraitBonus(attacker: Creature, target: Creature): Double {
        val weaponTraitBonus = calcWeaponTraitBonus(attacker, target)
        if (weaponTraitBonus == 0.0) {
            return 0.0
        }

        var weaknessBonus = 1.0
        for (traitType in TraitType.values()) {
            if (traitType.type == 2) {
                weaknessBonus *= calcGeneralTraitBonus(attacker, target, traitType, true)
                if (weaknessBonus == 0.0) {
                    return 0.0
                }
            }
        }

        return (weaponTraitBonus * weaknessBonus).constrain(0.05, 2.0)
    }

    fun getBasicPropertyResistBonus(basicProperty: BasicProperty, target: Creature): Double {
        if (basicProperty === BasicProperty.NONE || !target.hasBasicPropertyResist()) {
            return 1.0
        }

        val resist = target.getBasicPropertyResist(basicProperty)
        when (resist.resistLevel) {
            0 -> return 1.0
            1 -> return 0.6
            2 -> return 0.3
            else -> return 0.0
        }
    }

    /**
     * Calculated damage caused by ATTACK of attacker on target.
     * @param attacker player or NPC that makes ATTACK
     * @param target player or NPC, target of ATTACK
     * @param shld
     * @param crit if the ATTACK have critical success
     * @param ss if weapon item was charged by soulshot
     * @return
     */
    fun calcAutoAttackDamage(attacker: Creature, target: Creature, shld: Byte, crit: Boolean, ss: Boolean): Double {
        if (!attacker.isInRadius3d(target, target.stat.getValue(DoubleStat.SPHERIC_BARRIER_RANGE, java.lang.Double.MAX_VALUE))) {
            return 0.0
        }

        // DEFENCE CALCULATION (pDef + sDef)
        var defence = target.stat.getPDef()

        when (shld) {
            SHIELD_DEFENSE_SUCCEED -> {
                defence += target.shldDef
            }
            SHIELD_DEFENSE_PERFECT_BLOCK -> {
                return 1.0
            }
        }

        val weapon = attacker.activeWeaponInstance
        val isRanged = weapon != null && weapon.itemType.isRanged()
        val shotsBonus = attacker.stat.getValue(DoubleStat.SOULSHOTS_BONUS)

        val cAtk = if (crit) 2.0 * calcCritDamage(attacker, target, null) else 1.0
        val cAtkAdd = if (crit) calcCritDamageAdd(attacker, target, null) else 0.0
        val critMod = if (crit) 1.0 else 0.0
        val ssBonus = if (ss) 2.0 * shotsBonus else 1.0
        val random_damage = attacker.randomDamageMultiplier
        val proxBonus = (when {
            attacker.isInFrontOf(target) -> 0.0
            attacker.isBehind(target) -> 0.2
            else -> 0.05
        }) * attacker.stat.getPAtk()
        val weaponMod = if (isRanged) 70.0 else 77.0
        var attack = attacker.stat.getPAtk() * random_damage + proxBonus

        // ....................______________Critical Section___________________...._______Non-Critical Section______
        // ATTACK CALCULATION (((pAtk * cAtk * ss + cAtkAdd) * crit) * weaponMod) + (pAtk (1 - crit) * ss * weaponMod)
        // ````````````````````^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^````^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
        attack = (attack * cAtk * ssBonus + cAtkAdd) * critMod * weaponMod + attack * (1 - critMod) * ssBonus * weaponMod

        // DAMAGE CALCULATION (ATTACK / DEFENCE) * trait bonus * attr bonus * pvp bonus * pve bonus
        var damage = attack / defence
        damage *= calcAttackTraitBonus(attacker, target)
        damage *= calcAttributeBonus(attacker, target, null)
        damage *= calculatePvpPveBonus(attacker, target, null, crit)

        damage = max(0.0, damage)

        return damage
    }

    /**
     * Calculate Probability in following effects:<br></br>
     * TargetCancel,<br></br>
     * TargetMeProbability,<br></br>
     * SkillTurning,<br></br>
     * Betray,<br></br>
     * Bluff,<br></br>
     * DeleteHate,<br></br>
     * RandomizeHate,<br></br>
     * DeleteHateOfMe,<br></br>
     * TransferHate,<br></br>
     * Confuse<br></br>
     * Knockback<br></br>
     * Pull<br></br>
     * @param baseChance chance from effect parameter
     * @param attacker
     * @param target
     * @param skill
     * @return chance for effect to succeed
     */
    fun calcProbability(
        baseChance: Double,
        attacker: Creature,
        target: Creature,
        skill: Skill
    ): Boolean {
        // Skills without set probability should only test against trait invulnerability.
        if (baseChance.isNaN()) {
            return calcGeneralTraitBonus(attacker, target, skill.traitType, true) > 0
        }

        // Outdated formula: return Rnd.get(100) < ((((((skill.getMagicLevel() + baseChance) - target.getLevel()) + 30) - target.getINT()) * calcAttributeBonus(attacker, target, skill)) * calcGeneralTraitBonus(attacker, target, skill.getTraitType(), false));
        // TODO: Find more retail-like formula
        val abnormalResist = getAbnormalResist(skill.basicProperty, target)
        val attributeBonus = calcAttributeBonus(attacker, target, skill)
        val traitBonus = calcGeneralTraitBonus(attacker, target, skill.traitType, false)
        return Rnd.get(100) < (skill.magicLevel + baseChance - target.level - abnormalResist) * attributeBonus * traitBonus
    }

    fun getAbnormalResist(basicProperty: BasicProperty, target: Creature): Double {
        return when (basicProperty) {
            BasicProperty.PHYSICAL_ABNORMAL_RESIST -> {
                target.stat.getValue(DoubleStat.ABNORMAL_RESIST_PHYSICAL)
            }
            BasicProperty.MAGIC_ABNORMAL_RESIST -> {
                target.stat.getValue(DoubleStat.ABNORMAL_RESIST_MAGICAL)
            }
            else -> {
                0.0
            }
        }
    }

    fun calcPveDamagePenalty(attacker: Creature, target: Creature, skill: Skill?, crit: Boolean): Double {
        if (!target.isMonster || target.level < MIN_NPC_LVL_DMG_PENALTY) {
            return 1.0
        }

        val attackerPlayer = attacker.player
        if (attackerPlayer == null || target.level - attackerPlayer.level <= 1) {
            return 1.0
        }

        val lvlDiff: Int = target.level - attackerPlayer.level - 1
        val map = when {
            skill != null -> NPC_SKILL_DMG_PENALTY
            crit -> NPC_CRIT_DMG_PENALTY
            else -> NPC_DMG_PENALTY
        }

        val key = min(lvlDiff, map.size - 1)
        return map.getOrDefault(key, 0.0)
    }

    /**
     * Calculates if the specified creature can get its stun effect removed due to damage taken.
     * @param activeChar the character to be checked
     * @return `true` if character should get its stun effects removed, `false` otherwise.
     */
    fun calcStunBreak(activeChar: Creature): Boolean {
        // Check if target is stunned and 10% chance (retail is 14% and 35% on crit?)
        return if (activeChar.flags.hasBlockActions() && Rnd.chance(10)) {
            // Any stun that has double duration due to skill mastery, doesn't get removed until its time reaches the usual abnormal time.
            activeChar.abnormalList.contains(AbnormalType.STUN)
        } else false
    }

    fun calcRealTargetBreak(): Boolean {
        // Real Target breaks at 3% (Rnd > 3.0 doesn't break) probability.
        return Rnd.chance(3)
    }

    /**
     * @param attackSpeed the attack speed of the Creature.
     * @return `500000 / attackSpeed`.
     */
    fun calculateTimeBetweenAttacks(attackSpeed: Int): Int {
        // Measured Nov 2015 by Nik. Formula: atk.spd/500 = hits per second.
        return max(50, 500000 / attackSpeed)
    }

    /**
     * @param totalAttackTime the time needed to make a full attack.
     * @param attackType the weapon type used for attack.
     * @param secondHit calculates the second hit for dual attacks.
     * @return the time required from the start of the attack until you hit the target.
     */
    fun calculateTimeToHit(totalAttackTime: Int, attackType: WeaponType, twoHanded: Boolean, secondHit: Boolean): Int {
        // Gracia Final Retail confirmed:
        // Time to damage (1 hand, 1 hit): TotalBasicAttackTime * 0.644
        // Time to damage (2 hand, 1 hit): TotalBasicAttackTime * 0.735
        // Time to damage (2 hand, 2 hit): TotalBasicAttackTime * 0.2726 and TotalBasicAttackTime * 0.6
        // Time to damage (bow/xbow): TotalBasicAttackTime * 0.978

        // Measured July 2016 by Nik.
        // Due to retail packet delay, we are unable to gather too accurate results. Therefore the below formulas are based on original Gracia Final values.
        // Any original values that appear higher than tested have been replaced with the tested values, because even with packet delay its obvious they are wrong.
        // All other original values are compared with the test results and differences are considered to be too insignificant and mostly caused due to packet delay.
        return when (attackType) {
            // Bows
            WeaponType.BOW, WeaponType.CROSSBOW, WeaponType.TWOHANDCROSSBOW -> {
                (totalAttackTime * 0.95).toInt()
            }
            // Dual weapons
            WeaponType.DUALBLUNT, WeaponType.DUALDAGGER, WeaponType.DUAL, WeaponType.DUALFIST -> {
                if (secondHit) {
                    (totalAttackTime * 0.6).toInt()
                } else {
                    (totalAttackTime * 0.2726).toInt()
                }
            }
            else -> {
                // Two-hand weapons
                if (twoHanded) {
                    (totalAttackTime * 0.735).toInt()
                } else {
                    (totalAttackTime * 0.644).toInt() // One-hand weapons
                }
            }
        }
    }

    fun calculatePvpPveBonus(
        attacker: Creature,
        target: Creature,
        skill: Skill?,
        crit: Boolean
    ): Double { // PvP bonus
        if (attacker.isPlayable && target.isPlayable) {
            val pvpAttack: Double
            val pvpDefense: Double
            if (skill != null) {
                if (skill.isMagic) {
                    // Magical Skill PvP
                    pvpAttack = attacker.stat.getValue(DoubleStat.PVP_MAGICAL_SKILL_DAMAGE, 1.0)
                    pvpDefense = target.stat.getValue(DoubleStat.PVP_MAGICAL_SKILL_DEFENCE, 1.0)
                } else {
                    // Physical Skill PvP
                    pvpAttack = attacker.stat.getValue(DoubleStat.PVP_PHYSICAL_SKILL_DAMAGE, 1.0)
                    pvpDefense = target.stat.getValue(DoubleStat.PVP_PHYSICAL_SKILL_DEFENCE, 1.0)
                }
            } else {
                // Autoattack PvP
                pvpAttack = attacker.stat.getValue(DoubleStat.PVP_PHYSICAL_ATTACK_DAMAGE, 1.0)
                pvpDefense = target.stat.getValue(DoubleStat.PVP_PHYSICAL_ATTACK_DEFENCE, 1.0)
            }
            return 1 + (pvpAttack - pvpDefense)
        }

        // PvE Bonus
        if (target.isMonster || attacker.isMonster) {
            val pveAttack: Double
            val pveDefense: Double
            val pvePenalty: Double = calcPveDamagePenalty(attacker, target, skill, crit)
            val isRaid = attacker.isRaid || target.isRaid
            if (skill != null) {
                if (skill.isMagic) { // Magical Skill PvE
                    pveAttack = if (isRaid) {
                        attacker.stat.getValue(DoubleStat.PVE_RAID_MAGICAL_SKILL_DAMAGE, 1.0)
                    } else {
                        attacker.stat.getValue(DoubleStat.PVE_MAGICAL_SKILL_DAMAGE, 1.0)
                    }
                    pveDefense = if (isRaid) {
                        target.stat.getValue(DoubleStat.PVE_RAID_MAGICAL_SKILL_DEFENCE, 1.0)
                    } else {
                        target.stat.getValue(DoubleStat.PVE_MAGICAL_SKILL_DEFENCE, 1.0)
                    }
                } else { // Physical Skill PvE
                    pveAttack = if (isRaid) {
                        attacker.stat.getValue(DoubleStat.PVE_RAID_PHYSICAL_SKILL_DAMAGE, 1.0)
                    } else {
                        attacker.stat.getValue(DoubleStat.PVE_PHYSICAL_SKILL_DAMAGE, 1.0)
                    }
                    pveDefense = if (isRaid) {
                        target.stat.getValue(DoubleStat.PVE_RAID_PHYSICAL_SKILL_DEFENCE, 1.0)
                    } else {
                        target.stat.getValue(DoubleStat.PVE_PHYSICAL_SKILL_DEFENCE, 1.0)
                    }
                }
            } else {
                // Autoattack PvE
                pveAttack = if (isRaid) {
                    attacker.stat.getValue(
                        DoubleStat.PVE_RAID_PHYSICAL_ATTACK_DAMAGE,
                        1.0
                    )
                } else {
                    attacker.stat.getValue(DoubleStat.PVE_PHYSICAL_ATTACK_DAMAGE, 1.0)
                }
                pveDefense = if (isRaid) {
                    target.stat.getValue(DoubleStat.PVE_RAID_PHYSICAL_ATTACK_DEFENCE, 1.0)
                } else {
                    target.stat.getValue(DoubleStat.PVE_PHYSICAL_ATTACK_DEFENCE, 1.0)
                }
            }

            return (1 + (pveAttack - pveDefense)) * pvePenalty
        }

        return 1.0
    }

    fun calcMagicSuccess(attacker: Creature, target: Creature, skill: Skill): Boolean {
        // FIXME: Fix this LevelMod Formula.
        val lvlDifference = target.level - when {
            skill.magicLevel > 0 -> skill.magicLevel
            else -> attacker.level
        }.toDouble()
        val lvlModifier = 1.3.pow(lvlDifference)
        var targetModifier = 1.0
        if (target.isMonster && !target.isRaid && !target.isRaidMinion && target.level >= MIN_NPC_LVL_MAGIC_PENALTY && attacker.isPlayer && target.level - attacker.level >= 3) {
            val lvlDiff = target.level - attacker.level - 2
            targetModifier = NPC_SKILL_CHANCE_PENALTY.getValue(lvlDiff)
        }
        // general magic resist
        val resModifier = target.stat.getValue(DoubleStat.MAGIC_SUCCESS_RES, 1.0)
        val rate = 100 - (lvlModifier * targetModifier * resModifier).toFloat().roundToInt()

        if (attacker.isDebug) {
            val set = StatsSet()
            set.set("lvlDifference", lvlDifference)
            set.set("lvlModifier", lvlModifier)
            set.set("resModifier", resModifier)
            set.set("targetModifier", targetModifier)
            set.set("rate", rate)
            Debug.sendSkillDebug(attacker, target, skill, set)
        }

        return Rnd.get(100) < rate
    }

    fun calcManaDam(attacker: Creature, target: Creature, skill: Skill, power: Double, shld: Byte, sps: Boolean, bss: Boolean, mcrit: Boolean, critLimit: Double): Double {
        // Formula: (SQR(M.Atk)*Power*(Target Max MP/97))/M.Def
        var mAtk = attacker.stat.getMAtk()
        var mDef = target.stat.getMDef()
        val mp = target.stat.getMaxMp()

        when (shld) {
            SHIELD_DEFENSE_SUCCEED -> mDef += target.shldDef
            SHIELD_DEFENSE_PERFECT_BLOCK -> return 1.0 // perfect block
        }

        // Bonus Spiritshot
        val shotsBonus = attacker.stat.getValue(DoubleStat.SPIRITSHOTS_BONUS)
        mAtk *= when {
            bss -> 4.0 * shotsBonus
            sps -> 2.0 * shotsBonus
            else -> 1.0
        }

        var damage = sqrt(mAtk) * power * (mp / 97.0) / mDef
        damage *= calcGeneralTraitBonus(attacker, target, skill.traitType, false)
        damage *= calculatePvpPveBonus(attacker, target, skill, mcrit)

        // Failure calculation
        if (/*PlayerConfig.ALT_GAME_MAGICFAILURES &&*/ !calcMagicSuccess(attacker, target, skill)) {
            if (attacker.isPlayer) {
                val sm = SystemMessagePacket(SystemMsg.DAMAGE_IS_DECREASED_BECAUSE_C1_RESISTED_C2S_MAGIC)
                sm.addName(target)
                sm.addName(attacker)
                attacker.sendPacket(sm)
                damage /= 2.0
            }

            if (target.isPlayer) {
                val sm2 = SystemMessagePacket(SystemMsg.C1_WEAKLY_RESISTED_C2S_MAGIC)
                sm2.addName(target)
                sm2.addName(attacker)
                target.sendPacket(sm2)
            }
        }

        if (mcrit) {
            damage *= 3.0
            damage = Math.min(damage, critLimit)
            attacker.sendPacket(SystemMsg.MAGIC_CRITICAL_HIT)
        }
        return damage
    }

    fun calculateSkillResurrectRestorePercent(baseRestorePercent: Double, caster: Creature): Double {
        if (baseRestorePercent == 0.0 || baseRestorePercent == 100.0) {
            return baseRestorePercent
        }

        var restorePercent = baseRestorePercent * BaseStats.WIT.calcBonus(caster)
        if (restorePercent - baseRestorePercent > 20.0) {
            restorePercent += 20.0
        }

        return restorePercent.constrain(baseRestorePercent, 90.0)
    }

    fun calcElementalCrit(
            attacker: Creature
    ): Boolean {
        val element = attacker.activeElement
        val critChance = attacker.stat.getElementalCritRate(element) / 100.0 + 1 * 10 // Примерно правильная формула: https://4gameforum.com/threads/710160/
        return Rnd.chance(critChance)
    }

    fun calcElementalDamage(
            attacker: Creature,
            target: Creature,
            skill: Skill?,
            crit: Boolean,
            useShot: Boolean
    ): Double {
        return calcElementalDamage(
                attacker,
                target,
                skill,
                skill?.getPower(target) ?: 0.0,
                crit,
                useShot
        )
    }

    fun calcElementalDamage(
            attacker: Creature,
            target: Creature,
            skill: Skill?,
            power: Double,
            crit: Boolean,
            useShot: Boolean
    ): Double {
        //TODO: Переделать по оффу.
        val element = attacker.activeElement
        if (element == ElementalElement.NONE) {
            return 0.0
        }

        var elementalDamagePower = attacker.stat.getElementalAttackPower(element) - target.stat.getElementalDefence(element) // Влияние защиты верно, судя по таблице: https://4gameforum.com/threads/709735/

        val targetElement = target.activeElement
        elementalDamagePower *= when {
            targetElement == ElementalElement.NONE || element == targetElement -> 1.0
            element.subordinate == targetElement -> 1.2
            element.dominant == targetElement -> 0.6
            else -> 0.8
        }

        var damage = 0.0
        if (skill == null) {
            damage = attacker.stat.getPAtk().toDouble()
            if (useShot)
                damage *= (100 + attacker.chargedSoulshotPower) / 100.0
            damage *= 35.0 / target.stat.getPDef()
            damage *= elementalDamagePower / 1000.0
        } else if (power > 0) {
            if (skill.isMagic) {
                var mAtk = attacker.stat.getMAtk()
                if (useShot)
                    mAtk *= (100 + attacker.chargedSpiritshotPower) / 100.0

                damage = 35.0 * power * sqrt(mAtk) / target.stat.getMDef()
                damage *= elementalDamagePower / 1000.0
            } else {
                damage = attacker.stat.getPAtk() + power
                if (useShot)
                    damage *= (100 + attacker.chargedSoulshotPower) / 100.0
                damage *= 35.0 / target.stat.getPDef()
                damage *= elementalDamagePower / 1000.0
            }
        }

        if (damage != 0.0) {
            if (crit) {
                val critMod = 1.0 + attacker.stat.getElementalCritAttack(element) * 0.4
                damage += damage * critMod // TODO: Прикинуто приблизительно: https://4gameforum.com/threads/715323/
            }
        }

        return damage
    }

    /**
     * Gets the condition bonus.
     * @param attacker the attacking character.
     * @param target the attacked character.
     * @return the bonus of the attacker against the target.
     */
    fun getConditionBonus(attacker: Creature, target: Creature): Double {
        var mod = 100.0
        // Get high or low bonus
        if (attacker.z - target.z > 50) {
            mod += HitCondBonusHolder.getInstance().getHitCondBonus(HitCondBonusType.HIGH)
        } else if (attacker.z - target.z < -50) {
            mod += HitCondBonusHolder.getInstance().getHitCondBonus(HitCondBonusType.LOW)
        }

        // Get weather bonus
        if (GameTimeController.getInstance().isNowNight) {
            mod += HitCondBonusHolder.getInstance().getHitCondBonus(HitCondBonusType.DARK)
        }

        // if () No rain support yet.
        // mod += HitCondBonusHolder.getInstance().getHitCondBonus(HitCondBonusType.RAIN);

        // Get side bonus
        mod += when (PositionUtils2.getPosition(attacker, target)) {
            Position.SIDE -> {
                HitCondBonusHolder.getInstance().getHitCondBonus(HitCondBonusType.SIDE)
            }
            Position.BACK -> {
                HitCondBonusHolder.getInstance().getHitCondBonus(HitCondBonusType.BACK)
            }
            else -> {
                HitCondBonusHolder.getInstance().getHitCondBonus(HitCondBonusType.AHEAD)
            }
        }

        // If (mod / 100) is less than 0, return 0, because we can't lower more than 100%.
        return max(mod / 100.0, 0.0)
    }

    fun calculateKarmaLost(player: Player, exp: Long): Int {
        if (Config.RATE_KARMA_LOST_STATIC != -1)
            return Config.RATE_KARMA_LOST_STATIC

        var expMod = exp.toDouble()
        val karmaLooseMul = KarmaIncreaseDataHolder.getInstance().getData(player.level)
        if (expMod > 0) {
            val modifier = when {
                Config.KARMA_RATE_KARMA_LOST == -1 -> Config.RATE_XP_BY_LVL[player.level]
                else -> Config.KARMA_RATE_KARMA_LOST.toDouble()
            }
            expMod /= modifier
        }
        return (abs(expMod) / karmaLooseMul/* / 15.0*/).toInt()
    }

}