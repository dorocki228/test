package l2s.gameserver.network.l2.s2c;

public class AutoAttackStartPacket extends L2GameServerPacket
{
	private final int _targetId;

	public AutoAttackStartPacket(int targetId)
	{
		_targetId = targetId;
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_targetId);
	}
}
