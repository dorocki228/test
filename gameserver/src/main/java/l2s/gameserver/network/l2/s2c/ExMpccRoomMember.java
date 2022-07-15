package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import l2s.gameserver.instancemanager.MatchingRoomManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.matching.MatchingRoom;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author VISTALL
 */
public class ExMpccRoomMember implements IClientOutgoingPacket
{
	private int _type;
	private List<MpccRoomMemberInfo> _members = Collections.emptyList();

	public ExMpccRoomMember(MatchingRoom room, Player player)
	{
		_type = room.getMemberType(player);
		_members = new ArrayList<MpccRoomMemberInfo>(room.getPlayers().size());

		for(Player member : room.getPlayers())
			_members.add(new MpccRoomMemberInfo(member, room.getMemberType(member)));
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_MPCC_ROOM_MEMBER.writeId(packetWriter);
		packetWriter.writeD(_type);
		packetWriter.writeD(_members.size());
		for(MpccRoomMemberInfo member : _members)
		{
			packetWriter.writeD(member.objectId);
			packetWriter.writeS(member.name);
			packetWriter.writeD(member.classId);
			packetWriter.writeD(member.level);
			packetWriter.writeD(member.location);
			packetWriter.writeD(member.memberType);
		}

		return true;
	}

	static class MpccRoomMemberInfo
	{
		public final int objectId;
		public final int classId;
		public final int level;
		public final int location;
		public final int memberType;
		public final String name;

		public MpccRoomMemberInfo(Player member, int type)
		{
			this.objectId = member.getObjectId();
			this.name = member.getName();
			this.classId = member.getClassId().ordinal();
			this.level = member.getLevel();
			this.location = MatchingRoomManager.getInstance().getLocation(member);
			this.memberType = type;
		}
	}
}