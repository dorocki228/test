package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExFieldEventEffect implements IClientOutgoingPacket
{
	private final int _unk;

	public ExFieldEventEffect(int unk)
	{
		_unk = unk;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_FIELD_EVENT_EFFECT.writeId(packetWriter);
		packetWriter.writeD(_unk);

		return true;
	}
}