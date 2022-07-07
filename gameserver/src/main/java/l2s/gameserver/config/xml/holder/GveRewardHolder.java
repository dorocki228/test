package l2s.gameserver.config.xml.holder;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.templates.gve.SimpleReward;
import l2s.gameserver.templates.item.ItemGrade;

import java.util.HashMap;
import java.util.Map;

public final class GveRewardHolder extends AbstractHolder
{
	private static final GveRewardHolder _instance = new GveRewardHolder();

	private final TIntObjectHashMap<SimpleReward> _itemRewards = new TIntObjectHashMap<>();
	private final TIntObjectHashMap<SimpleReward> _levelRewards = new TIntObjectHashMap<>();
	private final TIntObjectHashMap<SimpleReward> _pvpRewards = new TIntObjectHashMap<>();
	private final TIntObjectHashMap<SimpleReward> _setRewards = new TIntObjectHashMap<>();
	private final TIntObjectHashMap<Map<ItemGrade, TIntObjectHashMap<SimpleReward>>> _enchantRewards = new TIntObjectHashMap<>();
	private SimpleReward _nobleReward = null;
	private SimpleReward _heroReward = null;
	private final Map<Integer, Integer> _titleColorReward = new HashMap<>();
	private Integer MAX_LVL = 0;
	private final Integer MAX_PVP = 0;

	public static GveRewardHolder getInstance()
	{
		return _instance;
	}

	@Override
	public int size()
	{
		int size = 0;

		size += _itemRewards.size();
		size += _levelRewards.size();
		size += _pvpRewards.size();
		size += _setRewards.size();
		size += _titleColorReward.size();
		for(Map<ItemGrade, TIntObjectHashMap<SimpleReward>> grade : _enchantRewards.valueCollection())
			for(TIntObjectHashMap<SimpleReward> value : grade.values())
				size += value.size();

		if(_nobleReward != null)
			++size;
		if(_heroReward != null)
			++size;
		return size;
	}

	@Override
	public void clear()
	{
		_itemRewards.clear();
		_levelRewards.clear();
		_pvpRewards.clear();
		_setRewards.clear();
		_enchantRewards.clear();
		_nobleReward = null;
		_heroReward = null;
		_titleColorReward.clear();
	}

	public void addItemReward(int id, SimpleReward reward)
	{
		_itemRewards.put(id, reward);
	}

	public void addLevelReward(int lvl, SimpleReward reward)
	{
		MAX_LVL = Math.max(MAX_LVL, lvl);
		_levelRewards.put(lvl, reward);
	}

	public void addPvpReward(int count, SimpleReward reward)
	{
		_pvpRewards.put(count, reward);
	}

	public void addSetReward(int id, SimpleReward reward)
	{
		_setRewards.put(id, reward);
	}

	public void addEnchantReward(int slot, ItemGrade grade, int value, SimpleReward reward)
	{

		Map<ItemGrade, TIntObjectHashMap<SimpleReward>> gradeMap = _enchantRewards.get(slot);
		if(gradeMap == null)
			_enchantRewards.put(slot, (gradeMap = new HashMap<>()));

		TIntObjectHashMap<SimpleReward> enchantMap = gradeMap.get(grade);
		if(enchantMap == null)
			gradeMap.put(grade, (enchantMap = new TIntObjectHashMap<>()));

		enchantMap.put(value, reward);
	}

	public void addNobleReward(SimpleReward reward)
	{
		_nobleReward = reward;
	}

	public void addHeroReward(SimpleReward reward)
	{
		_heroReward = reward;
	}

	public SimpleReward getItemReward(ItemInstance item)
	{
		if(item != null)
			return _itemRewards.get(item.getItemId());
		return null;
	}

	public SimpleReward getLevelReward(Integer level)
	{
		if(level > MAX_LVL)
			return _levelRewards.get(MAX_LVL);
		return _levelRewards.get(level);
	}

	public SimpleReward getPvpReward(Integer pvp)
	{
		if(pvp > MAX_PVP)
			return _pvpRewards.get(MAX_PVP);
		return _pvpRewards.get(pvp);
	}

	public SimpleReward getSetReward(Integer chest)
	{
		return _setRewards.get(chest);
	}

	public SimpleReward getEnchantReward(int equipSlot, ItemGrade grade, Integer enchant)
	{
		TIntObjectIterator<Map<ItemGrade, TIntObjectHashMap<SimpleReward>>> it = _enchantRewards.iterator();

		while(it.hasNext())
		{
			it.advance();

			int slot = it.key();

			if(slot == equipSlot)
			{
				TIntObjectHashMap<SimpleReward> v = it.value().get(grade);
				if(v != null)
				{
					Integer val = Integer.MIN_VALUE;
					for(int k : v.keys())
					{
						if(k <= enchant)
							val = Math.max(val, k);
					}

					return v.get(val);
				}

				return null;
			}
		}
		return null;
	}

	public SimpleReward getNobleReward()
	{
		return _nobleReward;
	}

	public SimpleReward getHeroReward()
	{
		return _heroReward;
	}

	public void addTitleColorReward(int reward, int color)
	{
		_titleColorReward.put(reward, color);
	}

	public int getTitleColor(int reward)
	{
		Integer val = Integer.MIN_VALUE;

		for(int k : _titleColorReward.keySet())
		{
			if(k <= reward)
				val = Math.max(val, k);
		}

		return _titleColorReward.get(val);
	}
}
