package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;
import l2s.gameserver.skills.SkillCastingType;

import java.util.EnumMap;
import java.util.Map;

public class ActionFailPacket implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new ActionFailPacket();

	private static final Map<SkillCastingType, IClientOutgoingPacket> STATIC_PACKET_BY_CASTING_TYPE = new EnumMap<>(SkillCastingType.class);

	static {
		for (SkillCastingType castingType : SkillCastingType.values()) {
			STATIC_PACKET_BY_CASTING_TYPE.put(castingType, new ActionFailPacket(castingType.getClientBarId()));
		}
	}

	private final int _castingType;

	public ActionFailPacket()
	{
		_castingType = 0;
	}

	public ActionFailPacket(int castingType)
	{
		_castingType = castingType;
	}

	public static IClientOutgoingPacket get(SkillCastingType castingType) {
		return STATIC_PACKET_BY_CASTING_TYPE.getOrDefault(castingType, STATIC);
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.ACTION_FAIL.writeId(packetWriter);

		packetWriter.writeD(_castingType); // MagicSkillUse castingType
		return true;
	}
}