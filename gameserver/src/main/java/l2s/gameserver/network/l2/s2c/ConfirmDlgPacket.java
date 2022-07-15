package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;
import l2s.gameserver.network.l2.components.SystemMsg;

/**
* @author VISTALL
* @date 23/03/2011
*/
public class ConfirmDlgPacket extends SysMsgContainer<ConfirmDlgPacket>
{
	private int _time;
	private int _requestId;

	public ConfirmDlgPacket(SystemMsg msg, int time)
	{
		super(msg);
		_time = time;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.CONFIRM_DLG.writeId(packetWriter);
		writeElements(packetWriter);
		packetWriter.writeD(_time);
		packetWriter.writeD(_requestId);
		return true;
	}

	public void setRequestId(int requestId)
	{
		_requestId = requestId;
	}
}