package l2s.gameserver.data.xml.holder;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.model.Skill;
import l2s.gameserver.utils.SkillUtils;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Bonux
**/
public final class SkillHolder extends AbstractHolder
{
	private static final SkillHolder _instance = new SkillHolder();

	private final Map<Integer, Skill> _skills = new ConcurrentHashMap<>();
	private final Map<Integer, Skill> _skillsByIndex = new ConcurrentHashMap<>();
	private final Map<Integer, SortedSet<Skill>> _skillsById = new ConcurrentHashMap<>();

	private final Map<Integer, Map<Integer, Integer>> _cachedHashCodes = new ConcurrentHashMap<>();
	private final AtomicInteger _lastHashCode = new AtomicInteger(0);

	public static SkillHolder getInstance()
	{
		return _instance;
	}

	public int getHashCode(int skillId, int skillLevel)
	{
		Map<Integer, Integer> hashCodes = _cachedHashCodes.get(skillId);
		if(hashCodes == null)
		{
			hashCodes = new ConcurrentHashMap<>();
			_cachedHashCodes.put(skillId, hashCodes);
		}

		int index = hashCodes.getOrDefault(skillLevel, 0);
		if(index == 0)
		{
			index = _lastHashCode.incrementAndGet();
			hashCodes.put(skillLevel, index);
		}
		return index;
	}

	public void addSkill(Skill skill)
	{
		_skills.put(skill.hashCode(), skill);

		SortedSet<Skill> skills = _skillsById.get(skill.getId());
		if(skills == null)
		{
			skills = new TreeSet<>(Comparator.comparingInt(Skill::getLevel));
			_skillsById.put(skill.getId(), skills);
		}
		skills.add(skill);

		_skillsByIndex.put(SkillUtils.getSkillPTSHash(skill.getId(), skills.size()), skill);
	}

	public Skill getSkillByIndex(int id, int index)
	{
		return _skillsByIndex.get(SkillUtils.getSkillPTSHash(id, index));
	}

	private Skill getSkill(int hashCode)
	{
		return _skills.get(hashCode);
	}

	public Skill getSkill(int id, int level)
	{
		final Skill skill = getSkill(getHashCode(id, level));
		if (skill == null) {
			//throw new IllegalArgumentException("Can't find skill " + id + " with level " + level + ".");
		}
		return skill;
	}

	public Set<Skill> getSkills(int id)
	{
		return _skillsById.get(id);
	}

	public int getMaxSkillLevel(int id)
	{
		return _skillsById.get(id).last().getLevel();
	}

	public Collection<Skill> getSkills()
	{
		return _skills.values();
	}

	public void callInit()
	{
		for(Skill skill : getSkills())
			skill.init();
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
		_skillsByIndex.clear();
		_skillsById.clear();
	}
}
