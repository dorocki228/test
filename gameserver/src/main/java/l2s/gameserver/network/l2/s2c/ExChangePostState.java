package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.mail.Mail;
import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExChangePostState implements IClientOutgoingPacket
{
	private boolean _receivedBoard;
	private Mail[] _mails;
	private int _changeId;

	public ExChangePostState(boolean receivedBoard, int type, Mail... n)
	{
		_receivedBoard = receivedBoard;
		_mails = n;
		_changeId = type;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_CHANGE_POST_STATE.writeId(packetWriter);
		packetWriter.writeD(_receivedBoard ? 1 : 0);
		packetWriter.writeD(_mails.length);
		for(Mail mail : _mails)
		{
			packetWriter.writeD(mail.getMessageId()); // postId
			packetWriter.writeD(_changeId); // state
		}

		return true;
	}
}