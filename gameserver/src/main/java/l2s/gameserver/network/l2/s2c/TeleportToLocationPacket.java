package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.utils.Location;

public class TeleportToLocationPacket extends L2GameServerPacket
{
	private final int _targetId;
	private final Location _loc;
	private final boolean isFastTeleport;

	public TeleportToLocationPacket(GameObject cha, Location loc)
	{
		_targetId = cha.getObjectId();
		_loc = loc;
		isFastTeleport = false;
	}

	public TeleportToLocationPacket(GameObject cha, int x, int y, int z)
	{
		_targetId = cha.getObjectId();
		_loc = new Location(x, y, z, cha.getHeading());
		isFastTeleport = false;
	}

	public TeleportToLocationPacket(GameObject cha, Location loc, boolean isFastTeleport)
	{
		_targetId = cha.getObjectId();
		_loc = loc;
		this.isFastTeleport = isFastTeleport;
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_targetId);
        writeD(_loc.x);
        writeD(_loc.y);
        writeD(_loc.z + Config.CLIENT_Z_SHIFT);
		writeD(isFastTeleport ? 1 : 0); // 0 - with black screen, 1 - fast teleport (for position correcting)
        writeD(_loc.h);
        writeD(0);
	}
}
