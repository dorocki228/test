package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExRaidReserveResult implements IClientOutgoingPacket
{
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_RAID_RESERVE_RESULT.writeId(packetWriter);
		// TODO dx[dddd]

		return true;
	}
}