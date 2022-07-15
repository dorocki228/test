package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;
import l2s.gameserver.network.l2.OutgoingPackets;

public class MoveToPawnPacket implements IClientOutgoingPacket
{
	private int _chaId, _targetId, _distance;
	private int _x, _y, _z, _tx, _ty, _tz;

	public MoveToPawnPacket(Creature cha, Creature target, int distance)
	{
		_chaId = cha.getObjectId();
		_targetId = target.getObjectId();
		_distance = distance;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_tx = target.getX();
		_ty = target.getY();
		_tz = target.getZ();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.MOVE_TO_PAWN.writeId(packetWriter);
		packetWriter.writeD(_chaId);
		packetWriter.writeD(_targetId);
		packetWriter.writeD(_distance);

		packetWriter.writeD(_x);
		packetWriter.writeD(_y);
		packetWriter.writeD(_z);

		packetWriter.writeD(_tx);
		packetWriter.writeD(_ty);
		packetWriter.writeD(_tz);

		return true;
	}
}