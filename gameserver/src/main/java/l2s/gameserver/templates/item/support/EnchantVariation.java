package l2s.gameserver.templates.item.support;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class EnchantVariation
{
	private final int _id;
	private final TIntObjectMap<EnchantLevel> _levels;
	private int _maxLvl;

	public EnchantVariation(int id)
	{
		_levels = new TIntObjectHashMap();
		_id = id;
	}

	public int getId()
	{
		return _id;
	}

	public void addLevel(EnchantLevel level)
	{
		if(_maxLvl < level.getLevel())
			_maxLvl = level.getLevel();
		_levels.put(level.getLevel(), level);
	}

	public EnchantLevel getLevel(int lvl)
	{
		if(lvl > _maxLvl)
			return _levels.get(_maxLvl);
		return _levels.get(lvl);
	}

	public static class EnchantLevel
	{
		private final int _lvl;
		private final double _baseChance;
		private final double _magicWeaponChance;
		private final double _fullBodyChance;
		private final boolean _succVisualEffect;

		public EnchantLevel(int lvl, double baseChance, double magicWeaponChance, double fullBodyChance, boolean succVisualEffect)
		{
			_lvl = lvl;
			_baseChance = baseChance;
			_magicWeaponChance = magicWeaponChance;
			_fullBodyChance = fullBodyChance;
			_succVisualEffect = succVisualEffect;
		}

		public int getLevel()
		{
			return _lvl;
		}

		public double getBaseChance()
		{
			return _baseChance;
		}

		public double getMagicWeaponChance()
		{
			return _magicWeaponChance;
		}

		public double getFullBodyChance()
		{
			return _fullBodyChance;
		}

		public boolean haveSuccessVisualEffect()
		{
			return _succVisualEffect;
		}
	}
}
