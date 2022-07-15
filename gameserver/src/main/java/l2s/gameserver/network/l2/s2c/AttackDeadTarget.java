package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class AttackDeadTarget implements IClientOutgoingPacket
{
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.ATTACK_DEAD_TARGET.writeId(packetWriter);
		return true;
	}
}