package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import l2s.gameserver.data.xml.holder.InstantZoneHolder;
import l2s.gameserver.instancemanager.MatchingRoomManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.matching.MatchingRoom;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * Format:(ch) d d [dsdddd]
 */
public class ExPartyRoomMember implements IClientOutgoingPacket
{
	private int _type;
	private List<PartyRoomMemberInfo> _members = Collections.emptyList();

	public ExPartyRoomMember(MatchingRoom room, Player activeChar)
	{
		_type = room.getMemberType(activeChar);
		_members = new ArrayList<PartyRoomMemberInfo>(room.getPlayers().size());
		for(Player $member : room.getPlayers())
			_members.add(new PartyRoomMemberInfo($member, room.getMemberType($member)));
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_PARTY_ROOM_MEMBER.writeId(packetWriter);
		packetWriter.writeD(_type);
		packetWriter.writeD(_members.size());
		for(PartyRoomMemberInfo member_info : _members)
		{
			packetWriter.writeD(member_info.objectId);
			packetWriter.writeS(member_info.name);
			packetWriter.writeD(member_info.classId);
			packetWriter.writeD(member_info.level);
			packetWriter.writeD(member_info.location);
			packetWriter.writeD(member_info.memberType);
			packetWriter.writeD(member_info.instanceReuses.size());
			for(int i : member_info.instanceReuses)
				packetWriter.writeD(i);
		}

		return true;
	}

	static class PartyRoomMemberInfo
	{
		public final int objectId, classId, level, location, memberType;
		public final String name;
		public final List<Integer> instanceReuses;

		public PartyRoomMemberInfo(Player member, int type)
		{
			objectId = member.getObjectId();
			name = member.getName();
			classId = member.getClassId().ordinal();
			level = member.getLevel();
			location = MatchingRoomManager.getInstance().getLocation(member);
			memberType = type;
			instanceReuses = InstantZoneHolder.getInstance().getLockedInstancesList(member);
		}
	}
}