package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExPartyMemberRenamed implements IClientOutgoingPacket
{
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PARTY_MEMBER_RENAMED.writeId(packetWriter);
		// TODO ddd

		return true;
	}
}