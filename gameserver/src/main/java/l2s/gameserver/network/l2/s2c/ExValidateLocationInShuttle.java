package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExValidateLocationInShuttle implements IClientOutgoingPacket
{
	private int _playerObjectId, _shuttleId;
	private Location _loc;

	public ExValidateLocationInShuttle(Player cha)
	{
		_playerObjectId = cha.getObjectId();
		_shuttleId = cha.getBoat().getBoatId();
		_loc = cha.getInBoatPosition();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_VALIDATE_LOCATION_IN_SHUTTLE.writeId(packetWriter);
		packetWriter.writeD(_playerObjectId); // Player ObjID
		packetWriter.writeD(_shuttleId); // Shuttle ObjID
		packetWriter.writeD(_loc.x); // X in shuttle
		packetWriter.writeD(_loc.y); // Y in shuttle
		packetWriter.writeD(_loc.z); // Z in shuttle
		packetWriter.writeD(_loc.h); // H in shuttle

		return true;
	}
}