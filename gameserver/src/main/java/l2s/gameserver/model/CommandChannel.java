package l2s.gameserver.model;

import com.google.common.collect.Iterators;
import l2s.gameserver.model.base.PledgeRank;
import l2s.gameserver.model.instances.NpcFriendInstance;
import l2s.gameserver.model.matching.MatchingRoom;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class CommandChannel implements PlayerGroup
{
	public static final int STRATEGY_GUIDE_ID = 8871;
	public static final int CLAN_IMPERIUM_ID = 391;
	private final List<Party> _commandChannelParties;
	private Player _commandChannelLeader;
	private int _commandChannelLvl;
	private MatchingRoom _matchingRoom;

	public CommandChannel(Player leader)
	{
		_commandChannelParties = new CopyOnWriteArrayList<>();
		_commandChannelLeader = leader;
		_commandChannelParties.add(leader.getParty());
		_commandChannelLvl = leader.getParty().getLevel();
		leader.getParty().setCommandChannel(this);
		broadCast(ExOpenMPCCPacket.STATIC);
	}

	public void addParty(Party party)
	{
		broadCast(new ExMPCCPartyInfoUpdate(party, 1));
		_commandChannelParties.add(party);
		refreshLevel();
		party.setCommandChannel(this);
		for(Player $member : party)
		{
			$member.sendPacket(ExOpenMPCCPacket.STATIC);
			if(_matchingRoom != null)
				_matchingRoom.broadcastPlayerUpdate($member);
		}
	}

	public void removeParty(Party party)
	{
		_commandChannelParties.remove(party);
		refreshLevel();
		party.setCommandChannel(null);
		party.broadCast(ExCloseMPCCPacket.STATIC);
		if(_commandChannelParties.size() < 2)
			disbandChannel();
		else
			for(Player $member : party)
			{
				$member.sendPacket(new ExMPCCPartyInfoUpdate(party, 0));
				if(_matchingRoom != null)
					_matchingRoom.broadcastPlayerUpdate($member);
			}
	}

	public void disbandChannel()
	{
		broadCast(SystemMsg.THE_COMMAND_CHANNEL_HAS_BEEN_DISBANDED);
		for(Party party : _commandChannelParties)
		{
			party.setCommandChannel(null);
			party.broadCast(ExCloseMPCCPacket.STATIC);
		}
		if(_matchingRoom != null)
			_matchingRoom.disband();
		_commandChannelParties.clear();
		_commandChannelLeader = null;
	}

	@Override
	public int getMemberCount()
	{
		int count = 0;
		for(Party party : _commandChannelParties)
			count += party.getMemberCount();
		return count;
	}

	@Override
	public void broadCast(IBroadcastPacket... gsp)
	{
		for(Party party : _commandChannelParties)
			party.broadCast(gsp);
	}

	public void broadcastToChannelPartyLeaders(L2GameServerPacket gsp)
	{
		for(Party party : _commandChannelParties)
		{
			Player leader = party.getPartyLeader();
			if(leader != null)
				leader.sendPacket(gsp);
		}
	}

	public List<Party> getParties()
	{
		return _commandChannelParties;
	}

	public List<Player> getMembers()
	{
		List<Player> members = new ArrayList<>(_commandChannelParties.size());
		for(Party party : getParties())
			members.addAll(party.getPartyMembers());
		return members;
	}

	@Override
	public Player getGroupLeader()
	{
		return getChannelLeader();
	}

	@Override
	public Iterator<Player> iterator()
	{
		List<Iterator<Player>> iterators = getParties().stream()
				.map(p -> p.getPartyMembers().iterator())
				.collect(Collectors.toCollection(() -> new ArrayList<>(_commandChannelParties.size())));
		return Iterators.concat(iterators.iterator());
	}

	public int getLevel()
	{
		return _commandChannelLvl;
	}

	public void setChannelLeader(Player newLeader)
	{
		_commandChannelLeader = newLeader;
		broadCast(new SystemMessage(1589).addString(newLeader.getName()));
	}

	public Player getChannelLeader()
	{
		return _commandChannelLeader;
	}

	public boolean isLeaderCommandChannel(Player player)
	{
		return _commandChannelLeader == player;
	}

	public boolean meetRaidWarCondition(NpcFriendInstance npc)
	{
		if(!npc.isRaid())
			return false;
		int npcId = npc.getNpcId();
		switch(npcId)
		{
			case 29001:
			case 29006:
			case 29014:
			case 29022:
			{
				return getMemberCount() > 36;
			}
			case 29020:
			{
				return getMemberCount() > 56;
			}
			case 29019:
			{
				return getMemberCount() > 225;
			}
			case 29028:
			{
				return getMemberCount() > 99;
			}
			default:
			{
				return getMemberCount() > 18;
			}
		}
	}

	private void refreshLevel()
	{
		_commandChannelLvl = 0;
		for(Party pty : _commandChannelParties)
			if(pty.getLevel() > _commandChannelLvl)
				_commandChannelLvl = pty.getLevel();
	}

	public static boolean checkAuthority(Player creator)
	{
		Party party = creator.getParty();
		Clan clan = creator.getClan();
		if(party == null || !party.isLeader(creator) || clan == null || clan.getSkillLevel(391) < 1 || creator.getPledgeRank().ordinal() < PledgeRank.WISEMAN.ordinal())
		{
			creator.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_AUTHORITY_TO_INVITE_SOMEONE_TO_THE_COMMAND_CHANNEL);
			return false;
		}
		boolean haveSkill = creator.getSkillLevel(391) > 0;
		boolean haveItem = creator.getInventory().getItemByItemId(8871) != null;
		if(!haveSkill && !haveItem)
		{
			creator.sendPacket(SystemMsg.YOU_CAN_NO_LONGER_SET_UP_A_COMMAND_CHANNEL);
			return false;
		}
		return true;
	}

	public MatchingRoom getMatchingRoom()
	{
		return _matchingRoom;
	}

	public void setMatchingRoom(MatchingRoom matchingRoom)
	{
		_matchingRoom = matchingRoom;
	}
}
