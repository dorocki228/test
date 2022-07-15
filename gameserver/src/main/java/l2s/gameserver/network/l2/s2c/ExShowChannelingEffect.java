package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExShowChannelingEffect implements IClientOutgoingPacket
{
	private final int _casterObjectId;
	private final int _targetObjectId;
	private final int _state;
	
	public ExShowChannelingEffect(Creature caster, Creature target, int state)
	{
		_casterObjectId = caster.getObjectId();
		_targetObjectId = target.getObjectId();
		_state = state;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_SHOW_CHANNELING_EFFECT.writeId(packetWriter);
		packetWriter.writeD(_casterObjectId);
		packetWriter.writeD(_targetObjectId);
		packetWriter.writeD(_state);

		return true;
	}
}