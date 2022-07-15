package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author GodWorld
 * @reworked by Bonux
**/
public class ExPledgeWaitingListAlarm implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new ExPledgeWaitingListAlarm();

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PLEDGE_WAITING_LIST_ALARM.writeId(packetWriter);

		return true;
	}
}