package l2s.gameserver.network.l2.s2c;

public class ExPVPMatchUserDie extends L2GameServerPacket
{
	private final int _blueKills;
	private final int _redKills;

	public ExPVPMatchUserDie(int blueKills, int redKills)
	{
		_blueKills = blueKills;
		_redKills = redKills;
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_blueKills);
        writeD(_redKills);
	}
}
