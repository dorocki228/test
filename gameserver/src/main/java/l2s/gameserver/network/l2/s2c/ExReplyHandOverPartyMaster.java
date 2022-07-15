package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author VISTALL
 * @date 20:34/01.09.2011
 */
public class ExReplyHandOverPartyMaster implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket TRUE = new ExReplyHandOverPartyMaster(true);
	public static final IClientOutgoingPacket FALSE = new ExReplyHandOverPartyMaster(false);

	private boolean _isLeader;

	public ExReplyHandOverPartyMaster(boolean leader)
	{
		_isLeader = leader;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_REPLY_HAND_OVER.writeId(packetWriter);
		packetWriter.writeD(_isLeader);

		return true;
	}
}
