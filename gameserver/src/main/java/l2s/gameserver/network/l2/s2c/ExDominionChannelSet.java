package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExDominionChannelSet implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket ACTIVE = new ExDominionChannelSet(1);
	public static final IClientOutgoingPacket DEACTIVE = new ExDominionChannelSet(0);

	private int _active;

	public ExDominionChannelSet(int active)
	{
		_active = active;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_DOMINION_CHANNEL_SET.writeId(packetWriter);
		packetWriter.writeD(_active);

		return true;
	}
}