package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExPVPMatchCCRetire implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new ExPVPMatchCCRetire();

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PVPMATCH_CC_RETIRE.writeId(packetWriter);

		return true;
	}
}