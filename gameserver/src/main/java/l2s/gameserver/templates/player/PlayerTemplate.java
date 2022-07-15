package l2s.gameserver.templates.player;

import l2s.commons.util.Rnd;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.base.BaseStats;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.base.Sex;
import l2s.gameserver.stats.DoubleStat;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.item.StartItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bonux
**/
public final class PlayerTemplate extends PCTemplate
{
	private final Race _race;
	private final Sex _sex;

	private final int _minINT;
	private final int _minSTR;
	private final int _minCON;
	private final int _minMEN;
	private final int _minDEX;
	private final int _minWIT;

	private final int _maxINT;
	private final int _maxSTR;
	private final int _maxCON;
	private final int _maxMEN;
	private final int _maxDEX;
	private final int _maxWIT;

	private final double _baseSafeFallHeight;

	private final List<Location> _startLocs = new ArrayList<Location>();
	private final List<StartItem> _startItems = new ArrayList<StartItem>();

	public PlayerTemplate(StatsSet set, Race race, Sex sex)
	{
		super(set);

		_race = race;
		_sex = sex;

		_minINT = set.getInteger("minINT", (int) BaseStats.MIN_STAT_VALUE);
		_minSTR = set.getInteger("minSTR", (int) BaseStats.MIN_STAT_VALUE);
		_minCON = set.getInteger("minCON", (int) BaseStats.MIN_STAT_VALUE);
		_minMEN = set.getInteger("minMEN", (int) BaseStats.MIN_STAT_VALUE);
		_minDEX = set.getInteger("minDEX", (int) BaseStats.MIN_STAT_VALUE);
		_minWIT = set.getInteger("minWIT", (int) BaseStats.MIN_STAT_VALUE);

		_maxINT = set.getInteger("maxINT", (int) BaseStats.MAX_STAT_VALUE);
		_maxSTR = set.getInteger("maxSTR", (int) BaseStats.MAX_STAT_VALUE);
		_maxCON = set.getInteger("maxCON", (int) BaseStats.MAX_STAT_VALUE);
		_maxMEN = set.getInteger("maxMEN", (int) BaseStats.MAX_STAT_VALUE);
		_maxDEX = set.getInteger("maxDEX", (int) BaseStats.MAX_STAT_VALUE);
		_maxWIT = set.getInteger("maxWIT", (int) BaseStats.MAX_STAT_VALUE);

		_baseSafeFallHeight = set.getDouble("baseSafeFallHeight");
		addBaseValue(DoubleStat.BREATH, set.getOptionalDouble("baseBreathBonus", 100.0));
	}

	public Race getRace()
	{
		return _race;
	}

	public Sex getSex()
	{
		return _sex;
	}

	public int getMinINT()
	{
		return _minINT;
	}

	public int getMinSTR()
	{
		return _minSTR;
	}

	public int getMinCON()
	{
		return _minCON;
	}

	public int getMinMEN()
	{
		return _minMEN;
	}

	public int getMinDEX()
	{
		return _minDEX;
	}

	public int getMinWIT()
	{
		return _minWIT;
	}

	public int getMaxINT()
	{
		return _maxINT;
	}

	public int getMaxSTR()
	{
		return _maxSTR;
	}

	public int getMaxCON()
	{
		return _maxCON;
	}

	public int getMaxMEN()
	{
		return _maxMEN;
	}

	public int getMaxDEX()
	{
		return _maxDEX;
	}

	public int getMaxWIT()
	{
		return _maxWIT;
	}

	public double getBaseSafeFallHeight()
	{
		return _baseSafeFallHeight;
	}

	public void addStartItem(StartItem item)
	{
		_startItems.add(item);
	}

	public StartItem[] getStartItems()
	{
		return _startItems.toArray(new StartItem[_startItems.size()]);
	}

	public void addStartLocation(Location loc)
	{
		_startLocs.add(loc);
	}

	public Location getStartLocation()
	{
		return _startLocs.get(Rnd.get(_startLocs.size()));
	}
}