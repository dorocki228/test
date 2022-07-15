package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExRaidCharSelected implements IClientOutgoingPacket
{
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_RAID_CHARACTER_SELECTED.writeId(packetWriter);
		// just a trigger

		return true;
	}
}