package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 *
 * @author monithly
 */
public class ExRestartResponse implements IClientOutgoingPacket
{
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_RESTART_RESPONSE.writeId(packetWriter);

		return true;
	}
}
