package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class WithdrawAlliance implements IClientOutgoingPacket
{
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.WITHDRAW_ALLIANCE.writeId(packetWriter);
		//TODO d

		return true;
	}
}