package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.instancemanager.MatchingRoomManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.matching.MatchingRoom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ListPartyWatingPacket extends L2GameServerPacket
{
	private static final int ITEMS_PER_PAGE = 16;
	private final Collection<MatchingRoom> _rooms;
	private final int _page;

	public ListPartyWatingPacket(int region, boolean allLevels, int page, Player activeChar)
	{
		_rooms = new ArrayList<>(16);
		_page = page;
		List<MatchingRoom> temp = MatchingRoomManager.getInstance().getMatchingRooms(MatchingRoom.PARTY_MATCHING, region, allLevels, activeChar);
		int first = Math.max((page - 1) * 16, 0);
		for(int firstNot = Math.min(page * 16, temp.size()), i = first; i < firstNot; ++i)
			_rooms.add(temp.get(i));
	}

	@Override
	protected final void writeImpl()
	{
        writeD(_page);
        writeD(_rooms.size());
		for(MatchingRoom room : _rooms)
		{
            writeD(room.getId());
			writeS(room.getTopic());
            writeD(room.getLocationId());
            writeD(room.getMinLevel());
            writeD(room.getMaxLevel());
            writeD(room.getMaxMembersSize());
			writeS(room.getLeader() == null ? "None" : room.getLeader().getName());
			Collection<Player> players = room.getPlayers();
            writeD(players.size());
			for(Player player : players)
			{
                writeD(player.getClassId().getId());
				writeS(player.getName());
			}
		}
	}
}
