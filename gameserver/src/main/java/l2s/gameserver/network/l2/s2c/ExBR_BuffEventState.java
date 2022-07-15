package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExBR_BuffEventState implements IClientOutgoingPacket
{
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_BR_BUFF_EVENT_STATE.writeId(packetWriter);
		// TODO dddd

		return true;
	}
}