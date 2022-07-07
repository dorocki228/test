package l2s.gameserver.network.l2.s2c;

public class TutorialShowHtmlPacket extends L2GameServerPacket
{
	public static int NORMAL_WINDOW;
	public static int LARGE_WINDOW;
	private final int _windowType;
	private final String _html;

	public TutorialShowHtmlPacket(int windowType, String html)
	{
		_windowType = windowType;
		_html = html;
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_windowType);
		writeS(_html);
	}

	static
	{
		NORMAL_WINDOW = 1;
		LARGE_WINDOW = 2;
	}
}
