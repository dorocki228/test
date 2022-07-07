package l2s.gameserver.network.l2.s2c.updatetype;

public enum UserInfoType implements IUpdateTypeComponent
{
	RELATION(0, 4),
	BASIC_INFO(1, 16),
	BASE_STATS(2, 18),
	MAX_HPCPMP(3, 14),
	CURRENT_HPMPCP_EXP_SP(4, 38),
	ENCHANTLEVEL(5, 4),
	APPAREANCE(6, 15),
	STATUS(7, 6),
	STATS(8, 56),
	ELEMENTALS(9, 14),
	POSITION(10, 18),
	SPEED(11, 18),
	MULTIPLIER(12, 18),
	COL_RADIUS_HEIGHT(13, 18),
	ATK_ELEMENTAL(14, 5),
	CLAN(15, 32),
	SOCIAL(16, 22),
	VITA_FAME(17, 15),
	SLOTS(18, 9),
	MOVEMENTS(19, 4),
	COLOR(20, 10),
	INVENTORY_LIMIT(21, 9),
	UNK_3(22, 9);

	private final int _mask;
	private final int _blockLength;

	UserInfoType(int mask, int blockLength)
	{
		_mask = mask;
		_blockLength = blockLength;
	}

	@Override
	public final int getMask()
	{
		return _mask;
	}

	public int getBlockLength()
	{
		return _blockLength;
	}
}
