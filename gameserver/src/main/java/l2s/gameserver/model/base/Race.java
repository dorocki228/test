package l2s.gameserver.model.base;

public enum Race
{
	HUMAN,
	ELF,
	DARKELF,
	ORC,
	DWARF;

	public static final Race[] VALUES;

	static
	{
		VALUES = values();
	}
}
