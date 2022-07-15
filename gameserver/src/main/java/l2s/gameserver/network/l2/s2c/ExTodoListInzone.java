package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExTodoListInzone implements IClientOutgoingPacket
{
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_TODOLIST_INZONE.writeId(packetWriter);
		int instancesCount = 0;
		packetWriter.writeH(0x00);	// TODO[UNDERGROUND]: Instances count
		for(int i = 0; i < instancesCount; i++)
		{
			packetWriter.writeC(0x00);	// TODO[UNDERGROUND]: Tab
			packetWriter.writeS("");	// TODO[UNDERGROUND]: HTML name
			packetWriter.writeS("");	// TODO[UNDERGROUND]: Zone name
			packetWriter.writeH(0x00);	// TODO[UNDERGROUND]: Min level
			packetWriter.writeH(0x00);	// TODO[UNDERGROUND]: Max level
			packetWriter.writeH(0x00);	// TODO[UNDERGROUND]: Min players
			packetWriter.writeH(0x00);	// TODO[UNDERGROUND]: Max players
			packetWriter.writeC(0x00);	// TODO[UNDERGROUND]: Entry info
			packetWriter.writeC(0x00);	// TODO[UNDERGROUND]: UNK
		}

		return true;
	}
}