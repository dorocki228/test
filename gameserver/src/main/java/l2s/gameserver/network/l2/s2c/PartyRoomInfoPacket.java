package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.matching.MatchingRoom;
import l2s.gameserver.network.l2.OutgoingPackets;

public class PartyRoomInfoPacket implements IClientOutgoingPacket
{
	private int _id;
	private int _minLevel;
	private int _maxLevel;
	private int _lootDist;
	private int _maxMembers;
	private int _location;
	private String _title;

	public PartyRoomInfoPacket(MatchingRoom room)
	{
		_id = room.getId();
		_minLevel = room.getMinLevel();
		_maxLevel = room.getMaxLevel();
		_lootDist = room.getLootType();
		_maxMembers = room.getMaxMembersSize();
		_location = room.getLocationId();
		_title = room.getTopic();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.PARTY_ROOM_INFO.writeId(packetWriter);
		packetWriter.writeD(_id); // room id
		packetWriter.writeD(_maxMembers); //max members
		packetWriter.writeD(_minLevel); //min level
		packetWriter.writeD(_maxLevel); //max level
		packetWriter.writeD(_lootDist); //loot distribution 1-Random 2-Random includ. etc
		packetWriter.writeD(_location); //location
		packetWriter.writeS(_title); // room name

		return true;
	}
}