package l2s.gameserver.instancemanager;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.matching.MatchingRoom;
import l2s.gameserver.templates.mapregion.RestartArea;
import l2s.gameserver.templates.mapregion.RestartPoint;
import org.apache.commons.lang3.ArrayUtils;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CTreeIntObjectMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class MatchingRoomManager
{
	private static final MatchingRoomManager _instance;
	private final RoomsHolder[] _holder;
	private final Set<Player> _players;

	public static MatchingRoomManager getInstance()
	{
		return _instance;
	}

	public MatchingRoomManager()
	{
		_holder = new RoomsHolder[2];
		_players = new CopyOnWriteArraySet<>();
		_holder[MatchingRoom.PARTY_MATCHING] = new RoomsHolder();
		_holder[MatchingRoom.CC_MATCHING] = new RoomsHolder();
	}

	public void addToWaitingList(Player player)
	{
		_players.add(player);
	}

	public void removeFromWaitingList(Player player)
	{
		_players.remove(player);
	}

	public List<Player> getWaitingList(int minLevel, int maxLevel, int[] classes)
	{
		List<Player> res = new ArrayList<>();
		for(Player $member : _players)
			if($member.getLevel() >= minLevel && $member.getLevel() <= maxLevel && (classes.length == 0 || ArrayUtils.contains(classes, $member.getClassId().getId())))
				res.add($member);
		return res;
	}

	public List<MatchingRoom> getMatchingRooms(int type, int region, boolean allLevels, Player activeChar)
	{
		List<MatchingRoom> res = new ArrayList<>();
		for(MatchingRoom room : _holder[type]._rooms.values())
		{
			if(region > 0 && room.getLocationId() != region)
				continue;
			if(region == -2 && room.getLocationId() != getInstance().getLocation(activeChar))
				continue;
			if (room.getLeader().getFraction().canAttack(activeChar))
				continue;
			if(!allLevels)
			{
				if(room.getMinLevel() > activeChar.getLevel())
					continue;
				if(room.getMaxLevel() < activeChar.getLevel())
					continue;
			}
			res.add(room);
		}
		return res;
	}

	public int addMatchingRoom(MatchingRoom r)
	{
		return _holder[r.getType()].addRoom(r);
	}

	public void removeMatchingRoom(MatchingRoom r)
	{
		_holder[r.getType()]._rooms.remove(r.getId());
	}

	public MatchingRoom getMatchingRoom(int type, int id)
	{
		return _holder[type]._rooms.get(id);
	}

	public int getLocation(Player player)
	{
		if(player == null)
			return 0;
		RestartArea ra = MapRegionManager.getInstance().getRegionData(RestartArea.class, player);
		if(ra != null)
		{
			RestartPoint rp = ra.getRestartPoint().get(player.getRace());
			return rp.getBbs();
		}
		return 0;
	}

	static
	{
		_instance = new MatchingRoomManager();
	}

	private class RoomsHolder
	{
		private int _id;
		private final IntObjectMap<MatchingRoom> _rooms;

		private RoomsHolder()
		{
			_id = 1;
			_rooms = new CTreeIntObjectMap();
		}

		public int addRoom(MatchingRoom r)
		{
			int val = _id++;
			_rooms.put(val, r);
			return val;
		}
	}
}
