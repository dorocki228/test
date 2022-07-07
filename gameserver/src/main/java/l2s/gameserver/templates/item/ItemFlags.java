package l2s.gameserver.templates.item;

public enum ItemFlags
{
	DESTROYABLE(true),
	DROPABLE(true),
	FREIGHTABLE(false),
	ENCHANTABLE(true),
	SELLABLE(true),
	TRADEABLE(true),
	STOREABLE(true),
	PRIVATESTOREABLE(true),
	ENSOULABLE(true);

	public static final ItemFlags[] VALUES;
	private final int _mask;
	private final boolean _defaultValue;

	ItemFlags(boolean defaultValue)
	{
		_defaultValue = defaultValue;
		_mask = 1 << ordinal();
	}

	public int mask()
	{
		return _mask;
	}

	public boolean getDefaultValue()
	{
		return _defaultValue;
	}

	static
	{
		VALUES = values();
	}
}
