package l2s.gameserver.templates;

import gnu.trove.map.TIntIntMap;
import gnu.trove.set.TIntSet;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.BaseStats;

import java.util.EnumMap;
import java.util.Map;

/**
 * Reworked by VISTALL
 */
public class HennaTemplate
{
	private final int _symbolId;
	private final int _dyeId;
	private final int _dyeLvl;
	private final long _drawPrice;
	private final long _drawCount;
	private final long _removePrice;
	private final long _removeCount;
	private final Map<BaseStats, Integer> baseStats = new EnumMap<>(BaseStats.class);
	private final TIntSet _classes;
	private final TIntIntMap _skills;
	private final int _period;

	public HennaTemplate(int symbolId, int dyeId, int dyeLvl, long drawPrice, long drawCount, long removePrice, long removeCount, int wit, int intA, int con, int str, int dex, int men, TIntSet classes, TIntIntMap skills, int period)
	{
		_symbolId = symbolId;
		_dyeId = dyeId;
		_dyeLvl = dyeLvl;
		_drawPrice = drawPrice;
		_drawCount = drawCount;
		_removePrice = removePrice;
		_removeCount = removeCount;
		baseStats.put(BaseStats.STR, str);
		baseStats.put(BaseStats.CON, con);
		baseStats.put(BaseStats.DEX, dex);
		baseStats.put(BaseStats.INT, intA);
		baseStats.put(BaseStats.MEN, men);
		baseStats.put(BaseStats.WIT, wit);
		_classes = classes;
		_skills = skills;
		_period = period;
	}

	public int getSymbolId()
	{
		return _symbolId;
	}

	public int getDyeId()
	{
		return _dyeId;
	}

	public int getDyeLvl()
	{
		return _dyeLvl;
	}

	public long getDrawPrice()
	{
		return _drawPrice;
	}

	public long getDrawCount()
	{
		return _drawCount;
	}

	public long getRemovePrice()
	{
		return _removePrice;
	}

	public long getRemoveCount()
	{
		return _removeCount;
	}

	public int getBaseStat(BaseStats stat)
	{
		return baseStats.getOrDefault(stat, 0);
	}

	public Map<BaseStats, Integer> getBaseStats()
	{
		return baseStats;
	}

	public boolean isForThisClass(Player player)
	{
		return _classes.contains(player.getActiveClassId());
	}

	public TIntIntMap getSkills()
	{
		return _skills;
	}

	public int getPeriod()
	{
		return _period;
	}
}