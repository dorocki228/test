package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author VISTALL
 * @date 11:33/03.07.2011
 */
public class ExGoodsInventoryChangedNotify implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new ExGoodsInventoryChangedNotify();

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_GOODINVENTORY_CHANGED_NOTI.writeId(packetWriter);

		return true;
	}
}
