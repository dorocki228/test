package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.entity.boat.Boat;
import l2s.gameserver.utils.Location;

public class VehicleCheckLocationPacket extends L2GameServerPacket
{
	private final int _boatObjectId;
	private final Location _loc;

	public VehicleCheckLocationPacket(Boat instance)
	{
		_boatObjectId = instance.getBoatId();
		_loc = instance.getLoc();
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_boatObjectId);
        writeD(_loc.x);
        writeD(_loc.y);
        writeD(_loc.z);
        writeD(_loc.h);
	}
}
