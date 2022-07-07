package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;

public class ExSpawnEmitterPacket extends L2GameServerPacket
{
	private final int _monsterObjId;
	private final int _playerObjId;

	public ExSpawnEmitterPacket(NpcInstance monster, Player player)
	{
		_playerObjId = player.getObjectId();
		_monsterObjId = monster.getObjectId();
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_monsterObjId);
        writeD(_playerObjId);
        writeD(0);
	}
}
