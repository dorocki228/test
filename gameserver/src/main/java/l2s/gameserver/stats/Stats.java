package l2s.gameserver.stats;

import l2s.gameserver.Config;

import java.util.NoSuchElementException;

public enum Stats
{
	MAX_HP("maxHp", 0.0, Double.POSITIVE_INFINITY, 1.0),
	MAX_MP("maxMp", 0.0, Double.POSITIVE_INFINITY, 1.0),
	MAX_CP("maxCp", 0.0, Double.POSITIVE_INFINITY, 1.0),
	REGENERATE_HP_RATE("regHp"),
	REGENERATE_CP_RATE("regCp"),
	REGENERATE_MP_RATE("regMp"),
	HP_LIMIT("hpLimit", 1.0, 100.0, 100.0),
	MP_LIMIT("mpLimit", 1.0, 100.0, 100.0),
	CP_LIMIT("cpLimit", 1.0, 100.0, 100.0),
	RUN_SPEED("runSpd"),
	BLOW_RESIST("blow_resist", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	BLOW_POWER("blow_power", -200.0, 200.0),

	POWER_DEFENCE("pDef"),
	MAGIC_DEFENCE("mDef"),
	POWER_ATTACK("pAtk"),
	MAGIC_ATTACK("mAtk"),
	POWER_ATTACK_SPEED("pAtkSpd"),
	MAGIC_ATTACK_SPEED("mAtkSpd"),
	MAGIC_REUSE_RATE("mReuse"),
	PHYSIC_REUSE_RATE("pReuse"),
	MUSIC_REUSE_RATE("musicReuse"),
	ATK_REUSE("atkReuse"),
	BASE_P_ATK_SPD("basePAtkSpd"),
	BASE_M_ATK_SPD("baseMAtkSpd"),
	P_EVASION_RATE("pEvasRate"),
	M_EVASION_RATE("mEvasRate"),
	P_ACCURACY_COMBAT("pAccCombat"),
	M_ACCURACY_COMBAT("mAccCombat"),
	BASE_P_CRITICAL_RATE("basePCritRate", 0.0, Double.POSITIVE_INFINITY),
	BASE_M_CRITICAL_RATE("baseMCritRate", 0.0, Double.POSITIVE_INFINITY),
	P_CRITICAL_RATE("pCritRate", 0.0, Double.POSITIVE_INFINITY, 100.0),
	M_CRITICAL_RATE("mCritRate", 0.0, Double.POSITIVE_INFINITY, 100.0),
	P_CRITICAL_DAMAGE("pCritDamage", 0.0, Double.POSITIVE_INFINITY, 100.0),
	M_CRITICAL_DAMAGE("mCritDamage", 0.0, Double.POSITIVE_INFINITY, 100.0),
	P_CRITICAL_DAMAGE_STATIC("pCritDamageStatic"),
	M_CRITICAL_DAMAGE_STATIC("mCritDamageStatic"),
	P_CRITICAL_DAMAGE_SKILL("pCritDamageSkill", 0.0, Double.POSITIVE_INFINITY, 100.0),
	M_CRITICAL_DAMAGE_SKILL("mCritDamageSkill", 0.0, Double.POSITIVE_INFINITY, 100.0),
	INFLICTS_P_DAMAGE_POWER("inflicts_p_damage_power"),
	INFLICTS_M_DAMAGE_POWER("inflicts_m_damage_power"),
	RECEIVE_P_DAMAGE_POWER("receive_p_damage_power"),
	RECEIVE_M_DAMAGE_POWER("receive_m_damage_power"),
	CAST_INTERRUPT("concentration", 0.0, 100.0),
	SHIELD_DEFENCE("sDef"),
	SHIELD_RATE("rShld", 0.0, 90.0),
	SHIELD_ANGLE("shldAngle", 0.0, 360.0, 60.0),
	POWER_ATTACK_RANGE("pAtkRange", 0.0, 1500.0),
	MAGIC_ATTACK_RANGE("mAtkRange", 0.0, 1500.0),
	POLE_ATTACK_ANGLE("poleAngle", 0.0, 180.0),
	POLE_TARGET_COUNT("poleTargetCount"),
	STAT_STR("STR", 1.0, 100.0),
	STAT_CON("CON", 1.0, 100.0),
	STAT_DEX("DEX", 1.0, 100.0),
	STAT_INT("INT", 1.0, 100.0),
	STAT_WIT("WIT", 1.0, 100.0),
	STAT_MEN("MEN", 1.0, 100.0),
	BREATH("breath"),
	FALL("fall"),
	EXP_LOST("expLost"),
	BLEED_RESIST("bleedResist", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	POISON_RESIST("poisonResist", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	STUN_RESIST("stunResist", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ROOT_RESIST("rootResist", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	MENTAL_RESIST("mentalResist", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	SLEEP_RESIST("sleepResist", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	PARALYZE_RESIST("paralyzeResist", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	CANCEL_RESIST("cancelResist", -200.0, 300.0),
	DEBUFF_RESIST("debuffResist", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	MAGIC_RESIST("magicResist", -200.0, 300.0),
	BLEED_POWER("bleedPower", -200.0, 200.0),
	POISON_POWER("poisonPower", -200.0, 200.0),
	STUN_POWER("stunPower", -200.0, 200.0),
	ROOT_POWER("rootPower", -200.0, 200.0),
	MENTAL_POWER("mentalPower", -200.0, 200.0),
	SLEEP_POWER("sleepPower", -200.0, 200.0),
	PARALYZE_POWER("paralyzePower", -200.0, 200.0),
	CANCEL_POWER("cancelPower", -200.0, 200.0),
	DEBUFF_POWER("debuffPower", -200.0, 200.0),
	MAGIC_POWER("magicPower", -200.0, 200.0),
	FATALBLOW_RATE("blowRate", 0.0, 10.0, 1.0),
	SKILL_CRIT_CHANCE_MOD("SkillCritChanceMod", 10.0, 190.0, 100.0),
	DEATH_VULNERABILITY("deathVuln", 10.0, 190.0, 100.0),
	P_CRIT_DAMAGE_RECEPTIVE("pCritDamRcpt", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 100.0),
	M_CRIT_DAMAGE_RECEPTIVE("mCritDamRcpt", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	P_CRIT_CHANCE_RECEPTIVE("pCritChanceRcpt", 10.0, 190.0, 100.0),
	M_CRIT_CHANCE_RECEPTIVE("mCritChanceRcpt", 10.0, 190.0, 100.0),
	DEFENCE_FIRE("defenceFire", -600.0, 600.0),
	DEFENCE_WATER("defenceWater", -600.0, 600.0),
	DEFENCE_WIND("defenceWind", -600.0, 600.0),
	DEFENCE_EARTH("defenceEarth", -600.0, 600.0),
	DEFENCE_HOLY("defenceHoly", -600.0, 600.0),
	DEFENCE_UNHOLY("defenceUnholy", -600.0, 600.0),
	ATTACK_FIRE("attackFire", 0.0, Config.ELEMENT_ATTACK_LIMIT),
	ATTACK_WATER("attackWater", 0.0, Config.ELEMENT_ATTACK_LIMIT),
	ATTACK_WIND("attackWind", 0.0, Config.ELEMENT_ATTACK_LIMIT),
	ATTACK_EARTH("attackEarth", 0.0, Config.ELEMENT_ATTACK_LIMIT),
	ATTACK_HOLY("attackHoly", 0.0, Config.ELEMENT_ATTACK_LIMIT),
	ATTACK_UNHOLY("attackUnholy", 0.0, Config.ELEMENT_ATTACK_LIMIT),
	SWORD_WPN_VULNERABILITY("swordWpnVuln", 10.0, 200.0, 100.0),
	DUAL_WPN_VULNERABILITY("dualWpnVuln", 10.0, 200.0, 100.0),
	BLUNT_WPN_VULNERABILITY("bluntWpnVuln", 10.0, 200.0, 100.0),
	DAGGER_WPN_VULNERABILITY("daggerWpnVuln", 10.0, 200.0, 100.0),
	BOW_WPN_VULNERABILITY("bowWpnVuln", 10.0, 200.0, 100.0),
	POLE_WPN_VULNERABILITY("poleWpnVuln", 10.0, 200.0, 100.0),
	FIST_WPN_VULNERABILITY("fistWpnVuln", 10.0, 200.0, 100.0),
	ABSORB_DAMAGE_PERCENT("absorbDam", 0.0, 100.0, 0.0),
	ABSORB_BOW_DAMAGE_PERCENT("absorbBowDam", 0.0, 100.0, 0.0),
	ABSORB_PSKILL_DAMAGE_PERCENT("absorbPSkillDam", 0.0, 100.0, 0.0),
	ABSORB_MSKILL_DAMAGE_PERCENT("absorbMSkillDam", 0.0, 100.0, 0.0),
	ABSORB_DAMAGEMP_PERCENT("absorbDamMp", 0.0, 100.0, 0.0),
	TRANSFER_TO_SUMMON_DAMAGE_PERCENT("transferPetDam", 0.0, 100.0),
	TRANSFER_TO_EFFECTOR_DAMAGE_PERCENT("transferToEffectorDam", 0.0, 100.0),
	TRANSFER_TO_MP_DAMAGE_PERCENT("p_mp_shield", 0.0, 100.0),
	REFLECT_AND_BLOCK_DAMAGE_CHANCE("reflectAndBlockDam", 0.0, Config.REFLECT_AND_BLOCK_DAMAGE_CHANCE_CAP),
	REFLECT_AND_BLOCK_PSKILL_DAMAGE_CHANCE("reflectAndBlockPSkillDam", 0.0, Config.REFLECT_AND_BLOCK_PSKILL_DAMAGE_CHANCE_CAP),
	REFLECT_AND_BLOCK_MSKILL_DAMAGE_CHANCE("reflectAndBlockMSkillDam", 0.0, Config.REFLECT_AND_BLOCK_MSKILL_DAMAGE_CHANCE_CAP),
	REFLECT_DAMAGE_PERCENT("reflectDam", 0.0, Config.REFLECT_DAMAGE_PERCENT_CAP),
	REFLECT_BOW_DAMAGE_PERCENT("reflectBowDam", 0.0, Config.REFLECT_BOW_DAMAGE_PERCENT_CAP),
	REFLECT_PSKILL_DAMAGE_PERCENT("reflectPSkillDam", 0.0, Config.REFLECT_PSKILL_DAMAGE_PERCENT_CAP),
	REFLECT_MSKILL_DAMAGE_PERCENT("reflectMSkillDam", 0.0, Config.REFLECT_MSKILL_DAMAGE_PERCENT_CAP),
	REFLECT_PHYSIC_SKILL("reflectPhysicSkill", 0.0, 60.0),
	REFLECT_MAGIC_SKILL("reflectMagicSkill", 0.0, 60.0),
	REFLECT_PHYSIC_DEBUFF("reflectPhysicDebuff", 0.0, 60.0),
	REFLECT_MAGIC_DEBUFF("reflectMagicDebuff", 0.0, 60.0),
	P_SKILL_EVASION("pSkillEvasion", 0.0, 100.0),
	COUNTER_ATTACK("counterAttack", 0.0, 100.0),
	P_SKILL_POWER("pSkillPower"),
	P_SKILL_POWER_STATIC("pSkillPowerStatic"),
	M_SKILL_POWER("mSkillPower"),
	CHARGED_P_SKILL_POWER("charged_p_skill_power"),
	PVP_PHYS_DMG_BONUS("pvpPhysDmgBonus"),
	PVP_PHYS_SKILL_DMG_BONUS("pvpPhysSkillDmgBonus"),
	PVP_MAGIC_SKILL_DMG_BONUS("pvpMagicSkillDmgBonus"),
	PVP_PHYS_DEFENCE_BONUS("pvpPhysDefenceBonus"),
	PVP_PHYS_SKILL_DEFENCE_BONUS("pvpPhysSkillDefenceBonus"),
	PVP_MAGIC_SKILL_DEFENCE_BONUS("pvpMagicSkillDefenceBonus"),
	PVE_PHYS_DMG_BONUS("pvePhysDmgBonus"),
	PVE_PHYS_SKILL_DMG_BONUS("pvePhysSkillDmgBonus"),
	PVE_MAGIC_SKILL_DMG_BONUS("pveMagicSkillDmgBonus"),
	PVE_PHYS_DEFENCE_BONUS("pvePhysDefenceBonus"),
	PVE_PHYS_SKILL_DEFENCE_BONUS("pvePhysSkillDefenceBonus"),
	PVE_MAGIC_SKILL_DEFENCE_BONUS("pveMagicSkillDefenceBonus"),
	HEAL_EFFECTIVNESS("hpEff", 0.0, 1000.0),
	MANAHEAL_EFFECTIVNESS("mpEff", 0.0, 1000.0),
	CPHEAL_EFFECTIVNESS("cpEff", 0.0, 1000.0),
	HEAL_POWER("healPower"),
	MP_MAGIC_SKILL_CONSUME("mpConsum"),
	MP_PHYSICAL_SKILL_CONSUME("mpConsumePhysical"),
	MP_DANCE_SKILL_CONSUME("mpDanceConsume"),
	CHEAP_SHOT("cheap_shot"),
	MAX_LOAD("maxLoad"),
	MAX_NO_PENALTY_LOAD("maxNoPenaltyLoad"),
	INVENTORY_LIMIT("inventoryLimit"),
	STORAGE_LIMIT("storageLimit"),
	TRADE_LIMIT("tradeLimit"),
	COMMON_RECIPE_LIMIT("CommonRecipeLimit"),
	DWARVEN_RECIPE_LIMIT("DwarvenRecipeLimit"),
	BUFF_LIMIT("buffLimit"),
	SOULS_LIMIT("soulsLimit"),
	SOULS_CONSUME_EXP("soulsExp"),
	TALISMANS_LIMIT("talismansLimit", 0.0, 6.0),
	JEWELS_LIMIT("jewels_limit", 0.0, 6.0),
	CUBICS_LIMIT("cubicsLimit", 0.0, 3.0, 1.0),
	GRADE_EXPERTISE_LEVEL("gradeExpertiseLevel"),
	EXP_RATE_MULTIPLIER("exp_rate_multiplier", -1.0, 100.0),
	SP_RATE_MULTIPLIER("sp_rate_multiplier", -1.0, 100.0),
	ADENA_RATE_MULTIPLIER("adena_rate_multiplier", -1.0, 100.0),
	DROP_RATE_MULTIPLIER("drop_rate_multiplier", -1.0, 100.0),
	SPOIL_RATE_MULTIPLIER("spoil_rate_multiplier", -1.0, 100.0),
	DROP_CHANCE_MODIFIER("drop_chance_modifier"),
	SPOIL_CHANCE_MODIFIER("spoil_chance_modifier"),
	SKILLS_ELEMENT_ID("skills_element_id", -1.0, 100.0, -1.0),
	SUMMON_POINTS("summon_points", 0.0, 100.0, 0.0),
	DAMAGE_AGGRO_PERCENT("damageAggroPercent", 0.0, 300.0, 0.0),
	RECIEVE_DAMAGE_LIMIT("recieveDamageLimit", -1.0, Double.POSITIVE_INFINITY, -1.0),
	RECIEVE_DAMAGE_LIMIT_P_SKILL("recieveDamageLimitPSkill", -1.0, Double.POSITIVE_INFINITY, -1.0),
	RECIEVE_DAMAGE_LIMIT_M_SKILL("recieveDamageLimitMSkill", -1.0, Double.POSITIVE_INFINITY, -1.0),
	KILL_AND_RESTORE_HP("killAndRestoreHp", 0.0, 100.0, 0.0),
	RESIST_REFLECT_DAM("resistRelectDam", 0.0, 100.0, 0.0),
	AIRJOKE_RESIST("airjokeResist", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	MUTATE_RESIST("mutateResist", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	DISARM_RESIST("disarmResist", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	PULL_RESIST("pullResist", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	KNOCKBACK_RESIST("knockBackResist", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	KNOCKDOWN_RESIST("knockDownResist", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	MUTATE_POWER("mutatePower", -200.0, 200.0),
	AIRJOKE_POWER("airjokePower", -200.0, 200.0),
	DISARM_POWER("disarmPower", -200.0, 200.0),
	PULL_POWER("pullPower", -200.0, 200.0),
	KNOCKBACK_POWER("knockBackPower", -200.0, 200.0),
	KNOCKDOWN_POWER("knockDownPower", -200.0, 200.0),
	BUFF_TIME_MODIFIER("buff_time_modifier", 1.0, Double.POSITIVE_INFINITY, 1.0),
	DEBUFF_TIME_MODIFIER("debuff_time_modifier", 1.0, Double.POSITIVE_INFINITY, 1.0),
	P_SKILL_CRIT_RATE_DEX_DEPENDENCE("p_skill_crit_rate_dex_dependence", 0.0, 1.0, 0.0),
	SPEED_ON_DEX_DEPENDENCE("speed_on_dex_dependence", 0.0, 1.0, 0.0),
	ENCHANT_CHANCE_MODIFIER("enchant_chance_modifier", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0),
	SOULSHOT_POWER("soulshot_power", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	SPIRITSHOT_POWER("spiritshot_power", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	DAMAGE_BLOCK_RADIUS("damage_block_radius", -1.0, Double.POSITIVE_INFINITY, -1.0),
	DAMAGE_HATE_BONUS("DAMAGE_HATE_BONUS"),
	SharingEquipment("sharingEquipment", 0.0, 1.0, 0.0),
	ShillienProtection("shillienProtection", 0.0, 1.0, 0.0),
	SacrificialSoul("sacrificialSoul", 0.0, 1.0, 0.0),
	RestoreHPGiveDamage("restoreHPGiveDamage", 0.0, 1.0, 0.0),
	MarkOfTrick("MarkOfTrick", 0.0, 1.0, 0.0),
	DivinityOfEinhasad("DivinityOfEinhasad", 0.0, 1.0, 0.0),
	BlockFly("blockFly", 0.0, 1.0, 0.0),
	P_CRIT_RATE_LIMIT("p_crit_rate_limit"),
	ADDITIONAL_EXPERTISE_INDEX("additional_expertise_index"),
	ATTACK_TARGETS_COUNT("attack_targets_count"),
	EXP_FISHING_RATE_MULTIPLIER("exp_fishing_rate_multiplier"),
	SP_FISHING_RATE_MULTIPLIER("sp_fishing_rate_multiplier");

	public static final int NUM_STATS;
	private final String _value;
	private final double _min;
	private final double _max;
	private final double _init;

	public String getValue()
	{
		return _value;
	}

	public double getInit()
	{
		return _init;
	}

	Stats(String s)
	{
		this(s, 0.0, Double.POSITIVE_INFINITY, 0.0);
	}

	Stats(String s, double min, double max)
	{
		this(s, min, max, 0.0);
	}

	Stats(String s, double min, double max, double init)
	{
		_value = s;
		_min = min;
		_max = max;
		_init = init;
	}

	public double validate(double val)
	{
		if(val < _min)
			return _min;
		if(val > _max)
			return _max;
		return val;
	}

	public static Stats valueOfXml(String name)
	{
		for(Stats s : values())
			if(s.getValue().equals(name))
				return s;
		throw new NoSuchElementException("Unknown name '" + name + "' for enum Stats");
	}

	@Override
	public String toString()
	{
		return _value;
	}

	static
	{
		NUM_STATS = values().length;
	}
}
