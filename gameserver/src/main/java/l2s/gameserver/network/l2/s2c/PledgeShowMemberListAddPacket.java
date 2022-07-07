package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.pledge.UnitMember;

public class PledgeShowMemberListAddPacket extends L2GameServerPacket
{
	private final PledgePacketMember _member;

	public PledgeShowMemberListAddPacket(UnitMember member)
	{
		_member = new PledgePacketMember(member);
	}

	@Override
	protected final void writeImpl()
	{
		writeS(_member._name);
		writeD(_member._level);
		writeD(_member._classId);
		writeD(_member._sex);
		writeD(_member._race);
		writeD(_member._online);
		writeD(_member._pledgeType);
		writeC(_member._rewardStatus);
	}

	private class PledgePacketMember
	{
		private final String _name;
		private final int _level;
		private final int _classId;
		private final int _sex;
		private final int _race;
		private final int _online;
		private final int _pledgeType;
		private final int _rewardStatus;

		public PledgePacketMember(UnitMember m)
		{
			_name = m.getName();
			_level = m.getLevel();
			_classId = m.getClassId();
			_sex = m.getSex();
			_race = 0;
			_online = m.isOnline() ? m.getObjectId() : 0;
			_pledgeType = m.getPledgeType();
			_rewardStatus = m.getClanRewardStatus();
		}
	}
}
