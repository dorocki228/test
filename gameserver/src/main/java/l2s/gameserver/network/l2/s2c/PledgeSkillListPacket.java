package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.SubUnit;
import l2s.gameserver.skills.SkillEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PledgeSkillListPacket extends L2GameServerPacket
{
	private List<SkillInfo> _allSkills;
	private final List<UnitSkillInfo> _unitSkills;

	public PledgeSkillListPacket(Clan clan)
	{
		_allSkills = Collections.emptyList();
		_unitSkills = new ArrayList<>();
		Collection<SkillEntry> skills = clan.getSkills();
		_allSkills = new ArrayList<>(skills.size());
		for(SkillEntry sk : skills)
			_allSkills.add(new SkillInfo(sk.getId(), sk.getLevel()));
		for(SubUnit subUnit : clan.getAllSubUnits())
			for(SkillEntry sk2 : subUnit.getSkills())
				_unitSkills.add(new UnitSkillInfo(subUnit.getType(), sk2.getId(), sk2.getLevel()));
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_allSkills.size());
		writeD(_unitSkills.size());
		for(SkillInfo info : _allSkills)
		{
			writeD(info._id);
			writeD(info._level);
		}
		for(UnitSkillInfo info2 : _unitSkills)
		{
			writeD(info2._type);
			writeD(info2._id);
			writeD(info2._level);
		}
	}

	static class SkillInfo
	{
		public int _id;
		public int _level;

		public SkillInfo(int id, int level)
		{
			_id = id;
			_level = level;
		}
	}

	static class UnitSkillInfo extends SkillInfo
	{
		private final int _type;

		public UnitSkillInfo(int type, int id, int level)
		{
			super(id, level);
			_type = type;
		}
	}
}
