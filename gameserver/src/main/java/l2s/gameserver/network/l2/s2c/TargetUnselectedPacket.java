package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.network.l2.OutgoingPackets;

/**
 * format  ddddd
 */
public class TargetUnselectedPacket implements IClientOutgoingPacket
{
	private int _targetId;
	private Location _loc;

	public TargetUnselectedPacket(GameObject obj)
	{
		_targetId = obj.getObjectId();
		_loc = obj.getLoc();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.TARGET_UNSELECTED.writeId(packetWriter);
		packetWriter.writeD(_targetId);
		packetWriter.writeD(_loc.x);
		packetWriter.writeD(_loc.y);
		packetWriter.writeD(_loc.z);
		packetWriter.writeD(0x00); // иногда бывает 1

		return true;
	}
}