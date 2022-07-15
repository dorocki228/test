package l2s.gameserver.data.xml.holder;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.model.base.BaseStats;
import l2s.gameserver.templates.BaseStatsBonus;

/**
 * @author Bonux
**/
public final class BaseStatsBonusHolder extends AbstractHolder
{
	private static final BaseStatsBonusHolder _instance = new BaseStatsBonusHolder();

	private final Table<BaseStats, Integer, Double> _bonuses = HashBasedTable.create();

	public static BaseStatsBonusHolder getInstance()
	{
		return _instance;
	}

	public void addBaseStatsBonus(BaseStats baseStats, int value, double bonus)
	{
		_bonuses.put(baseStats, value, bonus);
	}

	public Double getBaseStatsBonus(BaseStats baseStats, int value)
	{
		return _bonuses.get(baseStats, value);
	}

	@Override
	public int size()
	{
		return _bonuses.size();
	}

	@Override
	public void clear()
	{
		_bonuses.clear();
	}
}
