package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.utils.Location;

public class ExTeleportToLocationActivate extends L2GameServerPacket
{
	private final int _targetId;
	private final Location _loc;

	public ExTeleportToLocationActivate(GameObject cha, Location loc)
	{
		_targetId = cha.getObjectId();
		_loc = loc;
	}

	public ExTeleportToLocationActivate(GameObject cha, int x, int y, int z)
	{
		_targetId = cha.getObjectId();
		_loc = new Location(x, y, z, cha.getHeading());
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_targetId);
        writeD(_loc.x);
        writeD(_loc.y);
        writeD(_loc.z + Config.CLIENT_Z_SHIFT);
        writeD(0);
        writeD(_loc.h);
        writeD(0);
	}
}
