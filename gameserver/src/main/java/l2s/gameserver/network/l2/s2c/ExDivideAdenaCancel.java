package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Erlandys
 */
public class ExDivideAdenaCancel implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new ExDivideAdenaCancel();

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_DIVIDE_ADENA_CANCEL.writeId(packetWriter);
		//

		return true;
	}
}