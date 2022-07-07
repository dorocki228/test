package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;
import l2s.gameserver.utils.Location;

public class ValidateLocationPacket extends L2GameServerPacket
{
	private final int _chaObjId;
	private final Location _loc;

	public ValidateLocationPacket(Creature cha)
	{
		_chaObjId = cha.getObjectId();
		_loc = cha.getLoc();
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_chaObjId);
        writeD(_loc.x);
        writeD(_loc.y);
        writeD(_loc.z);
        writeD(_loc.h);
	}
}
