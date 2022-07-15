package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author VISTALL
 * @date 11:19/03.07.2011
 */
public class ExGoodsInventoryResult implements IClientOutgoingPacket
{
	public static IClientOutgoingPacket NOTHING = new ExGoodsInventoryResult(1);
	public static IClientOutgoingPacket SUCCESS = new ExGoodsInventoryResult(2);
	public static IClientOutgoingPacket ERROR = new ExGoodsInventoryResult(-1);
	public static IClientOutgoingPacket TRY_AGAIN_LATER = new ExGoodsInventoryResult(-2);
	public static IClientOutgoingPacket INVENTORY_FULL = new ExGoodsInventoryResult(-3);
	public static IClientOutgoingPacket NOT_CONNECT_TO_PRODUCT_SERVER = new ExGoodsInventoryResult(-4);
	public static IClientOutgoingPacket CANT_USE_AT_TRADE_OR_PRIVATE_SHOP = new ExGoodsInventoryResult(-5);
	public static IClientOutgoingPacket NOT_EXISTS = new ExGoodsInventoryResult(-6);
	public static IClientOutgoingPacket TO_MANY_USERS_TRY_AGAIN_INVENTORY = new ExGoodsInventoryResult(-101);
	public static IClientOutgoingPacket TO_MANY_USERS_TRY_AGAIN = new ExGoodsInventoryResult(-102);
	public static IClientOutgoingPacket PREVIOS_REQUEST_IS_NOT_COMPLETE = new ExGoodsInventoryResult(-103);
	public static IClientOutgoingPacket NOTHING2 = new ExGoodsInventoryResult(-104);
	public static IClientOutgoingPacket ALREADY_RETRACTED = new ExGoodsInventoryResult(-105);
	public static IClientOutgoingPacket ALREADY_RECIVED = new ExGoodsInventoryResult(-106);
	public static IClientOutgoingPacket PRODUCT_CANNOT_BE_RECEIVED_AT_CURRENT_SERVER = new ExGoodsInventoryResult(-107);
	public static IClientOutgoingPacket PRODUCT_CANNOT_BE_RECEIVED_AT_CURRENT_PLAYER = new ExGoodsInventoryResult(-108);

	private int _result;

	private ExGoodsInventoryResult(int result)
	{
		_result = result;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_GOODINVENTORY_RESULT.writeId(packetWriter);
		packetWriter.writeD(_result);

		return true;
	}
}
