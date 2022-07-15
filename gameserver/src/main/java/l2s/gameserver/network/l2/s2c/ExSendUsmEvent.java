package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExSendUsmEvent implements IClientOutgoingPacket
{
	private int _usmVideoId;

	public ExSendUsmEvent(int usmVideoId)
	{
		_usmVideoId = usmVideoId;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_SEND_USM_EVENT.writeId(packetWriter);
		packetWriter.writeD(_usmVideoId);

		return true;
	}
}