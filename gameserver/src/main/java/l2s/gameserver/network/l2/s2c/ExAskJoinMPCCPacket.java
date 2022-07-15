package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * Asks the player to join a Command Channel
 */
public class ExAskJoinMPCCPacket implements IClientOutgoingPacket
{
	private String _requestorName;

	/**
	 * @param String Name of CCLeader
	 */
	public ExAskJoinMPCCPacket(String requestorName)
	{
		_requestorName = requestorName;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_ASK_JOIN_MPCC.writeId(packetWriter);
		packetWriter.writeS(_requestorName); // лидер CC
		packetWriter.writeD(0x00);

		return true;
	}
}