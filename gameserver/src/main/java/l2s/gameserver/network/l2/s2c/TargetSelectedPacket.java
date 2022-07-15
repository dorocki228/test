package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.geometry.Location;
import l2s.gameserver.network.l2.OutgoingPackets;

/**
 * format   dddddd
 */
public class TargetSelectedPacket implements IClientOutgoingPacket
{
	private int _objectId;
	private int _targetId;
	private Location _loc;

	public TargetSelectedPacket(int objectId, int targetId, Location loc)
	{
		_objectId = objectId;
		_targetId = targetId;
		_loc = loc;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.TARGET_SELECTED.writeId(packetWriter);
		packetWriter.writeD(_objectId);
		packetWriter.writeD(_targetId);
		packetWriter.writeD(_loc.x);
		packetWriter.writeD(_loc.y);
		packetWriter.writeD(_loc.z);
		packetWriter.writeD(0x00);

		return true;
	}
}