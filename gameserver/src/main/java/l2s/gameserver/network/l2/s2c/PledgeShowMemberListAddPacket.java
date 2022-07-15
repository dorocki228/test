package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.pledge.UnitMember;
import l2s.gameserver.network.l2.OutgoingPackets;

public class PledgeShowMemberListAddPacket implements IClientOutgoingPacket
{
	private PledgePacketMember _member;

	public PledgeShowMemberListAddPacket(UnitMember member)
	{
		_member = new PledgePacketMember(member);
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.PLEDGE_SHOW_MEMBER_LIST_ADD.writeId(packetWriter);
		packetWriter.writeS(_member._name);
		packetWriter.writeD(_member._level);
		packetWriter.writeD(_member._classId);
		packetWriter.writeD(_member._sex);
		packetWriter.writeD(_member._race);
		packetWriter.writeD(_member._online);
		packetWriter.writeD(_member._pledgeType);
		packetWriter.writeC(_member._attendance);

		return true;
	}

	private static class PledgePacketMember
	{
		private String _name;
		private int _level;
		private int _classId;
		private int _sex;
		private int _race;
		private int _online;
		private int _pledgeType;
		private int _attendance;

		public PledgePacketMember(UnitMember m)
		{
			_name = m.getName();
			_level = m.getLevel();
			_classId = m.getClassId();
			_sex = m.getSex();
			_race = 0; //TODO m.getRace()
			_online = m.isOnline() ? m.getObjectId() : 0;
			_pledgeType = m.getPledgeType();
			_attendance = m.getAttendanceType().ordinal();
		}
	}
}