package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 *
 * @author monithly
 */
public class ExCheck_SpeedHack implements IClientOutgoingPacket
{
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_CHECK_SPEEDHACK.writeId(packetWriter);

		return true;
	}
}
