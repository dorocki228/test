package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class TutorialCloseHtmlPacket implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new TutorialCloseHtmlPacket();

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.TUTORIAL_CLOSE_HTML.writeId(packetWriter);

		return true;
	}
}