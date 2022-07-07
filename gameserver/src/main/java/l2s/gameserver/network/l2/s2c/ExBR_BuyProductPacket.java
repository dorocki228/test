package l2s.gameserver.network.l2.s2c;

public class ExBR_BuyProductPacket extends L2GameServerPacket
{
	public static final L2GameServerPacket RESULT_OK;
	public static final L2GameServerPacket RESULT_NOT_ENOUGH_POINTS;
	public static final L2GameServerPacket RESULT_WRONG_PRODUCT;
	public static final L2GameServerPacket RESULT_INVENTORY_FULL;
	public static final L2GameServerPacket RESULT_WRONG_ITEM;
	public static final L2GameServerPacket RESULT_SALE_PERIOD_ENDED;
	public static final L2GameServerPacket RESULT_WRONG_USER_STATE;
	public static final L2GameServerPacket RESULT_WRONG_PRODUCT_ITEM;
	public static final L2GameServerPacket RESULT_WRONG_DAY_OF_WEEK;
	public static final L2GameServerPacket RESULT_WRONG_SALE_PERIOD;
	public static final L2GameServerPacket RESULT_ITEM_WAS_SALED;
	private final int _result;

	public ExBR_BuyProductPacket(int result)
	{
		_result = result;
	}

	@Override
	protected void writeImpl()
	{
        writeD(_result);
	}

	static
	{
		RESULT_OK = new ExBR_BuyProductPacket(1);
		RESULT_NOT_ENOUGH_POINTS = new ExBR_BuyProductPacket(-1);
		RESULT_WRONG_PRODUCT = new ExBR_BuyProductPacket(-2);
		RESULT_INVENTORY_FULL = new ExBR_BuyProductPacket(-4);
		RESULT_WRONG_ITEM = new ExBR_BuyProductPacket(-5);
		RESULT_SALE_PERIOD_ENDED = new ExBR_BuyProductPacket(-7);
		RESULT_WRONG_USER_STATE = new ExBR_BuyProductPacket(-9);
		RESULT_WRONG_PRODUCT_ITEM = new ExBR_BuyProductPacket(-10);
		RESULT_WRONG_DAY_OF_WEEK = new ExBR_BuyProductPacket(-12);
		RESULT_WRONG_SALE_PERIOD = new ExBR_BuyProductPacket(-13);
		RESULT_ITEM_WAS_SALED = new ExBR_BuyProductPacket(-14);
	}
}
