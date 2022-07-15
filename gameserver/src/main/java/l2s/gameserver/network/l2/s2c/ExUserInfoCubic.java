package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.Cubic;
import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExUserInfoCubic implements IClientOutgoingPacket
{
	private final int _objectId, _agationId;
	private final Cubic[] _cubics;

	public ExUserInfoCubic(Player character)
	{
		_objectId = character.getObjectId();
		_cubics = character.getCubics().toArray(new Cubic[character.getCubics().size()]);
		_agationId = character.getAgathionNpcId();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_USER_INFO_CUBIC.writeId(packetWriter);
		packetWriter.writeD(_objectId);
		packetWriter.writeH(_cubics.length);
		for(Cubic cubic : _cubics)
			packetWriter.writeH(cubic == null ? 0 : cubic.getId());
		packetWriter.writeD(_agationId);

		return true;
	}
}
