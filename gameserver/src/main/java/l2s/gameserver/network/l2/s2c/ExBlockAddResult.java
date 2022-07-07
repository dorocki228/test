package l2s.gameserver.network.l2.s2c;

public class ExBlockAddResult extends L2GameServerPacket
{
	private final String _blockName;

	public ExBlockAddResult(String name)
	{
		_blockName = name;
	}

	@Override
	protected final void writeImpl()
	{
        writeD(1);
		writeS(_blockName);
	}
}
