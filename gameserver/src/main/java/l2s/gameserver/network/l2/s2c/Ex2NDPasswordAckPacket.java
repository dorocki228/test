package l2s.gameserver.network.l2.s2c;

public class Ex2NDPasswordAckPacket extends L2GameServerPacket
{
	public static final int SUCCESS = 0;
	public static final int WRONG_PATTERN = 1;
	private final int _response;

	public Ex2NDPasswordAckPacket(int response)
	{
		_response = response;
	}

	@Override
	protected void writeImpl()
	{
        writeC(0);
        writeD(_response == 1 ? 1 : 0);
        writeD(0);
	}
}
