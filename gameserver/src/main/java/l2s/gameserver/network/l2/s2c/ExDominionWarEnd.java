package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author VISTALL
 * @date 12:11/05.03.2011
 */
public class ExDominionWarEnd implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new ExDominionWarEnd();

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_DOMINION_WAR_END.writeId(packetWriter);

		return true;
	}
}
