package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class TutorialEnableClientEventPacket implements IClientOutgoingPacket
{
	private int _event = 0;

	public TutorialEnableClientEventPacket(int event)
	{
		_event = event;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.TUTORIAL_ENABLE_CLIENT_EVENT.writeId(packetWriter);
		packetWriter.writeD(_event);

		return true;
	}
}