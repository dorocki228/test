package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExShowQuestInfo implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new ExShowQuestInfo();

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_SHOW_QUEST_INFO.writeId(packetWriter);

		return true;
	}
}