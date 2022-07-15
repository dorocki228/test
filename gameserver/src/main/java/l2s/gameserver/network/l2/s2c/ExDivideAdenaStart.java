package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Erlandys
 */
public class ExDivideAdenaStart implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new ExDivideAdenaStart();

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_DIVIDE_ADENA_START.writeId(packetWriter);
		//

		return true;
	}
}