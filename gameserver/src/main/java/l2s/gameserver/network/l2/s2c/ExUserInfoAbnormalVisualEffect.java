package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingExPackets;
import l2s.gameserver.skills.AbnormalVisualEffect;

import java.util.Set;

/**
 * @reworked by Bonux
**/
public class ExUserInfoAbnormalVisualEffect implements IClientOutgoingPacket
{
	private final int _objectId;
	private final int _transformId;
	private final Set<AbnormalVisualEffect> abnormalVisualEffects;

	public ExUserInfoAbnormalVisualEffect(Player player)
	{
		_objectId = player.getObjectId();
		_transformId = player.getVisualTransformId();
		abnormalVisualEffects = player.getAbnormalEffects();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_USER_INFO_ABNORMALVISUALEFFECT.writeId(packetWriter);
		packetWriter.writeD(_objectId);
		packetWriter.writeD(_transformId);
		packetWriter.writeD(abnormalVisualEffects.size());
		for(AbnormalVisualEffect abnormal : abnormalVisualEffects)
			packetWriter.writeH(abnormal.getId());

		return true;
	}
}