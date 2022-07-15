package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.entity.events.impl.DuelEvent;
import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExDuelEnd implements IClientOutgoingPacket
{
	private int _duelType;

	public ExDuelEnd(DuelEvent e)
	{
		_duelType = e.getDuelType();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_DUEL_END.writeId(packetWriter);
		packetWriter.writeD(_duelType);

		return true;
	}
}