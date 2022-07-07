package l2s.gameserver.templates;

import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.stats.StatTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OptionDataTemplate extends StatTemplate
{
	private final List<SkillEntry> _skills;
	private final int _id;

	public OptionDataTemplate(int id)
	{
		_skills = new ArrayList<>(0);
		_id = id;
	}

	public void addSkill(SkillEntry skill)
	{
		_skills.add(skill);
	}

	public List<SkillEntry> getSkills()
	{
		return _skills;
	}

	public int getId()
	{
		return _id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof OptionDataTemplate)) return false;
		OptionDataTemplate that = (OptionDataTemplate) o;
		return _id == that._id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(_id);
	}
}
