package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class AutoAttackStartPacket implements IClientOutgoingPacket
{
	// dh
	private int _targetId;

	public AutoAttackStartPacket(int targetId)
	{
		_targetId = targetId;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.AUTO_ATTACK_START.writeId(packetWriter);
		packetWriter.writeD(_targetId);
		return true;
	}
}