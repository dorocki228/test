package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExPledgeActivityMarkReset implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new ExPledgeActivityMarkReset();

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PLEDGE_ACTIVITY_MARK_RESET.writeId(packetWriter);
		// STATIC

		return true;
	}
}