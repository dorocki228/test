package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.SubUnit;
import l2s.gameserver.model.pledge.UnitMember;
import l2s.gameserver.network.l2.OutgoingPackets;

import java.util.ArrayList;
import java.util.List;

public class PledgeShowMemberListAllPacket implements IClientOutgoingPacket
{
	private int _clanObjectId, _clanCrestId, _level, _rank, _reputation, _allianceObjectId, _allianceCrestId;
	private int _hasCastle, _hasClanHall, _hasInstantClanHall;
	private boolean _isDisbanded, _atClanWar;
	private String _unitName, _leaderName, _allianceName;
	private int _pledgeType;
	private List<PledgePacketMember> _members;

	public PledgeShowMemberListAllPacket(Clan clan, final SubUnit sub)
	{
		_pledgeType = sub.getType();
		_clanObjectId = clan.getClanId();
		_unitName = sub.getName();
		_leaderName = sub.getLeaderName();
		_clanCrestId = clan.getCrestId();
		_level = clan.getLevel();
		_hasCastle = clan.getCastle();

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

		_members = new ArrayList<PledgePacketMember>(sub.size());

		for(UnitMember m : sub.getUnitMembers())
			_members.add(new PledgePacketMember(m));
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.PLEDGE_SHOW_MEMBER_LIST_ALL.writeId(packetWriter);
		packetWriter.writeD(_pledgeType == Clan.SUBUNIT_MAIN_CLAN ? 0 : 1);
		packetWriter.writeD(_clanObjectId);
		packetWriter.writeD(Config.REQUEST_ID);
		packetWriter.writeD(_pledgeType);
		packetWriter.writeS(_unitName);
		packetWriter.writeS(_leaderName);

		packetWriter.writeD(_clanCrestId); // crest id .. is used again
		packetWriter.writeD(_level);
		packetWriter.writeD(_hasCastle);
		if(_hasInstantClanHall > 0)
		{
			packetWriter.writeD(1);
			packetWriter.writeD(_hasInstantClanHall);
		}
		else if(_hasClanHall != 0)
		{
			packetWriter.writeD(0);
			packetWriter.writeD(_hasClanHall);
		}
		else
		{
			packetWriter.writeD(0);
			packetWriter.writeD(0);
		}
		packetWriter.writeD(0x00); // Has Fortress
		packetWriter.writeD(_rank);
		packetWriter.writeD(_reputation);
		packetWriter.writeD(_isDisbanded ? 3 : 0);
		packetWriter.writeD(0x00);
		packetWriter.writeD(_allianceObjectId);
		packetWriter.writeS(_allianceName);
		packetWriter.writeD(_allianceCrestId);
		packetWriter.writeD(_atClanWar);
		packetWriter.writeD(0x00);//territory Id

		packetWriter.writeD(_members.size());
		for(PledgePacketMember m : _members)
		{
			packetWriter.writeS(m._name);
			packetWriter.writeD(m._level);
			packetWriter.writeD(m._classId);
			packetWriter.writeD(m._sex);
			packetWriter.writeD(m._race);
			packetWriter.writeD(m._online);
			packetWriter.writeD(m._hasSponsor ? 1 : 0);
			packetWriter.writeC(m._attendance);
		}

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
		private boolean _hasSponsor;
		private int _attendance;

		public PledgePacketMember(UnitMember m)
		{
			_name = m.getName();
			_level = m.getLevel();
			_classId = m.getClassId();
			_sex = m.getSex();
			_race = 0; //TODO m.getRace()
			_online = m.isOnline() ? m.getObjectId() : 0;
			_hasSponsor = m.getSponsor() != 0;
			_attendance = m.getAttendanceType().ordinal();
		}
	}
}