package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.boat.Boat;
import l2s.gameserver.utils.Location;

public class MoveToLocationInVehiclePacket extends L2GameServerPacket
{
	private final int _playerObjectId;
	private final int _boatObjectId;
	private final Location _origin;
	private final Location _destination;

	public MoveToLocationInVehiclePacket(Player cha, Boat boat, Location origin, Location destination)
	{
		_playerObjectId = cha.getObjectId();
		_boatObjectId = boat.getBoatId();
		_origin = origin;
		_destination = destination;
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_playerObjectId);
        writeD(_boatObjectId);
        writeD(_destination.x);
        writeD(_destination.y);
        writeD(_destination.z);
        writeD(_origin.x);
        writeD(_origin.y);
        writeD(_origin.z);
	}
}
