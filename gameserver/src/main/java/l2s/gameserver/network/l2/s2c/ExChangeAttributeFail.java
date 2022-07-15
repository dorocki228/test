package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
 */
public class ExChangeAttributeFail implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new ExChangeAttributeFail();

	public ExChangeAttributeFail()
	{
		//
	}

	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_CHANGE_ATTRIBUTE_FAIL.writeId(packetWriter);

		return true;
	}
}