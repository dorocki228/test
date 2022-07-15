package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExConfirmVipAttendanceCheck implements IClientOutgoingPacket
{
	private final boolean _success;
	private final int _receivedIndex;

	public ExConfirmVipAttendanceCheck(boolean success, int receivedIndex)
	{
		_success = success;
		_receivedIndex = receivedIndex;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_CONFIRM_VIP_ATTENDANCE_CHECK.writeId(packetWriter);
		packetWriter.writeC(_success); // Result
		packetWriter.writeC(_receivedIndex); // Received index
		packetWriter.writeD(0x00); // UNK
		packetWriter.writeD(0x00); // UNK

		return true;
	}
}