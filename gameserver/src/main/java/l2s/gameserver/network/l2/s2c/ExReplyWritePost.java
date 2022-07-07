package l2s.gameserver.network.l2.s2c;

public class ExReplyWritePost extends L2GameServerPacket
{
	public static final L2GameServerPacket STATIC_TRUE;
	public static final L2GameServerPacket STATIC_FALSE;
	private final int _reply;

	public ExReplyWritePost(int i)
	{
		_reply = i;
	}

	@Override
	protected void writeImpl()
	{
        writeD(_reply);
	}

	static
	{
		STATIC_TRUE = new ExReplyWritePost(1);
		STATIC_FALSE = new ExReplyWritePost(0);
	}
}
