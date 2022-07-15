package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExCloseAPListWnd implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new ExCloseAPListWnd();

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_CLOSE_AP_LIST_WND.writeId(packetWriter);
		//

		return true;
	}
}