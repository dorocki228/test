package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExPledgeSkillAdd implements IClientOutgoingPacket
{
	private int _skillId;
	private int _skillLevel;

	public ExPledgeSkillAdd(int skillId, int skillLevel)
	{
		_skillId = skillId;
		_skillLevel = skillLevel;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PLEDGE_SKILL_ADD.writeId(packetWriter);
		packetWriter.writeD(_skillId);
		packetWriter.writeD(_skillLevel);

		return true;
	}
}