package l2s.gameserver.data.xml.holder;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.templates.BaseStatsBonus;

public final class BaseStatsBonusHolder extends AbstractHolder
{
	private static final BaseStatsBonusHolder _instance;
	private final TIntObjectMap<BaseStatsBonus> _bonuses;

	public BaseStatsBonusHolder()
	{
		_bonuses = new TIntObjectHashMap<>();
	}

	public static BaseStatsBonusHolder getInstance()
	{
		return _instance;
	}

	public void addBaseStatsBonus(int value, BaseStatsBonus bonus)
	{
		_bonuses.put(value, bonus);
	}

	public BaseStatsBonus getBaseStatsBonus(int value)
	{
		return _bonuses.get(value);
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

	static
	{
		_instance = new BaseStatsBonusHolder();
	}
}
