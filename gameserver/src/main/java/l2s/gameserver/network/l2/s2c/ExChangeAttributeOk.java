package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
 */
public class ExChangeAttributeOk implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new ExChangeAttributeOk();

	public ExChangeAttributeOk()
	{
		//
	}

	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_CHANGE_ATTRIBUTE_OK.writeId(packetWriter);

		return true;
	}
}