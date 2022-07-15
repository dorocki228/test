package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class WareHouseDonePacket implements IClientOutgoingPacket
{

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.WAREHOUSE_DONE.writeId(packetWriter);
		packetWriter.writeD(0); //?

		return true;
	}
}