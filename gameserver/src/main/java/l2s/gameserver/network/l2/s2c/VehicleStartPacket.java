package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.entity.boat.Boat;
import l2s.gameserver.network.l2.OutgoingPackets;

public class VehicleStartPacket implements IClientOutgoingPacket
{
	private int _objectId, _state;

	public VehicleStartPacket(Boat boat)
	{
		_objectId = boat.getBoatId();
		_state = boat.getRunState();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.VEHICLE_START.writeId(packetWriter);
		packetWriter.writeD(_objectId);
		packetWriter.writeD(_state);

		return true;
	}
}