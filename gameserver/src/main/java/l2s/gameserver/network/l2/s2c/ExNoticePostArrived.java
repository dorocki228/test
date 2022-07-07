package l2s.gameserver.network.l2.s2c;

public class ExNoticePostArrived extends L2GameServerPacket
{
	public static final L2GameServerPacket STATIC_TRUE;
	public static final L2GameServerPacket STATIC_FALSE;
	private final int _anim;

	public ExNoticePostArrived(int useAnim)
	{
		_anim = useAnim;
	}

	@Override
	protected void writeImpl()
	{
        writeD(_anim);
	}

	static
	{
		STATIC_TRUE = new ExNoticePostArrived(1);
		STATIC_FALSE = new ExNoticePostArrived(0);
	}
}
