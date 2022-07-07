package l2s.gameserver.network.l2.s2c;

public class ExPledgeBonusUpdate extends L2GameServerPacket
{
	private final byte _type;
	private final int _progress;

	public ExPledgeBonusUpdate(byte type, int progress)
	{
		_type = type;
		_progress = progress;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(_type);
		writeD(_progress);
	}
}
