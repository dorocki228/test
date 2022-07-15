package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class SkillRemainSec implements IClientOutgoingPacket
{
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.SKILL_REMAIN_SEC.writeId(packetWriter);
		//TODO ddddddd

		return true;
	}
}