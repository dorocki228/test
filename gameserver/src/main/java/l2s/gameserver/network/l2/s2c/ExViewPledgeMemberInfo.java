package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.pledge.UnitMember;
import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExViewPledgeMemberInfo implements IClientOutgoingPacket
{
	private UnitMember _member;

	public ExViewPledgeMemberInfo(UnitMember member)
	{
		_member = member;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_VIEW_PLEDGE_MEMBER_INFO.writeId(packetWriter);
		packetWriter.writeD(_member.getPledgeType());
		packetWriter.writeS(_member.getName());
		packetWriter.writeS(_member.getTitle());
		packetWriter.writeD(_member.getPowerGrade());
		packetWriter.writeS(_member.getSubUnit().getName());
		packetWriter.writeS(_member.getRelatedName()); // apprentice/sponsor name if any

		return true;
	}
}