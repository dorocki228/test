package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class TutorialShowHtmlPacket implements IClientOutgoingPacket
{
	public static int NORMAL_WINDOW = 0x01;
	public static int LARGE_WINDOW = 0x02;

	private int _windowType;
	private String _html;

	public TutorialShowHtmlPacket(int windowType, String html)
	{
		_windowType = windowType;
		_html = html;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.TUTORIAL_SHOW_HTML.writeId(packetWriter);
		packetWriter.writeD(_windowType);
		packetWriter.writeS(_html);

		return true;
	}
}