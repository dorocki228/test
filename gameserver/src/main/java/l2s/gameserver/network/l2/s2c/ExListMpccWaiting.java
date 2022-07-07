package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.instancemanager.MatchingRoomManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.matching.MatchingRoom;

import java.util.ArrayList;
import java.util.List;

public class ExListMpccWaiting extends L2GameServerPacket
{
	private static final int ITEMS_PER_PAGE = 10;
	private final int _page;
	private final List<MatchingRoom> _list;

	public ExListMpccWaiting(Player player, int page, int location, boolean allLevels)
	{
        List<MatchingRoom> temp = MatchingRoomManager.getInstance().getMatchingRooms(MatchingRoom.CC_MATCHING, location, allLevels, player);
		_page = page;
		_list = new ArrayList<>(10);
        int firstNot = page * 10;
        int first = (page - 1) * 10;
        for(int i = 0; i < temp.size(); ++i)
			if(i >= first)
				if(i < firstNot)
					_list.add(temp.get(i));
	}

	@Override
	public void writeImpl()
	{
        writeD(_page);
        writeD(_list.size());
		for(MatchingRoom room : _list)
		{
            writeD(room.getId());
			Player leader = room.getLeader();
			writeS(leader == null ? "" : leader.getName());
            writeD(room.getPlayers().size());
            writeD(room.getMinLevel());
            writeD(room.getMaxLevel());
            writeD(1);
            writeD(room.getMaxMembersSize());
			writeS(room.getTopic());
		}
	}
}
