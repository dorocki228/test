package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;

public class ChangeMoveTypePacket extends L2GameServerPacket
{
	public static int WALK;
	public static int RUN;
	private final int _chaId;
	private final boolean _running;

	public ChangeMoveTypePacket(Creature cha)
	{
		_chaId = cha.getObjectId();
		_running = cha.isRunning();
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_chaId);
        writeD(_running ? 1 : 0);
        writeD(0);
	}

	static
	{
		WALK = 0;
		RUN = 1;
	}
}
