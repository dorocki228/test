package l2s.gameserver.network.l2.s2c;

public class TutorialEnableClientEventPacket extends L2GameServerPacket
{
	private int _event;

	public TutorialEnableClientEventPacket(int event)
	{
		_event = 0;
		_event = event;
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_event);
	}
}
