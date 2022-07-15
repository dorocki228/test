package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

/**
 * Reworked: VISTALL
 */
public class AcquireSkillDonePacket implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new AcquireSkillDonePacket();

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.ACQUIRE_SKILL_DONE.writeId(packetWriter);

		return true;
	}
}