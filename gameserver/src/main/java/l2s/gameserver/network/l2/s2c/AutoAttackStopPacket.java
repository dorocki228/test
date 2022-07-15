package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class AutoAttackStopPacket implements IClientOutgoingPacket
{
	// dh
	private int _targetId;

	/**
	 * @param _characters
	 */
	public AutoAttackStopPacket(int targetId)
	{
		_targetId = targetId;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.AUTO_ATTACK_STOP.writeId(packetWriter);
		packetWriter.writeD(_targetId);
		return true;
	}
}