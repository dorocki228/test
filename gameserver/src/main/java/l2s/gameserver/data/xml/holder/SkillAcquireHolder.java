package l2s.gameserver.data.xml.holder;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.SkillLearn;
import l2s.gameserver.model.base.AcquireType;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.pledge.SubUnit;
import l2s.gameserver.skills.SkillEntry;

import java.util.*;

public class SkillAcquireHolder extends AbstractHolder
{
	private static final SkillAcquireHolder _instance = new SkillAcquireHolder();
	private final TIntObjectMap<Set<SkillLearn>> _normalSkillTree;
	private final TIntObjectMap<Set<SkillLearn>> _generalSkillTree;
	private final TIntObjectMap<Set<SkillLearn>> _fishingSkillTree;

	private final Set<SkillLearn> _pledgeSkillTree;
	private final Set<SkillLearn> _subUnitSkillTree;
	private final Set<SkillLearn> _gmSkillTree;
	private final Set<SkillLearn> _heroSkillTree;

	public SkillAcquireHolder()
	{
		_normalSkillTree = new TIntObjectHashMap<>();
		_generalSkillTree = new TIntObjectHashMap<>();
		_fishingSkillTree = new TIntObjectHashMap<>();
		_pledgeSkillTree = new HashSet<>();
		_subUnitSkillTree = new HashSet<>();
		_gmSkillTree = new HashSet<>();
		_heroSkillTree = new HashSet<>();
	}

	public static SkillAcquireHolder getInstance()
	{
		return _instance;
	}

	private Collection<SkillLearn> getSkills(Player player, AcquireType type, SubUnit subUnit)
	{
		Collection<SkillLearn> skills = null;
		switch(type)
		{
			case NORMAL:
			{
				skills = _normalSkillTree.get(player.getActiveClassId());
				if(skills == null)
				{
					info("Skill tree for class " + player.getActiveClassId() + " is not defined !");
					return Collections.emptyList();
				}
				break;
			}
			case CLAN:
			{
				skills = _pledgeSkillTree;
				if(skills == null)
				{
					info("Pledge skill tree is not defined !");
					return Collections.emptyList();
				}
				return checkLearnsConditions(player, skills, player.getClan() != null ? player.getClan().getLevel() : 0);
			}
			case SUB_UNIT:
			{
				skills = _subUnitSkillTree;
				if(skills == null)
				{
					info("Sub-unit skill tree is not defined !");
					return Collections.emptyList();
				}
				return checkLearnsConditions(player, skills, player.getClan() != null ? player.getClan().getLevel() : 0);
			}
			case GM:
			{
				skills = _gmSkillTree;
				break;
			}
			case HERO:
			{
				skills = _heroSkillTree;
				if(skills == null)
				{
					info("Hero skill tree is not defined !");
					return Collections.emptyList();
				}
				break;
			}
			case FISHING:
			{
				skills = _fishingSkillTree.get(player.getRace().ordinal());
				if(skills == null)
				{
					info("Fishing skill tree is not defined !");
					return Collections.emptyList();
				}
				break;
			}
			default:
			{
				return Collections.emptyList();
			}
		}
		if(player == null)
			return skills;
		return checkLearnsConditions(player, skills, player.getLevel());
	}

	public Collection<SkillLearn> getAvailableSkills(Player player, AcquireType type)
	{
		return getAvailableSkills(player, type, null);
	}

	public Collection<SkillLearn> getAvailableSkills(Player player, AcquireType type, SubUnit subUnit)
	{
		Collection<SkillLearn> skills = getSkills(player, type, subUnit);
		switch(type)
		{
			case CLAN:
			{
				Collection<SkillEntry> clanSkills = player.getClan().getSkills();
				return getAvaliableList(skills, clanSkills.toArray(new SkillEntry[0]));
			}
			case SUB_UNIT:
			{
				Collection<SkillEntry> subUnitSkills = subUnit.getSkills();
				return getAvaliableList(skills, subUnitSkills.toArray(new SkillEntry[0]));
			}
			case FISHING:
			{
				skills = _fishingSkillTree.get(player.getRace().ordinal());
				return getAvaliableList(skills, player.getAllSkillsArray());
			}
			default:
			{
				if(player == null)
					return skills;
				return getAvaliableList(skills, player.getAllSkillsArray());
			}
		}
	}

	private Collection<SkillLearn> getAvaliableList(Collection<SkillLearn> skillLearns, SkillEntry[] skills)
	{
		TIntIntMap skillLvls = new TIntIntHashMap();
		for(SkillEntry skillEntry : skills)
			if(skillEntry != null)
				skillLvls.put(skillEntry.getId(), skillEntry.getLevel());

		Map<Integer, SkillLearn> skillLearnMap = new TreeMap<>();
		for(SkillLearn temp : skillLearns)
		{
			int skillId = temp.getId();
			int skillLvl = temp.getLevel();
			if(!skillLvls.containsKey(skillId) && skillLvl == 1 || skillLvls.containsKey(skillId) && skillLvl - skillLvls.get(skillId) == 1)
				skillLearnMap.put(temp.getId(), temp);
		}

		return skillLearnMap.values();
	}

	public Collection<SkillLearn> getAvailableNextLevelsSkills(Player player, AcquireType type)
	{
		return getAvailableNextLevelsSkills(player, type, null);
	}

	public Collection<SkillLearn> getAvailableNextLevelsSkills(Player player, AcquireType type, SubUnit subUnit)
	{
		Collection<SkillLearn> skills = getSkills(player, type, subUnit);
		switch(type)
		{
			case CLAN:
			{
				Collection<SkillEntry> clanSkills = player.getClan().getSkills();
				return getAvailableNextLevelsList(skills, clanSkills.toArray(new SkillEntry[0]));
			}
			case SUB_UNIT:
			{
				Collection<SkillEntry> subUnitSkills = subUnit.getSkills();
				return getAvailableNextLevelsList(skills, subUnitSkills.toArray(new SkillEntry[0]));
			}
			default:
			{
				if(player == null)
					return skills;

				return getAvailableNextLevelsList(skills, player.getAllSkillsArray());
			}
		}
	}

	private Collection<SkillLearn> getAvailableNextLevelsList(Collection<SkillLearn> skillLearns, SkillEntry[] skills)
	{
		TIntIntMap skillLvls = new TIntIntHashMap();
		for(SkillEntry skillEntry : skills)
			if(skillEntry != null)
				skillLvls.put(skillEntry.getId(), skillEntry.getLevel());

		Set<SkillLearn> skillLearnsList = new HashSet<>();
		for(SkillLearn temp : skillLearns)
		{
			int skillId = temp.getId();
			int skillLvl = temp.getLevel();
			if(!skillLvls.containsKey(skillId) || skillLvls.containsKey(skillId) && skillLvl > skillLvls.get(skillId))
				skillLearnsList.add(temp);
		}
		return skillLearnsList;
	}

	public Collection<SkillLearn> getAvailableMaxLvlSkills(Player player, AcquireType type)
	{
		return getAvailableMaxLvlSkills(player, type, null);
	}

	public Collection<SkillLearn> getAvailableMaxLvlSkills(Player player, AcquireType type, SubUnit subUnit)
	{
		Collection<SkillLearn> skills = getSkills(player, type, subUnit);
		switch(type)
		{
			case CLAN:
			{
				Collection<SkillEntry> clanSkills = player.getClan().getSkills();
				return getAvaliableMaxLvlSkillList(skills, clanSkills.toArray(new SkillEntry[0]));
			}
			case SUB_UNIT:
			{
				Collection<SkillEntry> subUnitSkills = subUnit.getSkills();
				return getAvaliableMaxLvlSkillList(skills, subUnitSkills.toArray(new SkillEntry[0]));
			}
			default:
			{
				if(player == null)
					return skills;
				return getAvaliableMaxLvlSkillList(skills, player.getAllSkillsArray());
			}
		}
	}

	private Collection<SkillLearn> getAvaliableMaxLvlSkillList(Collection<SkillLearn> skillLearns, SkillEntry[] skills)
	{
		Map<Integer, SkillLearn> skillLearnMap = new TreeMap<>();
		for(SkillLearn temp : skillLearns)
		{
			int skillId = temp.getId();
			if(!skillLearnMap.containsKey(skillId) || temp.getLevel() > skillLearnMap.get(skillId).getLevel())
				skillLearnMap.put(skillId, temp);
		}

		for(SkillEntry skillEntry : skills)
		{
			int skillId = skillEntry.getId();
			if(skillLearnMap.containsKey(skillId))
			{
				SkillLearn temp = skillLearnMap.get(skillId);
				if(temp != null)
					if(temp.getLevel() <= skillEntry.getLevel())
						skillLearnMap.remove(skillId);
			}
		}
		return skillLearnMap.values();
	}

	public Collection<SkillLearn> getAcquirableSkillListByClass(Player player)
	{
        Collection<SkillLearn> skills = _normalSkillTree.get(player.getActiveClassId());
		Collection<SkillLearn> currentLvlSkills = getAvaliableList(skills, player.getAllSkillsArray());

		currentLvlSkills = checkLearnsConditions(player, currentLvlSkills, player.getLevel());
        Map<Integer, SkillLearn> skillListMap = new TreeMap<>();
        for(SkillLearn temp : currentLvlSkills)
			if(!temp.isFreeAutoGet())
				skillListMap.put(temp.getId(), temp);

		Collection<SkillLearn> nextLvlsSkills = getAvaliableList(skills, player.getAllSkillsArray());
		nextLvlsSkills = checkLearnsConditions(player, nextLvlsSkills, player.getMaxLevel());
		for(SkillLearn temp2 : nextLvlsSkills)
			if(!temp2.isFreeAutoGet() && !skillListMap.containsKey(temp2.getId()))
				skillListMap.put(temp2.getId(), temp2);

		return skillListMap.values();
	}

	public SkillLearn getSkillLearn(Player player, int id, int level, AcquireType type)
	{
		Collection<SkillLearn> skills = null;
		switch(type)
		{
			case NORMAL:
			{
				skills = _normalSkillTree.get(player.getActiveClassId());
				break;
			}
			case CLAN:
			{
				skills = _pledgeSkillTree;
				break;
			}
			case SUB_UNIT:
			{
				skills = _subUnitSkillTree;
				break;
			}
			case FISHING:
			{
				skills = _fishingSkillTree.get(player.getRace().ordinal());
				break;
			}
			case GENERAL:
			{
				skills = _generalSkillTree.get(player.getActiveClassId());
				break;
			}
			case GM:
			{
				skills = _gmSkillTree;
				break;
			}
			case HERO:
			{
				skills = _heroSkillTree;
				break;
			}
			default:
			{
				return null;
			}
		}

		if(skills == null)
			return null;

		for(SkillLearn temp : skills)
			if(temp.isOfRace(player.getRace()) && temp.getLevel() == level && temp.getId() == id)
				return temp;

		return null;
	}

	@Deprecated
	public boolean isSkillPossible(Player player, Skill skill, AcquireType type)
	{
		switch(type)
		{
			case CLAN:
			case SUB_UNIT:
			{
				if(player.getClan() == null)
					return false;
				break;
			}
			case HERO:
			{
				if(!player.isHero() && !player.isBaseClassActive() && !player.isCustomHero())
					return false;
				break;
			}
			case GM:
			{
				if(!player.isGM())
					return false;
				break;
			}
		}
		SkillLearn learn = getSkillLearn(player, skill.getId(), skill.getLevel(), type);
		return learn != null && learn.testCondition(player);
	}

	public boolean isSkillPossible(Player player, SkillEntry skill, AcquireType type)
	{
		switch(type)
		{
			case CLAN:
			case SUB_UNIT:
			{
				if(player.getClan() == null)
					return false;
				break;
			}
			case HERO:
			{
				if(!player.isHero() && !player.isBaseClassActive() && !player.isCustomHero())
					return false;
				break;
			}
			case GM:
			{
				if(!player.isGM())
					return false;
				break;
			}
		}
		SkillLearn learn = getSkillLearn(player, skill.getId(), skill.getLevel(), type);
		return learn != null && learn.testCondition(player);
	}

	public boolean isSkillPossible(Player player, Skill skill)
	{
		for(AcquireType aq : AcquireType.VALUES)
			if(isSkillPossible(player, skill, aq))
				return true;
		return false;
	}

	public boolean containsInTree(Skill skill, AcquireType type)
	{
		Collection<SkillLearn> skills = null;
		switch(type)
		{
			case NORMAL:
			{
				skills = new HashSet<>();
				for(Set<SkillLearn> temp : _normalSkillTree.valueCollection())
					skills.addAll(temp);
				break;
			}
			case CLAN:
			{
				skills = _pledgeSkillTree;
				break;
			}
			case SUB_UNIT:
			{
				skills = _subUnitSkillTree;
				break;
			}
			case GENERAL:
			{
				skills = new HashSet<>();
				for(Set<SkillLearn> temp : _generalSkillTree.valueCollection())
					skills.addAll(temp);
				break;
			}
			case GM:
			{
				skills = _gmSkillTree;
				break;
			}
			case HERO:
			{
				skills = _heroSkillTree;
				break;
			}
			case FISHING:
			{
				skills = new HashSet<>();
				for(Set<SkillLearn> temp : _fishingSkillTree.valueCollection())
					skills.addAll(temp);
				break;
			}
			default:
			{
				return false;
			}
		}

		for(SkillLearn learn : skills)
			if(learn.getId() == skill.getId() && learn.getLevel() == skill.getLevel())
				return true;
		return false;
	}

	public boolean checkLearnCondition(Player player, SkillLearn skillLearn, int level)
	{
		return skillLearn != null && (player == null || skillLearn.getMinLevel() <= level && skillLearn.isOfRace(player.getRace()) && skillLearn.testCondition(player));
	}

	private Collection<SkillLearn> checkLearnsConditions(Player player, Collection<SkillLearn> skillLearns, int level)
	{
		if(skillLearns == null)
			return null;

		if(player == null)
			return skillLearns;

		Set<SkillLearn> skills = new HashSet<>();
		for(SkillLearn skillLearn : skillLearns)
			if(checkLearnCondition(player, skillLearn, level))
				skills.add(skillLearn);

		return skills;
	}

	public void addAllNormalSkillLearns(int id, Set<SkillLearn> s)
	{
		Set<SkillLearn> set = _normalSkillTree.get(id);
		if(set == null)
			_normalSkillTree.put(id, (set = new HashSet<>()));

		set.addAll(s);
	}

	public void initNormalSkillLearns()
	{
		TIntObjectMap<Set<SkillLearn>> map = new TIntObjectHashMap<>(_normalSkillTree);

		_normalSkillTree.clear();
		for(ClassId classId : ClassId.VALUES)
		{
			if(!classId.isDummy())
			{
				int classID = classId.getId();

				Set<SkillLearn> skills = map.get(classID);
				if(skills == null)
				{
					info("Not found NORMAL skill learn for class " + classID);
				}
				else
				{
					_normalSkillTree.put(classID, skills);

					classId = classId.getParent();
					while(classId != null)
					{
						if(_normalSkillTree.containsKey(classId.getId()))
							skills.addAll(_normalSkillTree.get(classId.getId()));

						classId = classId.getParent();
					}
				}
			}
		}
	}

	public void addAllGeneralSkillLearns(int classId, Set<SkillLearn> s)
	{
		Set<SkillLearn> set = _generalSkillTree.get(classId);
		if(set == null)
			_generalSkillTree.put(classId, (set = new HashSet<>()));

		set.addAll(s);
	}

	public void initGeneralSkillLearns()
	{
		TIntObjectMap<Set<SkillLearn>> map = new TIntObjectHashMap<>(_generalSkillTree);
		Set<SkillLearn> globalList = map.remove(-1);

		_generalSkillTree.clear();
        for(ClassId classId : ClassId.VALUES)
		{
			if(!classId.isDummy())
			{
				int classID = classId.getId();

				Set<SkillLearn> tempList = map.get(classID);
				if(tempList == null)
					tempList = new HashSet<>();

                Set<SkillLearn> skills = new HashSet<>();
                _generalSkillTree.put(classID, skills);

				classId = classId.getParent();
				while(classId != null)
				{
					if(_generalSkillTree.containsKey(classId.getId()))
						tempList.addAll(_generalSkillTree.get(classId.getId()));

					classId = classId.getParent();
				}

				tempList.addAll(globalList);
                skills.addAll(tempList);
			}
		}
	}

	public void addAllSubUnitLearns(Set<SkillLearn> s)
	{
		_subUnitSkillTree.addAll(s);
	}

	public void addAllFishingLearns(int race, Set<SkillLearn> s)
	{
		_fishingSkillTree.put(race, s);
	}

	public void addAllPledgeLearns(Set<SkillLearn> s)
	{
		_pledgeSkillTree.addAll(s);
	}

	public void addAllGMLearns(Set<SkillLearn> s)
	{
		_gmSkillTree.addAll(s);
	}

	public void addAllHeroLearns(Set<SkillLearn> s)
	{
		_heroSkillTree.addAll(s);
	}

	@Override
	public void log()
	{
		info("load " + sizeTroveMap(_normalSkillTree) + " normal learns for " + _normalSkillTree.size() + " classes.");
		info("load " + sizeTroveMap(_generalSkillTree) + " general skills learns for " + _generalSkillTree.size() + " classes.");
		info("load " + _pledgeSkillTree.size() + " pledge learns.");
		info("load " + _subUnitSkillTree.size() + " sub unit learns.");
		info("load " + _gmSkillTree.size() + " GM skills learns.");
		info("load " + _heroSkillTree.size() + " hero skills learns.");
		info("load " + _fishingSkillTree.size() + " fishing learns.");

	}

	@Override
	public int size()
	{
		return 0;
	}

	@Override
	public void clear()
	{
		_normalSkillTree.clear();
		_pledgeSkillTree.clear();
		_subUnitSkillTree.clear();
		_generalSkillTree.clear();
		_gmSkillTree.clear();
		_heroSkillTree.clear();
		_fishingSkillTree.clear();
	}

	private int sizeTroveMap(TIntObjectMap<Set<SkillLearn>> a)
	{
		int i = 0;
		TIntObjectIterator<Set<SkillLearn>> iterator = a.iterator();
		while(iterator.hasNext())
		{
			iterator.advance();
			i += iterator.value().size();
		}
		return i;
	}

}
