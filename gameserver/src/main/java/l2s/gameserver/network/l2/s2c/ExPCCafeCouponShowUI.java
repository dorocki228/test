package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * Даный пакет показывает менюшку для ввода серийника. Можно что-то придумать :)
 * Format: (ch)
 */
public class ExPCCafeCouponShowUI implements IClientOutgoingPacket
{
	public static final ExPCCafeCouponShowUI STATIC = new ExPCCafeCouponShowUI();

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PCCAFE_COUPON_SHOW_UI.writeId(packetWriter);
		//

		return true;
	}
}