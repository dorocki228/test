package l2s.gameserver.model.clansearch.base;

public enum ClanSearchRequestType
{
	ENTER_WITH_REQUEST,
	ENTER_WITH_OUT_REQUEST;

	public static final ClanSearchRequestType[] VALUES = values();

	public static ClanSearchRequestType getType(int value)
	{
		return value == -1 || value >= VALUES.length ? ENTER_WITH_REQUEST : VALUES[value];
	}
}
