package l2s.gameserver.templates.pet;

import l2s.gameserver.stats.DoubleStat;
import l2s.gameserver.templates.StatsSet;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Bonux
 */
public class PetLevelData
{
	private final int _maxMeal;
	private final long _exp;
	private final int _expType;
	private final int _battleMealConsume;
	private final int _normalMealConsume;

	private final Map<DoubleStat, Optional<Double>> baseValues = new EnumMap<>(DoubleStat.class);

	private final int[] _food;
	private final int _hungryLimit;
	private final int _soulshotCount;
	private final int _spiritshotCount;
	private final int _maxLoad;

	//Статты в режиме маунта
	private final int _battleMealConsumeOnRide;
	private final int _normalMealConsumeOnRide;

	private final double _walkSpdOnRide;
	private final double _runSpdOnRide;
	private final double _waterWalkSpdOnRide;
	private final double _waterRunSpdOnRide;
	private final double _flyWalkSpdOnRide;
	private final double _flyRunSpdOnRide;

	private final double _atkSpdOnRide;
	private final double _pAtkOnRide;
	private final double _mAtkOnRide;
	private final Optional<Double> _maxHpOnRide;
	private final Optional<Double> _maxMpOnRide;

	public PetLevelData(StatsSet set)
	{
		_maxMeal = set.getInteger("max_meal");
		_exp = set.getLong("exp");
		_expType = set.getInteger("exp_type");
		_battleMealConsume = set.getInteger("battle_meal_consume");
		_normalMealConsume = set.getInteger("normal_meal_consume");
		_hungryLimit = set.getInteger("hungry_limit");
		_soulshotCount = set.getInteger("soulshot_count");
		_spiritshotCount = set.getInteger("spiritshot_count");

		// Max HP/MP/CP
		baseValues.put(DoubleStat.MAX_HP, set.getOptionalDouble("hp"));
		baseValues.put(DoubleStat.MAX_MP, set.getOptionalDouble("mp"));

		baseValues.put(DoubleStat.HP_REGEN, set.getOptionalDouble("hp_regen"));
		baseValues.put(DoubleStat.MP_REGEN, set.getOptionalDouble("mp_regen"));

		baseValues.put(DoubleStat.PHYSICAL_ATTACK, set.getOptionalDouble("p_atk"));
		baseValues.put(DoubleStat.PHYSICAL_DEFENCE, set.getOptionalDouble("p_def"));
		baseValues.put(DoubleStat.MAGICAL_ATTACK, set.getOptionalDouble("m_atk"));
		baseValues.put(DoubleStat.MAGICAL_DEFENCE, set.getOptionalDouble("m_def"));

		_food = set.getIntegerArray("food", new int[0]);
		_maxLoad = set.getInteger("max_load");
		_battleMealConsumeOnRide = set.getInteger("battle_meal_consume_on_ride", 0);
		_normalMealConsumeOnRide = set.getInteger("normal_meal_consume_on_ride", 0);
		_walkSpdOnRide = set.getInteger("walk_speed_on_ride", 0);
		_runSpdOnRide = set.getInteger("run_speed_on_ride", 0);
		_waterWalkSpdOnRide = set.getInteger("water_walk_speed_on_ride", 0);
		_waterRunSpdOnRide = set.getInteger("water_run_speed_on_ride", 0);
		_flyWalkSpdOnRide = set.getInteger("fly_walk_speed_on_ride", 0);
		_flyRunSpdOnRide = set.getInteger("fly_run_speed_on_ride", 0);
		_atkSpdOnRide = set.getInteger("attack_speed_on_ride", 0);
		_pAtkOnRide = set.getDouble("p_attack_on_ride", 0.);
		_mAtkOnRide = set.getDouble("m_attack_on_ride", 0.);
		_maxHpOnRide = set.getOptionalDouble("max_hp_on_ride");
		_maxMpOnRide = set.getOptionalDouble("max_mp_on_ride");
	}

	public int getMaxMeal()
	{
		return _maxMeal;
	}

	public long getExp()
	{
		return _exp;
	}

	public int getExpType()
	{
		return _expType;
	}

	public int getBattleMealConsume()
	{
		return _battleMealConsume;
	}

	public int getNormalMealConsume()
	{
		return _normalMealConsume;
	}

	public Optional<Double> getBaseValue(DoubleStat stat)
	{
		return baseValues.getOrDefault(stat, Optional.empty());
	}

	public Optional<Double> getPAtk()
	{
		return getBaseValue(DoubleStat.PHYSICAL_ATTACK);
	}

	public Optional<Double> getPDef()
	{
		return getBaseValue(DoubleStat.PHYSICAL_DEFENCE);
	}

	public Optional<Double> getMAtk()
	{
		return getBaseValue(DoubleStat.MAGICAL_ATTACK);
	}

	public Optional<Double> getMDef()
	{
		return getBaseValue(DoubleStat.MAGICAL_DEFENCE);
	}

	public Optional<Double> getHP()
	{
		return getBaseValue(DoubleStat.MAX_HP);
	}

	public Optional<Double> getMP()
	{
		return getBaseValue(DoubleStat.MAX_MP);
	}

	public Optional<Double> getHPRegen()
	{
		return getBaseValue(DoubleStat.HP_REGEN);
	}

	public Optional<Double> getMPRegen()
	{
		return getBaseValue(DoubleStat.MP_REGEN);
	}

	public int[] getFood()
	{
		return _food;
	}

	public int getHungryLimit()
	{
		return _hungryLimit;
	}

	public int getSoulshotCount()
	{
		return _soulshotCount;
	}

	public int getSpiritshotCount()
	{
		return _spiritshotCount;
	}

	public int getMaxLoad()
	{
		return _maxLoad;
	}

	public int getBattleMealConsumeOnRide()
	{
		return _battleMealConsumeOnRide;
	}

	public int getNormalMealConsumeOnRide()
	{
		return _normalMealConsumeOnRide;
	}

	/**
	 * @param stat movement type
	 * @return the base riding speed of given movement type.
	 */
	public double getSpeedOnRide(DoubleStat stat)
	{
		switch (stat)
		{
			case WALK_SPEED:
				return _walkSpdOnRide;
			case RUN_SPEED:
				return _runSpdOnRide;
			case SWIM_WALK_SPEED:
				return _waterWalkSpdOnRide;
			case SWIM_RUN_SPEED:
				return _waterRunSpdOnRide;
			case FLY_WALK_SPEED:
				return _flyWalkSpdOnRide;
			case FLY_RUN_SPEED:
				return _flyRunSpdOnRide;
		}

		return 0;
	}

	public double getAtkSpdOnRide()
	{
		return _atkSpdOnRide;
	}

	public double getPAtkOnRide()
	{
		return _pAtkOnRide;
	}

	public double getMAtkOnRide()
	{
		return _mAtkOnRide;
	}

	public Optional<Double> getMaxHpOnRide()
	{
		return _maxHpOnRide;
	}

	public Optional<Double> getMaxMpOnRide()
	{
		return _maxMpOnRide;
	}
}