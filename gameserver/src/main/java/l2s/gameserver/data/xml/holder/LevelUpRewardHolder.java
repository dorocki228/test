package l2s.gameserver.data.xml.holder;

import gnu.trove.map.TIntLongMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.data.xml.AbstractHolder;

public final class LevelUpRewardHolder extends AbstractHolder
{
	private static final LevelUpRewardHolder _instance;
	private final TIntObjectMap<TIntLongMap> _rewardData;

	public LevelUpRewardHolder()
	{
		_rewardData = new TIntObjectHashMap<>();
	}

	public static LevelUpRewardHolder getInstance()
	{
		return _instance;
	}

	public void addRewardData(int level, TIntLongMap items)
	{
		_rewardData.put(level, items);
	}

	public TIntLongMap getRewardData(int level)
	{
		return _rewardData.get(level);
	}

	@Override
	public int size()
	{
		return _rewardData.size();
	}

	@Override
	public void clear()
	{
		_rewardData.clear();
	}

	static
	{
		_instance = new LevelUpRewardHolder();
	}
}
