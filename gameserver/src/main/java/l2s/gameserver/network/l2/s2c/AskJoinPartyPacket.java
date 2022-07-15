package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

/**
 *
 * sample
 * <p>
 * 4b
 * c1 b2 e0 4a
 * 00 00 00 00
 * <p>
 *
 * format
 * cdd
 *
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class AskJoinPartyPacket implements IClientOutgoingPacket
{
	private String _requestorName;
	private int _itemDistribution;

	/**
	 * @param int objectId of the target
	 * @param int
	 */
	public AskJoinPartyPacket(String requestorName, int itemDistribution)
	{
		_requestorName = requestorName;
		_itemDistribution = itemDistribution;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.ASK_JOIN_PARTY.writeId(packetWriter);

		packetWriter.writeS(_requestorName);
		packetWriter.writeD(_itemDistribution);

		return true;
	}
}