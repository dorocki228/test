package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExShowAPListWnd implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new ExShowAPListWnd();

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_SHOW_AP_LIST_WND.writeId(packetWriter);
		//

		return true;
	}
}