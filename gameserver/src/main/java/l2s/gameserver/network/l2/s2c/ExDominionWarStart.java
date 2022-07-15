package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExDominionWarStart implements IClientOutgoingPacket
{
	public ExDominionWarStart()
	{
		//
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_DOMINION_WAR_START.writeId(packetWriter);
		packetWriter.writeD(0x00);
		packetWriter.writeD(0x00);
		packetWriter.writeD(0x00); //territory Id
		packetWriter.writeD(0x00);
		packetWriter.writeD(0x00); //territory Id

		return true;
	}
}
