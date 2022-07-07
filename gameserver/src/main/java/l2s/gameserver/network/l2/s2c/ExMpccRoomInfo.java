package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.matching.MatchingRoom;

public class ExMpccRoomInfo extends L2GameServerPacket
{
	private final int _index;
	private final int _memberSize;
	private final int _minLevel;
	private final int _maxLevel;
	private final int _lootType;
	private final int _locationId;
	private final String _topic;

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
	public void writeImpl()
	{
        writeD(_index);
        writeD(_memberSize);
        writeD(_minLevel);
        writeD(_maxLevel);
        writeD(_lootType);
        writeD(_locationId);
		writeS(_topic);
	}
}
