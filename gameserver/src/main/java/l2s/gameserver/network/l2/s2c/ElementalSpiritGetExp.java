package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ElementalSpiritGetExp implements IClientOutgoingPacket
{
	private final int _elementId;
	private final long _exp;

	public ElementalSpiritGetExp(int elementId, long exp)
	{
		_elementId = elementId;
		_exp = exp;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_ELEMENTAL_SPIRIT_GET_EXP.writeId(packetWriter);
		packetWriter.writeC(_elementId);
		packetWriter.writeQ(_exp);
		return true;
	}
}