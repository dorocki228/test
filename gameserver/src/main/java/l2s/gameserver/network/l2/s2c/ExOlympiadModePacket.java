package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExOlympiadModePacket implements IClientOutgoingPacket
{
	// chc
	private int _mode;

	/**
	 * @param _mode (0 = return, 3 = spectate)
	 */
	public ExOlympiadModePacket(int mode)
	{
		_mode = mode;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_OLYMPIAD_MODE.writeId(packetWriter);
		packetWriter.writeC(_mode);

		return true;
	}
}