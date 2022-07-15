package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.entity.boat.Shuttle;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExShuttleMove implements IClientOutgoingPacket
{
	private final Shuttle _shuttle;
	private final Location _destination;

	public ExShuttleMove(Shuttle shuttle)
	{
		_shuttle = shuttle;
		_destination = shuttle.getMovement().getDestination();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_SHUTTLE_MOVE.writeId(packetWriter);
		packetWriter.writeD(_shuttle.getBoatId()); // Shuttle ObjID
		packetWriter.writeD((int) _shuttle.getMoveSpeed()); // Speed
		packetWriter.writeD(_shuttle.getRotationSpeed()); // Rotation Speed
		packetWriter.writeD(_destination.getX()); // Destination X
		packetWriter.writeD(_destination.getY()); // Destination Y
		packetWriter.writeD(_destination.getZ()); // Destination Z

		return true;
	}
}