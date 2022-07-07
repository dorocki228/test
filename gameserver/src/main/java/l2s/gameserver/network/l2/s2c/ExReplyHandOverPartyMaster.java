package l2s.gameserver.network.l2.s2c;

public class ExReplyHandOverPartyMaster extends L2GameServerPacket
{
	public static final L2GameServerPacket TRUE;
	public static final L2GameServerPacket FALSE;
	private final boolean _isLeader;

	public ExReplyHandOverPartyMaster(boolean leader)
	{
		_isLeader = leader;
	}

	@Override
	protected void writeImpl()
	{
        writeD(_isLeader);
	}

	static
	{
		TRUE = new ExReplyHandOverPartyMaster(true);
		FALSE = new ExReplyHandOverPartyMaster(false);
	}
}
