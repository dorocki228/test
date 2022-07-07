package l2s.gameserver.network.l2.s2c;

public class ExCubeGameChangeTimeToStart extends L2GameServerPacket
{
	int _seconds;

	public ExCubeGameChangeTimeToStart(int seconds)
	{
		_seconds = seconds;
	}

	@Override
	protected void writeImpl()
	{
        writeD(3);
        writeD(_seconds);
	}
}
