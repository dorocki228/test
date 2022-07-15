package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExPledgeBonusUpdate implements IClientOutgoingPacket
{
	public static enum BonusType
	{
		ATTENDANCE,
		HUNTING
	}

	private final BonusType _type;
	private final int _value;

	public ExPledgeBonusUpdate(BonusType type, int value)
	{
		_type = type;
		_value = value;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PLEDGE_BONUUPDATE.writeId(packetWriter);
		packetWriter.writeC(_type.ordinal());	// Bonus type
		packetWriter.writeD(_value);	// Progress amount

		return true;
	}
}