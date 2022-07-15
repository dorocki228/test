package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExPartyMatchingRoomHistory implements IClientOutgoingPacket
{
	public ExPartyMatchingRoomHistory()
	{
		//
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PARTY_MATCHING_ROOM_HISTORY.writeId(packetWriter);
		packetWriter.writeD(0x00); // Previously existent rooms count
		/*for(rooms count)
		{
			packet.writeS(""); // Name
			packet.writeS(""); // Owner
		}*/

		return true;
	}
}