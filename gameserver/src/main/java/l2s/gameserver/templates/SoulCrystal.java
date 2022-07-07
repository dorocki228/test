package l2s.gameserver.templates;

public class SoulCrystal
{
	private final int _itemId;
	private final int _level;
	private final int _nextItemId;
	private final int _cursedNextItemId;
	private final double _chance;

	public SoulCrystal(int itemId, int level, int nextItemId, int cursedNextItemId, double chance)
	{
		_itemId = itemId;
		_level = level;
		_nextItemId = nextItemId;
		_cursedNextItemId = cursedNextItemId;
		_chance = chance;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public int getLevel()
	{
		return _level;
	}

	public int getNextItemId()
	{
		return _nextItemId;
	}

	public int getCursedNextItemId()
	{
		return _cursedNextItemId;
	}

	public double getChance()
	{
		return _chance;
	}
}
