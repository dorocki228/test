package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public final class ExShowStatPage implements IClientOutgoingPacket
{
	private final int _page;

	public ExShowStatPage(int page)
	{
		_page = page;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_SHOW_STAT_PAGE.writeId(packetWriter);
		packetWriter.writeD(_page);

		return true;
	}
}