package l2s.gameserver.network.l2.s2c.updatetype;

public enum PartySmallWindowUpdateType implements IUpdateTypeComponent
{
	CURRENT_CP(1),
	MAX_CP(2),
	CURRENT_HP(4),
	MAX_HP(8),
	CURRENT_MP(16),
	MAX_MP(32),
	LEVEL(64),
	CLASS_ID(128),
	VITALITY_POINTS(256);

	private final int _mask;

	PartySmallWindowUpdateType(int mask)
	{
		_mask = mask;
	}

	@Override
	public int getMask()
	{
		return _mask;
	}
}
