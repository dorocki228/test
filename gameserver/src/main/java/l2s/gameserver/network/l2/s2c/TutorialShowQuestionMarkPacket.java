package l2s.gameserver.network.l2.s2c;

public class TutorialShowQuestionMarkPacket extends L2GameServerPacket
{
	private final int _number;

	public TutorialShowQuestionMarkPacket(int number)
	{
		_number = number;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0);
		writeD(_number);
	}
}
