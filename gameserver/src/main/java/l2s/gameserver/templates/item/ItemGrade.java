package l2s.gameserver.templates.item;

public enum ItemGrade
{
	NONE(0, 0),
	D(1458, 1),
	C(1459, 2),
	B(1460, 3),
	A(1461, 4),
	S(1462, 5),
	S80(1462, S, 5),
	S84(1462, S, 5),
	R(17371, 6),
	R95(17371, R, 6),
	R99(17371, R, 6);

	public static final ItemGrade[] VALUES;
	private final int _crystalId;
	private final int _extOrdinal;
	private final ItemGrade _extGrade;

	ItemGrade(int crystalId, ItemGrade extGrade, int extOrdinal)
	{
		_crystalId = crystalId;
		_extGrade = extGrade;
		_extOrdinal = extOrdinal;
	}

	ItemGrade(int crystalId, int extOrdinal)
	{
		_crystalId = crystalId;
		_extGrade = this;
		_extOrdinal = extOrdinal;
	}

	public int getCrystalId()
	{
		return _crystalId;
	}

	public ItemGrade extGrade()
	{
		return _extGrade;
	}

	public int extOrdinal()
	{
		return _extOrdinal;
	}

	static
	{
		VALUES = values();
	}
}
