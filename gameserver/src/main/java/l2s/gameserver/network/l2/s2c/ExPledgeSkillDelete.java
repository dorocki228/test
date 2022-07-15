package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExPledgeSkillDelete implements IClientOutgoingPacket
{
	public ExPledgeSkillDelete()
	{
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PLEDGE_SKILL_DELETE.writeId(packetWriter);

		return true;
	}
}