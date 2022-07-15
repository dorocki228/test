package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 *
 * @author monithly
 */
public class ExAgitAuctionCmd implements IClientOutgoingPacket
{
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_AGITAUCTION_CMD.writeId(packetWriter);

		return true;
	}
}
