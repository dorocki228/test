package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.geometry.Location;
import l2s.gameserver.network.l2.OutgoingPackets;

public class ObserverStartPacket implements IClientOutgoingPacket
{
	// ddSS
	private Location _loc;

	public ObserverStartPacket(Location loc)
	{
		_loc = loc;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.OBSERVER_START.writeId(packetWriter);
		packetWriter.writeD(_loc.x);
		packetWriter.writeD(_loc.y);
		packetWriter.writeD(_loc.z);
		packetWriter.writeD(0x00);	// YAW
		packetWriter.writeD(0x00);	// Pitch

		return true;
	}
}