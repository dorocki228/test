package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.pledge.RankPrivs;
import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExPledgePowerGradeList implements IClientOutgoingPacket
{
	private RankPrivs[] _privs;

	public ExPledgePowerGradeList(RankPrivs[] privs)
	{
		_privs = privs;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PLEDGE_POWER_GRADE_LIST.writeId(packetWriter);
		packetWriter.writeD(_privs.length);
		for(RankPrivs element : _privs)
		{
			packetWriter.writeD(element.getRank());
			packetWriter.writeD(element.getParty());
		}

		return true;
	}
}