package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class PledgeExtendedInfo implements IClientOutgoingPacket
{
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.PLEDGE_EXTENDED_INFO.writeId(packetWriter);
		//TODO SddSddddddddSd

		return true;
	}
}