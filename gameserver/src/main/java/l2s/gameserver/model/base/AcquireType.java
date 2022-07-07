package l2s.gameserver.model.base;

public enum AcquireType
{
	NORMAL(0),
	FISHING(1),
	CLAN(2),
	SUB_UNIT(3),
	FISHING_DWARF(4),
	GENERAL(11),
	HERO(13),
	GM(14);

	public static final AcquireType[] VALUES = values();
	private final int _id;

	AcquireType(int id)
	{
		_id = id;
	}

	public int getId()
	{
		return _id;
	}

	public static AcquireType getById(int id)
	{
		for(AcquireType at : VALUES)
			if(at.getId() == id)
				return at;
		return null;
	}
}
