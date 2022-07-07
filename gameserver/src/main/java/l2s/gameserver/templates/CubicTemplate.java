package l2s.gameserver.templates;

import gnu.trove.map.hash.TIntIntHashMap;
import l2s.gameserver.model.Skill;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CubicTemplate
{
	private final int _id;
	private final int _level;
	private final int _duration;
	private final int _delay;
	private final List<Map.Entry<Integer, List<SkillInfo>>> _skills;

	public CubicTemplate(int id, int level, int duration, int delay)
	{
		_skills = new ArrayList<>(3);
		_id = id;
		_level = level;
		_duration = duration;
		_delay = delay;
	}

	public void putSkills(int chance, List<SkillInfo> skill)
	{
		_skills.add(Map.entry(chance, skill));
	}

	public Iterable<Map.Entry<Integer, List<SkillInfo>>> getSkills()
	{
		return _skills;
	}

	public int getDuration()
	{
		return _duration;
	}

	public int getDelay()
	{
		return _delay;
	}

	public int getId()
	{
		return _id;
	}

	public int getLevel()
	{
		return _level;
	}

	public static class SkillInfo
	{
		private final Skill _skill;
		private final int _chance;
		private final ActionType _actionType;
		private final boolean _canAttackDoor;
		private final TIntIntHashMap _chanceList;

		public SkillInfo(Skill skill, int chance, ActionType actionType, boolean canAttackDoor, TIntIntHashMap set)
		{
			_skill = skill;
			_chance = chance;
			_actionType = actionType;
			_canAttackDoor = canAttackDoor;
			_chanceList = set;
		}

		public int getChance()
		{
			return _chance;
		}

		public ActionType getActionType()
		{
			return _actionType;
		}

		public Skill getSkill()
		{
			return _skill;
		}

		public boolean isCanAttackDoor()
		{
			return _canAttackDoor;
		}

		public int getChance(int a)
		{
			return _chanceList.get(a);
		}
	}

	public enum ActionType
	{
		ATTACK,
		BUFF,
		DEBUFF,
		CANCEL,
		HEAL,
		MANA
    }
}
