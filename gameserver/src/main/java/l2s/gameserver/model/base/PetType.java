package l2s.gameserver.model.base;

public enum PetType
{
	NORMAL,
	KARMA,
	SPECIAL;

	public static final PetType[] VALUES;

	static
	{
		VALUES = values();
	}
}
