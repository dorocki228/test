package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author VISTALL
 */
public class ExDissmissMpccRoom implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new ExDissmissMpccRoom();

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_DISMISMPCC_ROOM.writeId(packetWriter);

		return true;
	}
}