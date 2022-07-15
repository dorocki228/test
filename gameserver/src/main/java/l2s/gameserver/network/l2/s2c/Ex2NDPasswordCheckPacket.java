package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * Format (ch)dd
 * d: window type
 * d: ban user (1)
 */
public class Ex2NDPasswordCheckPacket implements IClientOutgoingPacket
{
	public static final int PASSWORD_NEW = 0x00;
	public static final int PASSWORD_PROMPT = 0x01;
	public static final int PASSWORD_OK = 0x02;

	private int _windowType;

	public Ex2NDPasswordCheckPacket(int windowType)
	{
		_windowType = windowType;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_2ND_PASSWORD_CHECK.writeId(packetWriter);
		packetWriter.writeD(_windowType);
		packetWriter.writeD(0x00);

		return true;
	}
}
