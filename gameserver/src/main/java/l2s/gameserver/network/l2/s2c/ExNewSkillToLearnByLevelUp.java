package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExNewSkillToLearnByLevelUp implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new ExNewSkillToLearnByLevelUp();

	public ExNewSkillToLearnByLevelUp()
	{
		//
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_NEW_SKILL_TO_LEARN_BY_LEVEL_UP.writeId(packetWriter);

		return true;
	}
}
