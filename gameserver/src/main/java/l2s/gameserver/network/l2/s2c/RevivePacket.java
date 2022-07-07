package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.GameObject;

public class RevivePacket extends L2GameServerPacket
{
	private final int _objectId;

	public RevivePacket(GameObject obj)
	{
		_objectId = obj.getObjectId();
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_objectId);
	}
}
