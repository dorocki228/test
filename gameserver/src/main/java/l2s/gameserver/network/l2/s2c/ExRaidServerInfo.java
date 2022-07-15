package l2s.gameserver.network.l2.s2c;


import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExRaidServerInfo implements IClientOutgoingPacket
{
	public ExRaidServerInfo()
	{
		//
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_RAID_SERVER_INFO.writeId(packetWriter);
		packetWriter.writeC(0x00); // UNK
		packetWriter.writeC(0x00); // UNK

		return true;
	}
}