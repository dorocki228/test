package l2s.gameserver.network.l2.s2c;

public class ExShowUsmPacket extends L2GameServerPacket
{
	private final int _usmVideoId;

	public ExShowUsmPacket(int usmVideoId)
	{
		_usmVideoId = usmVideoId;
	}

	@Override
	protected void writeImpl()
	{
        writeD(_usmVideoId);
	}
}
