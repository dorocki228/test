package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
 */
public class ExShowCommission implements IClientOutgoingPacket
{
	public ExShowCommission()
	{
		//
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_SHOW_COMMISSION.writeId(packetWriter);
		packetWriter.writeD(0x01); // ??Open??

		return true;
	}
}