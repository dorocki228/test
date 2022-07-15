package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 *
 * @author monithly
 */
public class ExCloseRaidSocket implements IClientOutgoingPacket
{
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_CLOSE_RAID_SOCKET.writeId(packetWriter);

		return true;
	}
}
