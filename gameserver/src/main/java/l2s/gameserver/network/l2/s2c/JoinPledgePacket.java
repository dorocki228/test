package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class JoinPledgePacket implements IClientOutgoingPacket
{
	private int _pledgeId;

	public JoinPledgePacket(int pledgeId)
	{
		_pledgeId = pledgeId;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.JOIN_PLEDGE.writeId(packetWriter);
		packetWriter.writeD(_pledgeId);

		return true;
	}
}