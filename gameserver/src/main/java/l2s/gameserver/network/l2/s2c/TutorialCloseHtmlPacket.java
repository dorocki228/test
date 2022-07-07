package l2s.gameserver.network.l2.s2c;

public class TutorialCloseHtmlPacket extends L2GameServerPacket
{
	public static final L2GameServerPacket STATIC;

	@Override
	protected final void writeImpl()
	{}

	static
	{
		STATIC = new TutorialCloseHtmlPacket();
	}
}
