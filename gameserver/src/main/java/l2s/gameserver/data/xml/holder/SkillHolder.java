package l2s.gameserver.data.xml.holder;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.model.Skill;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.utils.SkillUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class SkillHolder extends AbstractHolder
{
	private static final SkillHolder _instance = new SkillHolder();
	private final TIntObjectMap<SkillEntry> _skills;
	private final TIntObjectMap<List<SkillEntry>> _skillsById;

	public SkillHolder()
	{
		_skills = new TIntObjectHashMap<>();
		_skillsById = new TIntObjectHashMap<>();
	}

	public static SkillHolder getInstance()
	{
		return _instance;
	}

	public void addSkill(Skill skill)
	{
		SkillEntry skillEntry = new SkillEntry(SkillEntryType.NONE, skill);
		_skills.put(skillEntry.hashCode(), skillEntry);
		List<SkillEntry> skills = _skillsById.get(skillEntry.getId());
		if(skills == null)
		{
			skills = new ArrayList<>();
			_skillsById.put(skillEntry.getId(), skills);
		}
		skills.add(skillEntry);
	}

	public SkillEntry getSkillEntry(int id, int level)
	{
		return _skills.get(SkillUtils.generateSkillHashCode(id, level));
	}

	@Deprecated
	public Skill getSkill(int id, int level)
	{
		SkillEntry skillEntry = getSkillEntry(id, level);
		if(skillEntry != null)
			return skillEntry.getTemplate();
		return null;
	}

	public List<SkillEntry> getSkills(int id)
	{
		return _skillsById.get(id);
	}

	@Override
	public int size()
	{
		return _skills.size();
	}

	@Override
	public void clear()
	{
		_skills.clear();
	}

	public Collection<SkillEntry> getAllSkills()
	{
		return _skills.valueCollection();
	}

	@Override
	protected void process() {
		_skills.valueCollection().forEach(s-> s.getTemplate().setup());
	}
}
