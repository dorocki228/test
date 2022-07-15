package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;
import l2s.gameserver.network.l2.OutgoingPackets;

/**
 * format   ddddd
 */
public class StopMovePacket implements IClientOutgoingPacket
{
	private final int _objectId;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _heading;

	public StopMovePacket(Creature cha)
	{
		_objectId = cha.getObjectId();
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_heading = cha.getHeading();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.STOP_MOVE.writeId(packetWriter);
		packetWriter.writeD(_objectId);
		packetWriter.writeD(_x);
		packetWriter.writeD(_y);
		packetWriter.writeD(_z);
		packetWriter.writeD(_heading);

		return true;
	}
}