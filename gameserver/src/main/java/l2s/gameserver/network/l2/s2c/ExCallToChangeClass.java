package l2s.gameserver.network.l2.s2c;

public class ExCallToChangeClass extends L2GameServerPacket
{
	private final int _classId;
	private final boolean _showMsg;

	public ExCallToChangeClass(int classId, boolean showMsg)
	{
		_classId = classId;
		_showMsg = showMsg;
	}

	@Override
	protected void writeImpl()
	{
        writeD(_classId);
        writeD(_showMsg);
	}
}
