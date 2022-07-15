package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @reworked by Bonux
**/
public class ExUserInfoInvenWeight implements IClientOutgoingPacket
{
	private final int _objectId;
	private final int _currentLoad;
	private final int _maxLoad;

	public ExUserInfoInvenWeight(Player player)
	{
		_objectId = player.getObjectId();
		_currentLoad = player.getCurrentLoad();
		_maxLoad = player.getMaxLoad();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_USER_INFO_INVENWEIGHT.writeId(packetWriter);
		packetWriter.writeD(_objectId);
		packetWriter.writeD(_currentLoad);
		packetWriter.writeD(_maxLoad);

		return true;
	}
}