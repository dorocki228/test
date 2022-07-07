package l2s.gameserver.model;

import l2s.gameserver.model.base.Race;
import l2s.gameserver.stats.conditions.Condition;

import java.util.ArrayList;
import java.util.List;

public final class SkillLearn implements Comparable<SkillLearn>
{
	private final int _id;
	private final int _level;
	private final int _minLevel;
	private final int _cost;
	private final int _itemId;
	private final long _itemCount;
	private final Race _race;
	private final boolean _autoGet;
	private final List<Condition> _conditions;

	public SkillLearn(int id, int lvl, int minLvl, int cost, int itemId, long itemCount, boolean autoGet, Race race)
	{
		_conditions = new ArrayList<>();
		_id = id;
		_level = lvl;
		_minLevel = minLvl;
		_cost = cost;
		_itemId = itemId;
		_itemCount = itemCount;
		_autoGet = autoGet;
		_race = race;
	}

	public int getId()
	{
		return _id;
	}

	public int getLevel()
	{
		return _level;
	}

	public int getMinLevel()
	{
		return _minLevel;
	}

	public int getCost()
	{
		return _cost;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public long getItemCount()
	{
		return _itemCount;
	}

	public boolean isAutoGet()
	{
		return _autoGet;
	}

	public Race getRace()
	{
		return _race;
	}

	public boolean isFreeAutoGet()
	{
		return isAutoGet() && getCost() == 0 && (getItemId() == 0 || getItemCount() == 0L);
	}

	public boolean isOfRace(Race race)
	{
		return _race == null || _race == race;
	}

	public void addCondition(Condition condition)
	{
		_conditions.add(condition);
	}

	public boolean testCondition(Player player)
	{
		if(_conditions.isEmpty())
			return true;

		for(Condition condition : _conditions)
			if(!condition.test(player, null, null, null, 0))
				return false;
		return true;
	}

	@Override
	public int compareTo(SkillLearn o)
	{
		if(getId() == o.getId())
			return getLevel() - o.getLevel();
		return getId() - o.getId();
	}
}
