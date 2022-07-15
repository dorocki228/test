package l2s.gameserver.stats

import com.google.common.flogger.FluentLogger
import l2s.commons.math.MathUtils
import l2s.gameserver.model.Creature
import l2s.gameserver.model.base.AttributeType
import l2s.gameserver.stats.calculators.*

enum class DoubleStat(
    s: String,
    private val statCalculator: StatCalculator = DefaultCalculator,
    /*@Deprecated("remove")
    private val _min: Double = 0.0,
    @Deprecated("remove")
    private val _max: Double = Double.POSITIVE_INFINITY,
    @Deprecated("remove")
    private val init: Double = 0.0,*/
    private val addFunction: (Double, Double) -> Double = MathUtils::add,
    private val mulFunction: (Double, Double) -> Double = MathUtils::mul,
    val resetAddValue: Double = 0.0,
    val resetMulValue: Double = 1.0
) {
    STAT_STR("STR", statCalculator = BaseStatCalculator),
    STAT_CON("CON", statCalculator = BaseStatCalculator),
    STAT_DEX("DEX", statCalculator = BaseStatCalculator),
    STAT_INT("INT", statCalculator = BaseStatCalculator),
    STAT_WIT("WIT", statCalculator = BaseStatCalculator),
    STAT_MEN("MEN", statCalculator = BaseStatCalculator),

    MAX_HP("maxHp", statCalculator = MaxHpCalculator),
    MAX_MP("maxMp", statCalculator = MaxMpCalculator),
    MAX_MP_ADD("maxMpAdd"),
    MAX_CP("maxCp", statCalculator = MaxCpCalculator),

    MAX_RECOVERABLE_HP("maxRecoverableHp"), // The maximum HP that is able to be recovered trough heals
    MAX_RECOVERABLE_MP("maxRecoverableMp"),
    MAX_RECOVERABLE_CP("maxRecoverableCp"),

    HP_REGEN("regHp", statCalculator = RegenerateHpRateCalculator),
    CP_REGEN("regCp", statCalculator = RegenerateCpRateCalculator),
    MP_REGEN("regMp", statCalculator = RegenerateMpRateCalculator),
    MP_REGEN_ADD("regMpAdd"),

    MANA_CHARGE("manaCharge"),
    HEAL_EFFECT("healEffect"),
    HEAL_EFFECT_POTIONS("healEffectPotions"),

    // ATTACK & DEFENCE
    PHYSICAL_DEFENCE("pDef", statCalculator = PhysicalDefenceCalculator),
    MAGICAL_DEFENCE("mDef", statCalculator = MagicalDefenceCalculator),
    PHYSICAL_ATTACK("pAtk", statCalculator = PhysicalAttackCalculator),
    MAGICAL_ATTACK("mAtk", statCalculator = MagicalAttackCalculator),
    MAGICAL_ATTACK_ADD("mAtkAdd"),
    @Deprecated("remove")
    BASE_PHYSICAL_ATTACK_SPEED("basePAtkSpd"),
    PHYSICAL_ATTACK_SPEED("pAtkSpd", statCalculator = PhysicalAttackSpeedCalculator),
    // Magic Skill Casting Time Rate
    MAGICAL_ATTACK_SPEED("mAtkSpd", statCalculator = MagicalAttackSpeedCalculator),

    // Bows Hits Reuse Rate
    ATK_REUSE("atkReuse"),
    SHIELD_DEFENCE("sDef", statCalculator = ShieldDefenceCalculator),
    CRITICAL_DAMAGE("critical_damage"),
    // this is another type for special critical damage mods - vicious stance, critical power and critical damage SA
    CRITICAL_DAMAGE_ADD("critical_damage_add"),
    HATE_ATTACK("attackHate"),

    // PVP BONUS
    PVP_PHYSICAL_ATTACK_DAMAGE("pvpPhysDmg"),
    PVP_MAGICAL_SKILL_DAMAGE("pvpMagicalDmg"),
    PVP_PHYSICAL_SKILL_DAMAGE("pvpPhysSkillsDmg"),
    PVP_PHYSICAL_ATTACK_DEFENCE("pvpPhysDef"),
    PVP_MAGICAL_SKILL_DEFENCE("pvpMagicalDef"),
    PVP_PHYSICAL_SKILL_DEFENCE("pvpPhysSkillsDef"),

    // PVE BONUS
    PVE_PHYSICAL_ATTACK_DAMAGE("pvePhysDmg"),
    PVE_PHYSICAL_SKILL_DAMAGE("pvePhysSkillsDmg"),
    PVE_MAGICAL_SKILL_DAMAGE("pveMagicalDmg"),
    PVE_PHYSICAL_ATTACK_DEFENCE("pvePhysDef"),
    PVE_PHYSICAL_SKILL_DEFENCE("pvePhysSkillsDef"),
    PVE_MAGICAL_SKILL_DEFENCE("pveMagicalDef"),
    PVE_RAID_PHYSICAL_ATTACK_DAMAGE("pveRaidPhysDmg"),
    PVE_RAID_PHYSICAL_SKILL_DAMAGE("pveRaidPhysSkillsDmg"),
    PVE_RAID_MAGICAL_SKILL_DAMAGE("pveRaidMagicalDmg"),
    PVE_RAID_PHYSICAL_ATTACK_DEFENCE("pveRaidPhysDef"),
    PVE_RAID_PHYSICAL_SKILL_DEFENCE("pveRaidPhysSkillsDef"),
    PVE_RAID_MAGICAL_SKILL_DEFENCE("pveRaidMagicalDef"),

    // FIXED BONUS
    PVP_DAMAGE_TAKEN("pvpDamageTaken"),
    PVE_DAMAGE_TAKEN("pveDamageTaken"),

    // ATTACK & DEFENCE RATES
    MAGIC_CRITICAL_DAMAGE("mCritPower"),
    MAGIC_CRITICAL_DAMAGE_ADD("mCritPowerAdd"),
    PHYSICAL_SKILL_POWER("physicalSkillPower"), // Adding skill power (not multipliers) results in points added directly to final value unmodified by defence, traits, elements, criticals etc.
    // Even when damage is 0 due to general trait immune multiplier, added skill power is active and clearly visible (damage not being 0 but at the value of added skill power).
    MAGICAL_SKILL_POWER("magicalSkillPower"),
    CRITICAL_DAMAGE_SKILL("cAtkSkill"),
    CRITICAL_DAMAGE_SKILL_ADD("cAtkSkillAdd"),
    SHIELD_DEFENCE_RATE("rShld", ShieldDefenceRateCalculator),
    @Deprecated("remove")
    BASE_PHYSICAL_CRITICAL_RATE("basePCritRate"),
    CRITICAL_RATE("rCrit", PhysicalCriticalRateCalculator, MathUtils::add, MathUtils::add, 0.0, 1.0),
    CRITICAL_RATE_SKILL("rCritSkill", DefaultCalculator, MathUtils::add, MathUtils::add, 0.0, 1.0),
    MAGIC_CRITICAL_RATE("mCritRate", MagicalCriticalRateCalculator),
    BLOW_RATE("blowRate"),

    DEFENCE_CRITICAL_RATE("defCritRate"),
    DEFENCE_CRITICAL_RATE_ADD("defCritRateAdd"),
    DEFENCE_MAGIC_CRITICAL_RATE("defMCritRate"),
    DEFENCE_MAGIC_CRITICAL_RATE_ADD("defMCritRateAdd"),
    DEFENCE_CRITICAL_DAMAGE("defCritDamage"),
    DEFENCE_CRITICAL_DAMAGE_ADD("defCritDamageAdd"), // Resistance to critical damage in value (Example: +100 will be 100 more critical damage, NOT 100% more).
    DEFENCE_MAGIC_CRITICAL_DAMAGE("defMCritDamage"),
    DEFENCE_MAGIC_CRITICAL_DAMAGE_ADD("defMCritDamageAdd"),
    DEFENCE_CRITICAL_DAMAGE_SKILL("defCAtkSkill"),
    DEFENCE_CRITICAL_DAMAGE_SKILL_ADD("defCAtkSkillAdd"), // TODO add effect

    INSTANT_KILL_RESIST("instantKillResist"),

    /* TODO
    EXPSP_RATE("rExp"),*/
    BONUS_EXP("bonusExp"),
    BONUS_SP("bonusSp"),
    BONUS_DROP("bonusDrop"),
    BONUS_SPOIL("bonusSpoil"),
    BONUS_ADENA("bonusAdena"),

    ATTACK_CANCEL("cancel"),

    // ACCURACY & RANGE
    ACCURACY_COMBAT("pAccCombat", PhysicalAccuracyCalculator),
    ACCURACY_MAGIC("accMagic", MagicalAccuracyCalculator),
    EVASION_RATE("pEvasRate", PhysicalEvasionRateCalculator),
    MAGIC_EVASION_RATE("mEvasRate", MagicalEvasionRateCalculator),
    PHYSICAL_ATTACK_RANGE("pAtkRange", PhysicalRangeCalculator),
    PHYSICAL_ATTACK_RADIUS("p_attack_radius", PhysicalRadiusCalculator),
    PHYSICAL_ATTACK_ANGLE("poleAngle", PhysicalAngleCalculator),
    MAGIC_ATTACK_RANGE("mAtkRange"),
    ATTACK_COUNT_MAX("attack_targets_count"),

    // Run speed, walk & escape speed are calculated proportionally, magic speed is a buff
    MOVE_SPEED("moveSpeed"/*, SpeedCalculator*/),
    RUN_SPEED("runSpd", SpeedCalculator),
    WALK_SPEED("walkSpd", SpeedCalculator),
    SWIM_RUN_SPEED("fastSwimSpd", SpeedCalculator),
    SWIM_WALK_SPEED("slowSimSpd", SpeedCalculator),
    FLY_RUN_SPEED("fastFlySpd", SpeedCalculator),
    FLY_WALK_SPEED("slowFlySpd", SpeedCalculator),
    STAT_BONUS_SPEED("statSpeed", DefaultCalculator, MathUtils::add, MathUtils::mul, -1.0, 1.0),

    // VARIOUS
    BREATH("breath", BreathCalculator),
    FALL("fall"),

    // VULNERABILITIES
    DAMAGE_ZONE_VULN("damageZoneVuln"),
    // Resistance for cancel type skills
    RESIST_DISPEL_ALL("RESIST_DISPEL_ALL"),
    RESIST_DISPEL_BUFF("RESIST_DISPEL_BUFF"),
    RESIST_DISPEL_DEBUFF("cancelVuln"),
    RESIST_ABNORMAL_BUFF("RESIST_ABNORMAL_BUFF"),
    RESIST_ABNORMAL_DEBUFF("resist_abnormal_debuff"),
    RESIST_ABNORMAL_MULTI_BUFF("RESIST_ABNORMAL_MULTI_BUFF"),

    // RESISTANCES
    DEFENCE_FIRE("defenceFire", AttributeCalculator(AttributeType.FIRE, false)),
    DEFENCE_WATER("defenceWater", AttributeCalculator(AttributeType.WATER, false)),
    DEFENCE_WIND("defenceWind", AttributeCalculator(AttributeType.WIND, false)),
    DEFENCE_EARTH("defenceEarth", AttributeCalculator(AttributeType.EARTH, false)),
    DEFENCE_HOLY("defenceHoly", AttributeCalculator(AttributeType.HOLY, false)),
    DEFENCE_UNHOLY("defenceUnholy", AttributeCalculator(AttributeType.UNHOLY, false)),
    BASE_ELEMENTS_DEFENCE("elements_defence"),

    // ELEMENT POWER
    ATTACK_FIRE("attackFire", AttributeCalculator(AttributeType.FIRE, true)),
    ATTACK_WATER("attackWater", AttributeCalculator(AttributeType.WATER, true)),
    ATTACK_WIND("attackWind", AttributeCalculator(AttributeType.WIND, true)),
    ATTACK_EARTH("attackEarth", AttributeCalculator(AttributeType.EARTH, true)),
    ATTACK_HOLY("attackHoly", AttributeCalculator(AttributeType.HOLY, true)),
    ATTACK_UNHOLY("attackUnholy", AttributeCalculator(AttributeType.UNHOLY, true)),

    MAGIC_SUCCESS_RES("magicSuccRes"),
    // BUFF_IMMUNITY("buffImmunity"), //TODO: Implement me
    ABNORMAL_RESIST_PHYSICAL("abnormalResPhysical"),
    ABNORMAL_RESIST_MAGICAL("abnormalResMagical"),

    // PROFICIENCY
    // Отражение урона в процентах. Урон получает и атакующий и цель
    REFLECT_DAMAGE_PERCENT("reflectDam"),
    REFLECT_DAMAGE_PERCENT_DEFENSE("reflectDamDef"),
    REFLECT_SKILL_MAGIC("reflectSkillMagic"),
    REFLECT_SKILL_PHYSIC("reflectSkillPhysic"),
    VENGEANCE_SKILL_MAGIC_DAMAGE("vengeanceMdam"),
    VENGEANCE_SKILL_PHYSICAL_DAMAGE("vengeancePdam"),
    ABSORB_DAMAGE_PERCENT("absorbDam"),
    ABSORB_DAMAGE_CHANCE("absorbDamChance", VampiricChanceCalculator),
    ABSORB_DAMAGE_DEFENCE("absorbDamDefence"),
    TRANSFER_DAMAGE_SUMMON_PERCENT("transDam"),
    MANA_SHIELD_PERCENT("manaShield"),
    TRANSFER_DAMAGE_TO_PLAYER("transDamToPlayer"),
    ABSORB_MANA_DAMAGE_PERCENT("absorbDamMana"),

    WEIGHT_LIMIT("weightLimit"),
    WEIGHT_PENALTY("weightPenalty"),

    // ExSkill
    INVENTORY_NORMAL("inventoryLimit"),
    STORAGE_PRIVATE("whLimit"),
    TRADE_SELL("PrivateSellLimit"),
    TRADE_BUY("PrivateBuyLimit"),
    RECIPE_DWARVEN("DwarfRecipeLimit"),
    RECIPE_COMMON("CommonRecipeLimit"),

    // Skill mastery not exist in classic
    // SKILL_CRITICAL("skillCritical", DefaultCalculator, MathUtils::add, MathUtils::mul, -1.0, 1.0),
    // SKILL_CRITICAL_PROBABILITY("skillCriticalProbability"),

    // Vitality
    // TODO VITALITY_POINTS_RATE("vitalityPointsRate"),
    // TODO VITALITY_EXP_RATE("vitalityExpRate"),

    // Souls
    // TODO MAX_SOULS("maxSouls"),

    REDUCE_EXP_LOST_BY_PVP("reduceExpLostByPvp"),
    REDUCE_EXP_LOST_BY_MOB("reduceExpLostByMob"),
    REDUCE_EXP_LOST_BY_RAID("reduceExpLostByRaid"),

    // TODO not exist in classic
    REDUCE_DEATH_PENALTY_BY_PVP("reduceDeathPenaltyByPvp"),
    REDUCE_DEATH_PENALTY_BY_MOB("reduceDeathPenaltyByMob"),
    REDUCE_DEATH_PENALTY_BY_RAID("reduceDeathPenaltyByRaid"),

    // Brooches
    BROOCH_JEWELS("broochJewels"),

    SECONDARY_AGATHION_SLOTS("sub_agathions_limit"),

    // TODO Summon Points not exist in classic
    // MAX_SUMMON_POINTS("summonPoints"),

    // Cubic Count
    MAX_CUBIC("cubicCount"),

    // The maximum allowed range to be damaged/debuffed from.
    SPHERIC_BARRIER_RANGE("sphericBarrier"),

    // Blocks given amount of debuffs.
    // TODO DEBUFF_BLOCK("debuffBlock"),

    // Affects the random weapon damage.
    RANDOM_DAMAGE("randomDamage", RandomDamageCalculator),

    // Affects the random weapon damage.
    DAMAGE_LIMIT("damageCap"),

    // Maximun momentum one can charge
    MAX_MOMENTUM("maxMomentum"),

    // Which base stat ordinal should alter skill critical formula.
    STAT_BONUS_SKILL_CRITICAL("statSkillCritical", DefaultCalculator,
        MathUtils::add, MathUtils::mul, -1.0, 1.0),

    // TODO CRAFTING_CRITICAL("craftingCritical"),
    SOULSHOTS_BONUS("soulshotBonus", SoulshotsBonusCalculator),
    SPIRITSHOTS_BONUS("spiritshotBonus", SpiritshotsBonusCalculator),
    // TODO BEAST_SOULSHOTS_BONUS("beastSoulshotBonus"),
    WORLD_CHAT_POINTS("worldChatPoints"),
    ATTACK_DAMAGE("attackDamage"),

    AVOID_AGGRO("AVOID_AGGRO"),

    // elemental stats

    FIRE_ELEMENTAL_EXP_RATE("fire_elemental_exp_rate"),
    WATER_ELEMENTAL_EXP_RATE("water_elemental_exp_rate"),
    WIND_ELEMENTAL_EXP_RATE("wind_elemental_exp_rate"),
    EARTH_ELEMENTAL_EXP_RATE("earth_elemental_exp_rate"),

    FIRE_ELEMENTAL_ATTACK("fire_elemental_attack"),
    WATER_ELEMENTAL_ATTACK("water_elemental_attack"),
    WIND_ELEMENTAL_ATTACK("wind_elemental_attack"),
    EARTH_ELEMENTAL_ATTACK("earth_elemental_attack"),

    FIRE_ELEMENTAL_DEFENCE("fire_elemental_defence"),
    WATER_ELEMENTAL_DEFENCE("water_elemental_defence"),
    WIND_ELEMENTAL_DEFENCE("wind_elemental_defence"),
    EARTH_ELEMENTAL_DEFENCE("earth_elemental_defence"),

    FIRE_ELEMENTAL_CRIT_RATE("fire_elemental_crit_rate"),
    WATER_ELEMENTAL_CRIT_RATE("water_elemental_crit_rate"),
    WIND_ELEMENTAL_CRIT_RATE("wind_elemental_crit_rate"),
    EARTH_ELEMENTAL_CRIT_RATE("earth_elemental_crit_rate"),

    FIRE_ELEMENTAL_CRIT_ATTACK("fire_elemental_crit_attack"),
    WATER_ELEMENTAL_CRIT_ATTACK("water_elemental_crit_attack"),
    WIND_ELEMENTAL_CRIT_ATTACK("wind_elemental_crit_attack"),
    EARTH_ELEMENTAL_CRIT_ATTACK("earth_elemental_crit_attack"),

    // old

    EXP_LOST("expLost"),
    ITEMS_LOST_CHANCE("items_lost_chance"/*, 0.0, 100.0*/),

    DEATH_VULNERABILITY("deathVuln"/*, 10.0, 190.0, 100.0*/),

    DROP_CHANCE_MODIFIER("drop_chance_modifier"),
    DROP_COUNT_MODIFIER("drop_count_modifier"),
    SPOIL_CHANCE_MODIFIER("spoil_chance_modifier"),
    SPOIL_COUNT_MODIFIER("spoil_count_modifier"),

    ENCHANT_CHANCE_MODIFIER("enchant_chance_modifier"/*, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0*/),

    CRAFT_CHANCE_BONUS("craft_chance_bonus"/*, 0.0, 100.0, 0.0*/),
    CRAFT_CRITICAL_CREATION_CHANCE("craft_critical_creation_chance"/*, 0.0, 100.0, 0.0*/),

    SOULSHOT_POWER("soulshot_power"/*, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY*/),
    SPIRITSHOT_POWER("spiritshot_power"/*, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY*/),

    DAMAGE_BLOCK_RADIUS("damage_block_radius"/*, -1.0, Double.POSITIVE_INFINITY, -1.0*/),

    DAMAGE_BLOCK_COUNT("damage_block_count"/*, 0.0, Double.POSITIVE_INFINITY*/);

    val value: String = s.toUpperCase()

    /**
     * @param creature
     * @param baseValue
     * @return the final value
     */
    fun calculate(
            creature: Creature,
            calculationType: CalculationType,
            baseValue: Double?
    ): Double {
        return try {
            statCalculator.calc(creature, this, calculationType, baseValue)
        } catch (e: Exception) {
            logger.atWarning().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e)
                .log(
                    "Exception during finalization for : %s stat: %s.",
                    creature, toString()
                )

            DefaultCalculator.calc(creature, this, calculationType, baseValue)
        }
    }

    fun add(oldValue: Double, value: Double): Double {
        return addFunction(oldValue, value)
    }

    fun mul(oldValue: Double, value: Double): Double {
        return mulFunction(oldValue, value)
    }

    companion object {

        private val logger = FluentLogger.forEnclosingClass()

        val VALUES = values()
        @JvmField
        val NUM_STATS = VALUES.size

        fun find(name: String): DoubleStat {
            val upperCaseName = name.toUpperCase()
            val result = VALUES.find { it.value == upperCaseName } ?: VALUES.find { it.name == upperCaseName }
            if (result != null) {
                return result
            }

            // TODO rework
            return when (name) {
                "pattack" -> PHYSICAL_ATTACK
                "mattack" -> MAGICAL_ATTACK
                "pdefend" -> PHYSICAL_DEFENCE
                "mdefend" -> MAGICAL_DEFENCE
                "hp" -> MAX_HP
                "mp" -> MAX_MP
                "pattackspeed" -> PHYSICAL_ATTACK_SPEED
                "musespeed" -> MAGICAL_ATTACK_SPEED
                "criticalprob" -> CRITICAL_RATE
                else -> error("Unknown name '$name' for enum DoubleStat")
            }
        }

        fun weaponBaseValue(creature: Creature, stat: DoubleStat): Double {
            return stat.statCalculator.calcWeaponBaseValue(creature, stat)
        }

    }
}