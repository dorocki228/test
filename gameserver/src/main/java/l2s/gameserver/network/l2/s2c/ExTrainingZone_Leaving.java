package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Sdw
 */
public class ExTrainingZone_Leaving implements IClientOutgoingPacket
{
	public static ExTrainingZone_Leaving STATIC = new ExTrainingZone_Leaving();

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_TRAININGZONE_LEAVING.writeId(packetWriter);
		//

		return true;
	}
}