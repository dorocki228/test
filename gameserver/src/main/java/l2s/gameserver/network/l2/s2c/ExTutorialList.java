package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExTutorialList implements IClientOutgoingPacket
{
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_TUTORIAL_LIST.writeId(packetWriter);
		packetWriter.writeB(new byte[128]);
		/*packetWriter.writeS("");
		packetWriter.writeD(0x00);
		packetWriter.writeD(0x00);
		packetWriter.writeD(0x00);*/

		return true;
	}
}
