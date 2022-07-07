package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.utils.Location;

public class ValidateLocationInVehiclePacket extends L2GameServerPacket
{
	private final int _playerObjectId;
	private final int _boatObjectId;
	private final Location _loc;

	public ValidateLocationInVehiclePacket(Player player)
	{
		_playerObjectId = player.getObjectId();
		_boatObjectId = player.getBoat().getBoatId();
		_loc = player.getInBoatPosition();
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_playerObjectId);
        writeD(_boatObjectId);
        writeD(_loc.x);
        writeD(_loc.y);
        writeD(_loc.z);
        writeD(_loc.h);
	}
}