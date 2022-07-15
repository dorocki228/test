package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author : Ragnarok
 * @date : 28.03.12  16:23
 */
public class ExCallToChangeClass implements IClientOutgoingPacket
{
	private int _classId;
	private boolean _showMsg;

	public ExCallToChangeClass(int classId, boolean showMsg)
	{
		_classId = classId;
		_showMsg = showMsg;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_CALL_TO_CHANGE_CLASS.writeId(packetWriter);
		packetWriter.writeD(_classId); // New Class Id
		packetWriter.writeD(_showMsg); // Show Message

		return true;
	}
}
