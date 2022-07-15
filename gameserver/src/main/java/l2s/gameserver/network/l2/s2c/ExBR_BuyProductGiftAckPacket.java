package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExBR_BuyProductGiftAckPacket implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket RESULT_OK = new ExBR_BuyProductGiftAckPacket(1); // ok
	public static final IClientOutgoingPacket RESULT_NOT_ENOUGH_POINTS = new ExBR_BuyProductGiftAckPacket(-1);
	public static final IClientOutgoingPacket RESULT_WRONG_PRODUCT = new ExBR_BuyProductGiftAckPacket(-2); // also -5
	public static final IClientOutgoingPacket RESULT_INVENTORY_FULL = new ExBR_BuyProductGiftAckPacket(-4);
	public static final IClientOutgoingPacket RESULT_WRONG_ITEM = new ExBR_BuyProductGiftAckPacket(-5);
	public static final IClientOutgoingPacket RESULT_SALE_PERIOD_ENDED = new ExBR_BuyProductGiftAckPacket(-7); // also -8
	public static final IClientOutgoingPacket RESULT_WRONG_USER_STATE = new ExBR_BuyProductGiftAckPacket(-9); // also -11
	public static final IClientOutgoingPacket RESULT_WRONG_PRODUCT_ITEM = new ExBR_BuyProductGiftAckPacket(-10);
	public static final IClientOutgoingPacket RESULT_WRONG_DAY_OF_WEEK = new ExBR_BuyProductGiftAckPacket(-12);
	public static final IClientOutgoingPacket RESULT_WRONG_SALE_PERIOD = new ExBR_BuyProductGiftAckPacket(-13);
	public static final IClientOutgoingPacket RESULT_ITEM_WAS_SALED = new ExBR_BuyProductGiftAckPacket(-14);
	public static final IClientOutgoingPacket RESULT_RECIPIENT_DOESNT_EXIST = new ExBR_BuyProductGiftAckPacket(-17);
	public static final IClientOutgoingPacket RESULT_CAN_NOT_SEND_PACKAGE_TO_YOURSELF = new ExBR_BuyProductGiftAckPacket(-18);
	// -19 - Вы превысили лимит почты (240 шт.), поэтому отправка невозможна.
	// -20 - У получателя переполнен почтовый ящик (240 ед.), поэтому отправка невозможна.
	public static final IClientOutgoingPacket RESULT_BLOCKED_YOU_YOU_CANNOT_SEND_MAIL = new ExBR_BuyProductGiftAckPacket(-21);
	// -22 - Можно подарить максимум 8 типов предметов неограниченного количества.\\nПроверьте количество.\
	// -23 - Данный предмет нельзя подарить.
	// -24 - Невозможно приобрести: несоответствие уровня.
	public static final IClientOutgoingPacket RESULT_NOT_ENOUGH_ADENA = new ExBR_BuyProductGiftAckPacket(-25);
	public static final IClientOutgoingPacket RESULT_NOT_ENOUGH_FREE_COINS = new ExBR_BuyProductGiftAckPacket(-26);
	// -27 - Не выполнено условие даты создания персонажа, покупка невозможна.
	public static final IClientOutgoingPacket RESULT_ITEM_LIMITED = new ExBR_BuyProductGiftAckPacket(-28);	// При покупке количество этих предметов на один аккаунт ограничено. Куплено максимальное количество, больше купить нельзя.

	private final int _result;

	public ExBR_BuyProductGiftAckPacket(int result)
	{
		_result = result;
	}

	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_BR_BUY_PRODUCT_GIFT_ACK.writeId(packetWriter);
		packetWriter.writeD(_result);

		return true;
	}
}