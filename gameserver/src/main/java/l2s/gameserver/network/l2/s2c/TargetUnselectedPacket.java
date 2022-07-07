package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.GameObject;
import l2s.gameserver.utils.Location;

public class TargetUnselectedPacket extends L2GameServerPacket
{
	private final int _targetId;
	private final Location _loc;

	public TargetUnselectedPacket(GameObject obj)
	{
		_targetId = obj.getObjectId();
		_loc = obj.getLoc();
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_targetId);
        writeD(_loc.x);
        writeD(_loc.y);
        writeD(_loc.z);
        writeD(0);
	}
}
