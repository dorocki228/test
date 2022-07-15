package l2s.gameserver.templates;

import l2s.gameserver.stats.DoubleStat;
import l2s.gameserver.templates.item.WeaponTemplate.WeaponType;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public class CreatureTemplate
{
	private static int[] EMPTY_ATTRIBUTES = new int[] { 0, 0, 0, 0, 0, 0};

	private final StatsSet _statsSet;

	private final Map<DoubleStat, Optional<Double>> baseValues = new EnumMap<>(DoubleStat.class);

	private final int[] _baseAttributeAttack;
	private final int[] _baseAttributeDefence;

	private final double _collisionRadius;
	private final double _collisionHeight;

	private final WeaponType _baseAttackType;

	public CreatureTemplate(StatsSet set)
	{
		_statsSet = set;

		// Base stats
		addBaseValue(DoubleStat.STAT_STR, set.getOptionalDouble("baseSTR", 0));
		addBaseValue(DoubleStat.STAT_CON, set.getOptionalDouble("baseCON", 0));
		addBaseValue(DoubleStat.STAT_DEX, set.getOptionalDouble("baseDEX", 0));
		addBaseValue(DoubleStat.STAT_INT, set.getOptionalDouble("baseINT", 0));
		addBaseValue(DoubleStat.STAT_WIT, set.getOptionalDouble("baseWIT", 0));
		addBaseValue(DoubleStat.STAT_MEN, set.getOptionalDouble("baseMEN", 0));

		// Max HP/MP/CP
		addBaseValue(DoubleStat.MAX_HP, set.getOptionalDouble("baseHpMax"));
		addBaseValue(DoubleStat.MAX_MP, set.getOptionalDouble("baseMpMax"));
		addBaseValue(DoubleStat.MAX_CP, set.getOptionalDouble("baseCpMax"));

		addBaseValue(DoubleStat.HP_REGEN, set.getOptionalDouble("baseHpReg"));
		addBaseValue(DoubleStat.CP_REGEN, set.getOptionalDouble("baseCpReg"));
		addBaseValue(DoubleStat.MP_REGEN, set.getOptionalDouble("baseMpReg"));

		addBaseValue(DoubleStat.PHYSICAL_ATTACK, set.getOptionalDouble("basePAtk"));
		addBaseValue(DoubleStat.MAGICAL_ATTACK, set.getOptionalDouble("baseMAtk"));
		addBaseValue(DoubleStat.PHYSICAL_DEFENCE, set.getOptionalDouble("basePDef"));
		addBaseValue(DoubleStat.MAGICAL_DEFENCE, set.getOptionalDouble("baseMDef"));
		addBaseValue(DoubleStat.PHYSICAL_ATTACK_SPEED, set.getOptionalDouble("basePAtkSpd"));
		addBaseValue(DoubleStat.MAGICAL_ATTACK_SPEED, set.getOptionalDouble("baseMAtkSpd", 333.0));
		addBaseValue(DoubleStat.SHIELD_DEFENCE, set.getOptionalDouble("baseShldDef"));
		addBaseValue(DoubleStat.PHYSICAL_ATTACK_RANGE, set.getOptionalDouble("baseAtkRange"));

		double _baseAttackRadius;
		double _baseAttackAngle;
		String[] damageRange = set.getString("damage_range", "").split(";"); // 0?;0?;fan sector;base attack angle
		if(damageRange.length >= 4)
		{
			_baseAttackRadius = Integer.parseInt(damageRange[2]);
			_baseAttackAngle = Integer.parseInt(damageRange[3]);
		}
		else
		{
			_baseAttackRadius = 26;
			_baseAttackAngle = 120;
		}
		addBaseValue(DoubleStat.PHYSICAL_ATTACK_RADIUS, Optional.of(_baseAttackRadius));
		addBaseValue(DoubleStat.PHYSICAL_ATTACK_ANGLE, Optional.of(_baseAttackAngle));

		addBaseValue(DoubleStat.RANDOM_DAMAGE, set.getOptionalDouble("baseRandDam", 0.0));
		addBaseValue(DoubleStat.SHIELD_DEFENCE_RATE, set.getOptionalDouble("baseShldRate"));
		addBaseValue(DoubleStat.CRITICAL_RATE, set.getOptionalDouble("basePCritRate"));
		addBaseValue(DoubleStat.MAGIC_CRITICAL_RATE, set.getOptionalDouble("baseMCritRate"));

		// Speed
		addBaseValue(DoubleStat.RUN_SPEED, set.getOptionalDouble("baseRunSpd", 120.0));
		addBaseValue(DoubleStat.WALK_SPEED, set.getOptionalDouble("baseWalkSpd",50));
		// Swimming
		addBaseValue(DoubleStat.SWIM_RUN_SPEED, set.getOptionalDouble("baseWaterRunSpd", 120));
		addBaseValue(DoubleStat.SWIM_WALK_SPEED, set.getOptionalDouble("baseWaterWalkSpd", 50));
		// Flying
		addBaseValue(DoubleStat.FLY_RUN_SPEED, set.getOptionalDouble("baseFlyRunSpd", 120));
		addBaseValue(DoubleStat.FLY_WALK_SPEED, set.getOptionalDouble("baseFlyWalkSpd", 50));

		_baseAttributeAttack = set.getIntegerArray("baseAttributeAttack", EMPTY_ATTRIBUTES);
		_baseAttributeDefence = set.getIntegerArray("baseAttributeDefence", EMPTY_ATTRIBUTES);

		_collisionRadius = set.getDoubleArray("collision_radius", new double[] { 5 })[0];
		_collisionHeight = set.getDoubleArray("collision_height", new double[] { 5 })[0];
		_baseAttackType = WeaponType.valueOf(set.getString("baseAttackType", "FIST").toUpperCase());

		addBaseValue(DoubleStat.ABNORMAL_RESIST_PHYSICAL, set.getOptionalDouble("physical_abnormal_resist", 10));
		addBaseValue(DoubleStat.ABNORMAL_RESIST_MAGICAL, set.getOptionalDouble("magic_abnormal_resist", 10));

		// Accuracy / Evasion (Used for NPC only)
		addBaseValue(DoubleStat.ACCURACY_COMBAT, set.getOptionalDouble("accuracy"));
		addBaseValue(DoubleStat.ACCURACY_MAGIC, Optional.empty());
		addBaseValue(DoubleStat.EVASION_RATE, set.getOptionalDouble("evasion"));
		addBaseValue(DoubleStat.MAGIC_EVASION_RATE, Optional.empty());
	}

	protected void addBaseValue(DoubleStat stat, Optional<Double> value) {
		baseValues.put(stat, value);
	}

	public StatsSet getStatsSet()
	{
		return _statsSet;
	}

	public int getId()
	{
		return 0;
	}

	/**
	 * @param stat
	 * @return
	 */
	public Optional<Double> getBaseValue(DoubleStat stat)
	{
		return baseValues.getOrDefault(stat, Optional.empty());
	}

	public int getBaseINT()
	{
		return getBaseValue(DoubleStat.STAT_INT).orElse(0.0).intValue();
	}

	public int getBaseSTR()
	{
		return getBaseValue(DoubleStat.STAT_STR).orElse(0.0).intValue();
	}

	public int getBaseCON()
	{
		return getBaseValue(DoubleStat.STAT_CON).orElse(0.0).intValue();
	}

	public int getBaseMEN()
	{
		return getBaseValue(DoubleStat.STAT_MEN).orElse(0.0).intValue();
	}

	public int getBaseDEX()
	{
		return getBaseValue(DoubleStat.STAT_DEX).orElse(0.0).intValue();
	}

	public int getBaseWIT()
	{
		return getBaseValue(DoubleStat.STAT_WIT).orElse(0.0).intValue();
	}

	public Optional<Double> getBaseHpMax(int level)
	{
		return getBaseValue(DoubleStat.MAX_HP);
	}

	public Optional<Double> getBaseMpMax(int level)
	{
		return getBaseValue(DoubleStat.MAX_MP);
	}

	public Optional<Double> getBaseCpMax(int level)
	{
		return getBaseValue(DoubleStat.MAX_CP);
	}

	public Optional<Double> getBaseHpReg(int level)
	{
		return getBaseValue(DoubleStat.HP_REGEN);
	}

	public Optional<Double> getBaseMpReg(int level)
	{
		return getBaseValue(DoubleStat.MP_REGEN);
	}

	public Optional<Double> getBaseCpReg(int level)
	{
		return getBaseValue(DoubleStat.CP_REGEN);
	}

	public Optional<Double> getBasePAtk()
	{
		return getBaseValue(DoubleStat.PHYSICAL_ATTACK);
	}

	public Optional<Double> getBaseMAtk()
	{
		return getBaseValue(DoubleStat.MAGICAL_ATTACK);
	}

	public Optional<Double> getBasePDef()
	{
		return getBaseValue(DoubleStat.PHYSICAL_DEFENCE);
	}

	public Optional<Double> getBaseMDef()
	{
		return getBaseValue(DoubleStat.MAGICAL_DEFENCE);
	}

	public Optional<Double> getBasePAtkSpd()
	{
		return getBaseValue(DoubleStat.PHYSICAL_ATTACK_SPEED);
	}

	public Optional<Double> getBaseMAtkSpd()
	{
		return getBaseValue(DoubleStat.MAGICAL_ATTACK_SPEED)
				.or(() -> Optional.of(333.0));
	}

	public Optional<Double> getBaseShldDef()
	{
		return getBaseValue(DoubleStat.SHIELD_DEFENCE);
	}

	public Optional<Double> getBaseAtkRange()
	{
		return getBaseValue(DoubleStat.PHYSICAL_ATTACK_RANGE);
	}

	public Optional<Double> getBaseShldRate()
	{
		return getBaseValue(DoubleStat.SHIELD_DEFENCE_RATE);
	}

	public Optional<Double> getBasePCritRate()
	{
		return getBaseValue(DoubleStat.CRITICAL_RATE);
	}

	public Optional<Double> getBaseMCritRate()
	{
		return getBaseValue(DoubleStat.MAGIC_CRITICAL_RATE);
	}

	public int[] getBaseAttributeAttack()
	{
		return _baseAttributeAttack;
	}

	public int[] getBaseAttributeDefence()
	{
		return _baseAttributeDefence;
	}

	public double getCollisionRadius()
	{
		return _collisionRadius;
	}

	public double getCollisionHeight()
	{
		return _collisionHeight;
	}

	public WeaponType getBaseAttackType()
	{
		return _baseAttackType;
	}

	public static StatsSet getEmptyStatsSet()
	{
		return new StatsSet();
	}
}