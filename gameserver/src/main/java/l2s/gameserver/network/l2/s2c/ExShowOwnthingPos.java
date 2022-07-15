package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author VISTALL
 */
public class ExShowOwnthingPos implements IClientOutgoingPacket
{
	public ExShowOwnthingPos()
	{
		//
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_SHOW_OWNTHING_POS.writeId(packetWriter);
		packetWriter.writeD(0x00);

		return true;
	}
}