package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.RankPrivs;
import l2s.gameserver.model.pledge.UnitMember;
import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExViewPledgePower implements IClientOutgoingPacket
{
	private int PowerGrade, privs;
	private String member_name;

	public ExViewPledgePower(UnitMember member)
	{
		PowerGrade = member.getPowerGrade();
		member_name = member.getName();
		if(member.isClanLeader())
			privs = Clan.CP_ALL;
		else
		{
			RankPrivs temp = member.getClan().getRankPrivs(member.getPowerGrade());
			if(temp != null)
				privs = temp.getPrivs();
			else
				privs = 0;
		}
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_VIEW_PLEDGE_POWER.writeId(packetWriter);
		packetWriter.writeD(PowerGrade);
		packetWriter.writeS(member_name);
		packetWriter.writeD(privs);

		return true;
	}
}