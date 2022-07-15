package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.SubUnit;
import l2s.gameserver.model.pledge.UnitMember;
import l2s.gameserver.network.l2.OutgoingPackets;

public class PledgeShowMemberListUpdatePacket implements IClientOutgoingPacket
{
	private String _name;
	private int _lvl;
	private int _classId;
	private int _sex;
	private int _isOnline;
	private int _objectId;
	private int _pledgeType;
	private int _isApprentice;
	private int _attendance;

	public PledgeShowMemberListUpdatePacket(final Player player)
	{
		_name = player.getName();
		_lvl = player.getLevel();
		_classId = player.getClassId().getId();
		_sex = player.getSex().ordinal();
		_objectId = player.getObjectId();
		_isOnline = player.isOnline() ? 1 : 0;
		_pledgeType = player.getPledgeType();
		SubUnit subUnit = player.getSubUnit();
		UnitMember member = subUnit == null ? null : subUnit.getUnitMember(_objectId);
		if(member != null)
		{
			_isApprentice = member.hasSponsor() ? 1 : 0;
			_attendance = member.getAttendanceType().ordinal();
		}
	}

	public PledgeShowMemberListUpdatePacket(final UnitMember cm)
	{
		_name = cm.getName();
		_lvl = cm.getLevel();
		_classId = cm.getClassId();
		_sex = cm.getSex();
		_objectId = cm.getObjectId();
		_isOnline = cm.isOnline() ? 1 : 0;
		_pledgeType = cm.getPledgeType();
		_isApprentice = cm.hasSponsor() ? 1 : 0;
		_attendance = cm.getAttendanceType().ordinal();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.PLEDGE_SHOW_MEMBER_LIST_UPDATE.writeId(packetWriter);
		packetWriter.writeS(_name);
		packetWriter.writeD(_lvl);
		packetWriter.writeD(_classId);
		packetWriter.writeD(_sex);
		packetWriter.writeD(_objectId);
		packetWriter.writeD(_isOnline); // 1=online 0=offline
		packetWriter.writeD(_pledgeType);
		packetWriter.writeD(_isApprentice); // does a clan member have a sponsor
		packetWriter.writeC(_attendance);

		return true;
	}
}