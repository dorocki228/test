package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Henna;
import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExPeriodicHenna implements IClientOutgoingPacket
{
	private final Henna _henna;
	private final boolean _active;

	public ExPeriodicHenna(Player player)
	{
		_henna = player.getHennaList().getPremiumHenna();
		_active = _henna != null && player.getHennaList().isActive(_henna);
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PERIODIC_HENNA.writeId(packetWriter);
		if(_henna != null)
		{
			packetWriter.writeD(_henna.getTemplate().getSymbolId());	// Premium symbol ID
			packetWriter.writeD(_henna.getLeftTime());	// Premium symbol left time
			packetWriter.writeD(_active);	// Premium symbol active
		}
		else
		{
			packetWriter.writeD(0x00);	// Premium symbol ID
			packetWriter.writeD(0x00);	// Premium symbol left time
			packetWriter.writeD(0x00);	// Premium symbol active
		}

		return true;
	}
}
