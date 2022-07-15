package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class PledgeShowMemberListDeleteAllPacket implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new PledgeShowMemberListDeleteAllPacket();

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.PLEDGE_SHOW_MEMBER_LIST_DELETE_ALL.writeId(packetWriter);

		return true;
	}
}