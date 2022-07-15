package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Skill.SkillMagicType;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author : Bonux
*/
public class ExChangeMPCost implements IClientOutgoingPacket
{
	private final int _type;
	private final double _value;

	public ExChangeMPCost(SkillMagicType type, double value)
	{
		_type = type.ordinal();
		_value = value;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_CHANGE_MP_COST.writeId(packetWriter);
		packetWriter.writeD(_type);
		packetWriter.writeF(_value);

		return true;
	}
}
