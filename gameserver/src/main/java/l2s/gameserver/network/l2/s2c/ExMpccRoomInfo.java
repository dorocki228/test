package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.matching.MatchingRoom;
import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExMpccRoomInfo implements IClientOutgoingPacket
{
	private int _index;
	private int _memberSize;
	private int _minLevel;
	private int _maxLevel;
	private int _lootType;
	private int _locationId;
	private String _topic;

	public ExMpccRoomInfo(MatchingRoom matching)
	{
		_index = matching.getId();
		_locationId = matching.getLocationId();
		_topic = matching.getTopic();
		_minLevel = matching.getMinLevel();
		_maxLevel = matching.getMaxLevel();
		_memberSize = matching.getMaxMembersSize();
		_lootType = matching.getLootType();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_MPCC_ROOM_INFO.writeId(packetWriter);
		packetWriter.writeD(_index); //index
		packetWriter.writeD(_memberSize); // member size 1-50
		packetWriter.writeD(_minLevel); //min level
		packetWriter.writeD(_maxLevel); //max level
		packetWriter.writeD(_lootType); //loot type
		packetWriter.writeD(_locationId); //location id as party room
		packetWriter.writeS(_topic); //topic

		return true;
	}
}