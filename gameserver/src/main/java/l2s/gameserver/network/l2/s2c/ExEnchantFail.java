package l2s.gameserver.network.l2.s2c;

public final class ExEnchantFail extends L2GameServerPacket
{
	public static final ExEnchantFail STATIC = new ExEnchantFail(0, 0);
	private final int itemId;
	private final int itemCount;

	public ExEnchantFail(int itemId, int itemCount)
	{
		this.itemId = itemId;
		this.itemCount = itemCount;
	}

	@Override
	protected void writeImpl()
	{
		if(itemId != 0 && itemCount != 0)
		{
			writeD(itemId);
			writeD(itemCount);
		}
	}
}
