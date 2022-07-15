package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExOlympiadMatchEndPacket implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new ExOlympiadMatchEndPacket();

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_OLYMPIAD_MATCH_END.writeId(packetWriter);

		return true;
	}
}