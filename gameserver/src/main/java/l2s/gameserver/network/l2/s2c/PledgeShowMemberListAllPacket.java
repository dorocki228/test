package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.SubUnit;
import l2s.gameserver.model.pledge.UnitMember;

import java.util.ArrayList;
import java.util.List;

public class PledgeShowMemberListAllPacket extends L2GameServerPacket
{
	private final int _clanObjectId;
	private final int _clanCrestId;
	private final int _level;
	private final int _rank;
	private final int _reputation;
	private final int _hasFortress;
	private int _allianceObjectId;
	private int _allianceCrestId;
	private final int _hasCastle;
	private final int _hasClanHall;
	private final int _hasInstantClanHall;
	private final boolean _isDisbanded;
	private final boolean _atClanWar;
	private final String _unitName;
	private final String _leaderName;
	private String _allianceName;
	private final int _pledgeType;
	private final List<PledgePacketMember> _members;

	public PledgeShowMemberListAllPacket(Clan clan, SubUnit sub)
	{
		_pledgeType = sub.getType();
		_clanObjectId = clan.getClanId();
		_unitName = sub.getName();
		_leaderName = sub.getLeaderName();
		_clanCrestId = clan.getCrestId();
		_level = clan.getLevel();
		_hasCastle = clan.getCastle();
		_hasFortress = clan.getHasFortress();

		ClanHall clanHall = ResidenceHolder.getInstance().getResidence(ClanHall.class, clan.getHasHideout());
		if(clanHall != null)
		{
			_hasClanHall = clanHall.getId();
			_hasInstantClanHall = clanHall.getInstantZoneId();
		}
		else
		{
			_hasClanHall = 0;
			_hasInstantClanHall = 0;
		}
		_rank = clan.getRank();
		_reputation = clan.getReputationScore();
		_atClanWar = clan.isAtWar();
		_isDisbanded = clan.isPlacedForDisband();
		Alliance ally = clan.getAlliance();
		if(ally != null)
		{
			_allianceObjectId = ally.getAllyId();
			_allianceName = ally.getAllyName();
			_allianceCrestId = ally.getAllyCrestId();
		}
		_members = new ArrayList<>(sub.size());
		for(UnitMember m : sub.getUnitMembers())
			_members.add(new PledgePacketMember(m));
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_pledgeType != 0 ? 1 : 0);
		writeD(_clanObjectId);
		writeD(Config.REQUEST_ID);
		writeD(_pledgeType);
		writeS(_unitName);
		writeS(_leaderName);
		writeD(_clanCrestId);
		writeD(_level);
		writeD(_hasCastle);
		if(_hasInstantClanHall > 0)
		{
			writeD(1);
			writeD(_hasInstantClanHall);
		}
		else if(_hasClanHall != 0)
		{
			writeD(0);
			writeD(_hasClanHall);
		}
		else
		{
			writeD(0);
			writeD(0);
		}
		writeD(_hasFortress);
		writeD(_rank);
		writeD(_reputation);
		writeD(_isDisbanded ? 3 : 0);
		writeD(0);
		writeD(_allianceObjectId);
		writeS(_allianceName);
		writeD(_allianceCrestId);
		writeD(_atClanWar);
		writeD(0);
		writeD(_members.size());
		for(PledgePacketMember m : _members)
		{
			writeS(m._name);
			writeD(m._level);
			writeD(m._classId);
			writeD(m._sex);
			writeD(m._race);
			writeD(m._online);
			writeD(m._hasSponsor ? 1 : 0);
			writeC(m._hasBonus);
		}
	}

	private class PledgePacketMember
	{
		private final String _name;
		private final int _level;
		private final int _classId;
		private final int _sex;
		private final int _race;
		private final int _online;
		private final boolean _hasSponsor;
		private final int _hasBonus;

		public PledgePacketMember(UnitMember m)
		{
			_name = m.getName();
			_level = m.getLevel();
			_classId = m.getClassId();
			_sex = m.getSex();
			_race = 0;
			_online = m.isOnline() ? m.getObjectId() : 0;
			_hasSponsor = m.getSponsor() != 0;
			_hasBonus = m.getClanRewardStatus();
		}
	}
}
