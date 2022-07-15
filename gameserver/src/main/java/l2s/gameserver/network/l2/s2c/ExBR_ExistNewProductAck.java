package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExBR_ExistNewProductAck implements IClientOutgoingPacket
{
	private final int _value;

	public ExBR_ExistNewProductAck(Player player)
	{
		_value = player.getProductHistoryList().haveGifts() ? 0x02 : 0x00;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_BR_EXIST_NEW_PRODUCT_ACK.writeId(packetWriter);
		packetWriter.writeH(_value);	// Has Updates

		return true;
	}
}
