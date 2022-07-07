package l2s.gameserver.model.matching;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExClosePartyRoomPacket;
import l2s.gameserver.network.l2.s2c.ExPartyRoomMemberPacket;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.PartyRoomInfoPacket;

public class PartyMatchingRoom extends MatchingRoom
{
	public PartyMatchingRoom(Player leader, int minLevel, int maxLevel, int maxMemberSize, int lootType, String topic)
	{
		super(leader, minLevel, maxLevel, maxMemberSize, lootType, topic);
		leader.broadcastCharInfo();
	}

	@Override
	public SystemMsg notValidMessage()
	{
		return SystemMsg.YOU_DO_NOT_MEET_THE_REQUIREMENTS_TO_ENTER_THAT_PARTY_ROOM;
	}

	@Override
	public SystemMsg enterMessage()
	{
		return SystemMsg.C1_HAS_ENTERED_THE_PARTY_ROOM;
	}

	@Override
	public SystemMsg exitMessage(boolean toOthers, boolean kick)
	{
		if(toOthers)
			return kick ? SystemMsg.C1_HAS_BEEN_KICKED_FROM_THE_PARTY_ROOM : SystemMsg.C1_HAS_LEFT_THE_PARTY_ROOM;
		return kick ? SystemMsg.YOU_HAVE_BEEN_OUSTED_FROM_THE_PARTY_ROOM : SystemMsg.YOU_HAVE_EXITED_THE_PARTY_ROOM;
	}

	@Override
	public SystemMsg closeRoomMessage()
	{
		return SystemMsg.THE_PARTY_ROOM_HAS_BEEN_DISBANDED;
	}

	@Override
	public SystemMsg changeLeaderMessage()
	{
		return SystemMsg.THE_LEADER_OF_THE_PARTY_ROOM_HAS_CHANGED;
	}

	@Override
	public L2GameServerPacket closeRoomPacket()
	{
		return ExClosePartyRoomPacket.STATIC;
	}

	@Override
	public L2GameServerPacket infoRoomPacket()
	{
		return new PartyRoomInfoPacket(this);
	}

	@Override
	public L2GameServerPacket addMemberPacket(Player $member, Player active)
	{
		return membersPacket($member);
	}

	@Override
	public L2GameServerPacket removeMemberPacket(Player $member, Player active)
	{
		return membersPacket($member);
	}

	@Override
	public L2GameServerPacket updateMemberPacket(Player $member, Player active)
	{
		return membersPacket($member);
	}

	@Override
	public L2GameServerPacket membersPacket(Player active)
	{
		return new ExPartyRoomMemberPacket(this, active);
	}

	@Override
	public int getType()
	{
		return MatchingRoom.PARTY_MATCHING;
	}

	@Override
	public int getMemberType(Player member)
	{
		return member.equals(_leader) ? MatchingRoom.ROOM_MASTER : member.getParty() != null && _leader.getParty() == member.getParty() ? MatchingRoom.PARTY_MEMBER : MatchingRoom.WAIT_PLAYER;
	}
}
