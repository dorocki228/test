package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.entity.events.impl.DuelEvent;
import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExDuelStart implements IClientOutgoingPacket
{
	private int _duelType;

	public ExDuelStart(DuelEvent e)
	{
		_duelType = e.getDuelType();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_DUEL_START.writeId(packetWriter);
		packetWriter.writeD(_duelType);

		return true;
	}
}