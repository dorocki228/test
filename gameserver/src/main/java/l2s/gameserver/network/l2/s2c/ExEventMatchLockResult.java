package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExEventMatchLockResult implements IClientOutgoingPacket
{
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_EVENT_MATCH_LOCK_RESULT.writeId(packetWriter);
		// TODO пока не реализован даже в клиенте

		return true;
	}
}