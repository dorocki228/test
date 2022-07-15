package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExShowDominionRegistry implements IClientOutgoingPacket
{
	public ExShowDominionRegistry()
	{
		//
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_SHOW_DOMINION_REGISTRY.writeId(packetWriter);
		packetWriter.writeD(0x00);
		packetWriter.writeS("");
		packetWriter.writeS("");
		packetWriter.writeS("");
		packetWriter.writeD(0x00); // Clan Request
		packetWriter.writeD(0x00); // Merc Request
		packetWriter.writeD(0x00); // War Time
		packetWriter.writeD(0x00); // Current Time
		packetWriter.writeD(0x00); // Состояние клановой кнопки: 0 - не подписал, 1 - подписан на эту территорию
		packetWriter.writeD(0x00); // Состояние персональной кнопки: 0 - не подписал, 1 - подписан на эту территорию
		packetWriter.writeD(0x01);
		packetWriter.writeD(0x00); // Territory Count

		return true;
	}
}