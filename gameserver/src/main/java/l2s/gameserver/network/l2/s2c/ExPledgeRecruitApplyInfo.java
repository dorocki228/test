package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author GodWorld
 * @reworked by Bonux
**/
public class ExPledgeRecruitApplyInfo implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket DEFAULT = new ExPledgeRecruitApplyInfo(0);
	public static final IClientOutgoingPacket ORDER_LIST = new ExPledgeRecruitApplyInfo(1);
	public static final IClientOutgoingPacket CLAN_REG = new ExPledgeRecruitApplyInfo(2);
	public static final IClientOutgoingPacket UNKNOWN = new ExPledgeRecruitApplyInfo(3);
	public static final IClientOutgoingPacket WAITING = new ExPledgeRecruitApplyInfo(4);

	private final int _state;

	private ExPledgeRecruitApplyInfo(int state)
	{
		_state = state;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PLEDGE_RECRUIT_APPLY_INFO.writeId(packetWriter);
		packetWriter.writeD(_state);

		return true;
	}
}