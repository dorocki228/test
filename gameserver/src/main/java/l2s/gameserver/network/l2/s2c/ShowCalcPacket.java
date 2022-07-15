package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

/**
 * sample: d
 */
public class ShowCalcPacket implements IClientOutgoingPacket
{
	private int _calculatorId;

	public ShowCalcPacket(int calculatorId)
	{
		_calculatorId = calculatorId;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.SHOW_CALC.writeId(packetWriter);
		packetWriter.writeD(_calculatorId);

		return true;
	}
}