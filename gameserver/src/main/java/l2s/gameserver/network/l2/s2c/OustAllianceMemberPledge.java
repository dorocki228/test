package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class OustAllianceMemberPledge implements IClientOutgoingPacket
{
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.OUST_ALLIANCE_MEMBER_PLEDGE.writeId(packetWriter);
		//TODO d

		return true;
	}
}