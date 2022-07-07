package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.StaticObjectInstance;

public class ChairSitPacket extends L2GameServerPacket
{
	private final int _objectId;
	private final int _staticObjectId;

	public ChairSitPacket(Player player, StaticObjectInstance throne)
	{
		_objectId = player.getObjectId();
		_staticObjectId = throne.getUId();
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_objectId);
        writeD(_staticObjectId);
	}
}
