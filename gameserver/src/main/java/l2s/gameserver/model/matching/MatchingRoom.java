package l2s.gameserver.model.matching;

import l2s.gameserver.instancemanager.MatchingRoomManager;
import l2s.gameserver.listener.actor.player.OnPlayerPartyInviteListener;
import l2s.gameserver.listener.actor.player.OnPlayerPartyLeaveListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.PlayerGroup;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class MatchingRoom implements PlayerGroup
{
	public static int PARTY_MATCHING;
	public static int CC_MATCHING;
	public static int WAIT_PLAYER;
	public static int ROOM_MASTER;
	public static int PARTY_MEMBER;
	public static int UNION_LEADER;
	public static int UNION_PARTY;
	public static int WAIT_PARTY;
	public static int WAIT_NORMAL;
	private final int _id;
	private int _minLevel;
	private int _maxLevel;
	private int _maxMemberSize;
	private int _lootType;
	private String _topic;
	private final PartyListenerImpl _listener;
	protected Player _leader;
	protected Set<Player> _members;

	public MatchingRoom(Player leader, int minLevel, int maxLevel, int maxMemberSize, int lootType, String topic)
	{
		_listener = new PartyListenerImpl();
		_members = new CopyOnWriteArraySet<>();
		_leader = leader;
		_id = MatchingRoomManager.getInstance().addMatchingRoom(this);
		_minLevel = minLevel;
		_maxLevel = maxLevel;
		_maxMemberSize = maxMemberSize;
		_lootType = lootType;
		_topic = topic;
		addMember0(leader, null, true);
	}

	public boolean addMember(Player player)
	{
		if(_members.contains(player))
			return true;
		if(player.getLevel() < getMinLevel() || player.getLevel() > getMaxLevel() || getPlayers().size() >= getMaxMembersSize())
		{
			player.sendPacket(notValidMessage());
			return false;
		}
		return addMember0(player, new SystemMessagePacket(enterMessage()).addName(player), true);
	}

	public boolean addMemberForce(Player player)
	{
		if(_members.contains(player))
			return true;
		if(getPlayers().size() >= getMaxMembersSize())
		{
			player.sendPacket(notValidMessage());
			return false;
		}
		return addMember0(player, new SystemMessagePacket(enterMessage()).addName(player), false);
	}

	private boolean addMember0(Player player, L2GameServerPacket p, boolean sendInfo)
	{
		if(!_members.isEmpty())
			player.addListener(_listener);
		_members.add(player);
		player.setMatchingRoom(this);
		for(Player $member : this)
			if($member != player && $member.isMatchingRoomWindowOpened())
				$member.sendPacket(p, addMemberPacket($member, player));
		MatchingRoomManager.getInstance().removeFromWaitingList(player);
		if(sendInfo)
		{
			player.setMatchingRoomWindowOpened(true);
			player.sendPacket(infoRoomPacket(), membersPacket(player));
		}
		player.sendChanges();
		return true;
	}

	public void removeMember(Player member, boolean oust)
	{
		if(!_members.remove(member))
			return;
		member.removeListener(_listener);
		member.setMatchingRoom(null);
		if(_members.isEmpty())
			disband();
		else
		{
			L2GameServerPacket infoPacket = infoRoomPacket();
			SystemMsg exitMessage0 = exitMessage(true, oust);
			L2GameServerPacket exitMessage2 = exitMessage0 != null ? new SystemMessagePacket(exitMessage0).addName(member) : null;
			for(Player player : this)
				if(player.isMatchingRoomWindowOpened())
					player.sendPacket(infoPacket, removeMemberPacket(player, member), exitMessage2);
		}
		member.sendPacket(closeRoomPacket(), exitMessage(false, oust));
		member.setMatchingRoomWindowOpened(false);
		member.sendChanges();
	}

	public void broadcastPlayerUpdate(Player player)
	{
		for(Player $member : this)
			if($member.isMatchingRoomWindowOpened())
				$member.sendPacket(updateMemberPacket($member, player));
	}

	public void disband()
	{
		for(Player player : this)
		{
			player.removeListener(_listener);
			if(player.isMatchingRoomWindowOpened())
			{
				player.sendPacket(closeRoomMessage());
				player.sendPacket(closeRoomPacket());
			}
			player.setMatchingRoom(null);
			player.sendChanges();
		}
		_members.clear();
		MatchingRoomManager.getInstance().removeMatchingRoom(this);
	}

	public void setLeader(Player leader)
	{
		_leader = leader;
		if(!_members.contains(leader))
			addMember0(leader, null, true);
		else
		{
			if(!leader.isMatchingRoomWindowOpened())
			{
				leader.setMatchingRoomWindowOpened(true);
				leader.sendPacket(infoRoomPacket(), membersPacket(leader));
			}
			SystemMsg changeLeaderMessage = changeLeaderMessage();
			for(Player $member : this)
				if($member.isMatchingRoomWindowOpened())
					$member.sendPacket(updateMemberPacket($member, leader), changeLeaderMessage);
		}
	}

	public abstract SystemMsg notValidMessage();

	public abstract SystemMsg enterMessage();

	public abstract SystemMsg exitMessage(boolean p0, boolean p1);

	public abstract SystemMsg closeRoomMessage();

	public abstract SystemMsg changeLeaderMessage();

	public abstract L2GameServerPacket closeRoomPacket();

	public abstract L2GameServerPacket infoRoomPacket();

	public abstract L2GameServerPacket addMemberPacket(Player p0, Player p1);

	public abstract L2GameServerPacket removeMemberPacket(Player p0, Player p1);

	public abstract L2GameServerPacket updateMemberPacket(Player p0, Player p1);

	public abstract L2GameServerPacket membersPacket(Player p0);

	public abstract int getType();

	public abstract int getMemberType(Player p0);

	@Override
	public void broadCast(IBroadcastPacket... arg)
	{
		for(Player player : this)
			player.sendPacket(arg);
	}

	public int getId()
	{
		return _id;
	}

	public int getMinLevel()
	{
		return _minLevel;
	}

	public int getMaxLevel()
	{
		return _maxLevel;
	}

	public String getTopic()
	{
		return _topic;
	}

	public int getMaxMembersSize()
	{
		return _maxMemberSize;
	}

	public int getLocationId()
	{
		return MatchingRoomManager.getInstance().getLocation(_leader);
	}

	public Player getLeader()
	{
		return _leader;
	}

	public Collection<Player> getPlayers()
	{
		return _members;
	}

	public int getLootType()
	{
		return _lootType;
	}

	@Override
	public int getMemberCount()
	{
		return getPlayers().size();
	}

	@Override
	public Player getGroupLeader()
	{
		return getLeader();
	}

	@Override
	public Iterator<Player> iterator()
	{
		return _members.iterator();
	}

	public void setMinLevel(int minLevel)
	{
		_minLevel = minLevel;
	}

	public void setMaxLevel(int maxLevel)
	{
		_maxLevel = maxLevel;
	}

	public void setTopic(String topic)
	{
		_topic = topic;
	}

	public void setMaxMemberSize(int maxMemberSize)
	{
		_maxMemberSize = maxMemberSize;
	}

	public void setLootType(int lootType)
	{
		_lootType = lootType;
	}

	static
	{
		PARTY_MATCHING = 0;
		CC_MATCHING = 1;
		WAIT_PLAYER = 0;
		ROOM_MASTER = 1;
		PARTY_MEMBER = 2;
		UNION_LEADER = 3;
		UNION_PARTY = 4;
		WAIT_PARTY = 5;
		WAIT_NORMAL = 6;
	}

	private class PartyListenerImpl implements OnPlayerPartyInviteListener, OnPlayerPartyLeaveListener
	{
		@Override
		public void onPartyInvite(Player player)
		{
			broadcastPlayerUpdate(player);
		}

		@Override
		public void onPartyLeave(Player player)
		{
			broadcastPlayerUpdate(player);
		}
	}
}
