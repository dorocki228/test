package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.instancemanager.MatchingRoomManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.matching.MatchingRoom;
import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author VISTALL
 */
public class ExManageMpccRoomMember implements IClientOutgoingPacket
{
	public static int ADD_MEMBER = 0;
	public static int UPDATE_MEMBER = 1;
	public static int REMOVE_MEMBER = 2;

	private int _type;
	private MpccRoomMemberInfo _memberInfo;

	public ExManageMpccRoomMember(int type, MatchingRoom room, Player target)
	{
		_type = type;
		_memberInfo = (new MpccRoomMemberInfo(target, room.getMemberType(target)));
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_MANAGE_MPCC_ROOM_MEMBER.writeId(packetWriter);
		packetWriter.writeD(_type);
		packetWriter.writeD(_memberInfo.objectId);
		packetWriter.writeS(_memberInfo.name);
		packetWriter.writeD(_memberInfo.classId);
		packetWriter.writeD(_memberInfo.level);
		packetWriter.writeD(_memberInfo.location);
		packetWriter.writeD(_memberInfo.memberType);

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