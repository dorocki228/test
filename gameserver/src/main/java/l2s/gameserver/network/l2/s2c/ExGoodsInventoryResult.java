package l2s.gameserver.network.l2.s2c;

public class ExGoodsInventoryResult extends L2GameServerPacket
{
	public static L2GameServerPacket NOTHING;
	public static L2GameServerPacket SUCCESS;
	public static L2GameServerPacket ERROR;
	public static L2GameServerPacket TRY_AGAIN_LATER;
	public static L2GameServerPacket INVENTORY_FULL;
	public static L2GameServerPacket NOT_CONNECT_TO_PRODUCT_SERVER;
	public static L2GameServerPacket CANT_USE_AT_TRADE_OR_PRIVATE_SHOP;
	public static L2GameServerPacket NOT_EXISTS;
	public static L2GameServerPacket TO_MANY_USERS_TRY_AGAIN_INVENTORY;
	public static L2GameServerPacket TO_MANY_USERS_TRY_AGAIN;
	public static L2GameServerPacket PREVIOS_REQUEST_IS_NOT_COMPLETE;
	public static L2GameServerPacket NOTHING2;
	public static L2GameServerPacket ALREADY_RETRACTED;
	public static L2GameServerPacket ALREADY_RECIVED;
	public static L2GameServerPacket PRODUCT_CANNOT_BE_RECEIVED_AT_CURRENT_SERVER;
	public static L2GameServerPacket PRODUCT_CANNOT_BE_RECEIVED_AT_CURRENT_PLAYER;
	private final int _result;

	private ExGoodsInventoryResult(int result)
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
		NOTHING = new ExGoodsInventoryResult(1);
		SUCCESS = new ExGoodsInventoryResult(2);
		ERROR = new ExGoodsInventoryResult(-1);
		TRY_AGAIN_LATER = new ExGoodsInventoryResult(-2);
		INVENTORY_FULL = new ExGoodsInventoryResult(-3);
		NOT_CONNECT_TO_PRODUCT_SERVER = new ExGoodsInventoryResult(-4);
		CANT_USE_AT_TRADE_OR_PRIVATE_SHOP = new ExGoodsInventoryResult(-5);
		NOT_EXISTS = new ExGoodsInventoryResult(-6);
		TO_MANY_USERS_TRY_AGAIN_INVENTORY = new ExGoodsInventoryResult(-101);
		TO_MANY_USERS_TRY_AGAIN = new ExGoodsInventoryResult(-102);
		PREVIOS_REQUEST_IS_NOT_COMPLETE = new ExGoodsInventoryResult(-103);
		NOTHING2 = new ExGoodsInventoryResult(-104);
		ALREADY_RETRACTED = new ExGoodsInventoryResult(-105);
		ALREADY_RECIVED = new ExGoodsInventoryResult(-106);
		PRODUCT_CANNOT_BE_RECEIVED_AT_CURRENT_SERVER = new ExGoodsInventoryResult(-107);
		PRODUCT_CANNOT_BE_RECEIVED_AT_CURRENT_PLAYER = new ExGoodsInventoryResult(-108);
	}
}
