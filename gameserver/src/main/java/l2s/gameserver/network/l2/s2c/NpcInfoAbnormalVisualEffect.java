package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;
import l2s.gameserver.network.l2.OutgoingPackets;
import l2s.gameserver.skills.AbnormalVisualEffect;

import java.util.Set;

/**
 * @reworked by Bonux
**/
public class NpcInfoAbnormalVisualEffect implements IClientOutgoingPacket
{
	private final int _objectId;
	private final int _transformId;
	private final Set<AbnormalVisualEffect> abnormalVisualEffects;

	public NpcInfoAbnormalVisualEffect(Creature npc)
	{
		_objectId = npc.getObjectId();
		_transformId = npc.getVisualTransformId();
		abnormalVisualEffects = npc.getAbnormalEffects();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.NPC_INFO_ABNORMAL_VISUAL_EFFECT.writeId(packetWriter);
		packetWriter.writeD(_objectId);
		packetWriter.writeD(_transformId);
		packetWriter.writeH(abnormalVisualEffects.size());
		for(AbnormalVisualEffect abnormal : abnormalVisualEffects)
			packetWriter.writeH(abnormal.getId());

		return true;
	}
}