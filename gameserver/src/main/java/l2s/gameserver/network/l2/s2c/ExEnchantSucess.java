package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public final class ExEnchantSucess implements IClientOutgoingPacket
{
	private final int _itemId;

	public ExEnchantSucess(int itemId)
	{
		_itemId = itemId;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_ENCHANT_SUCCESS.writeId(packetWriter);
		packetWriter.writeD(_itemId);

		return true;
	}
}