package  l2s.Phantoms.templates;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import  l2s.commons.util.Rnd;
import  l2s.gameserver.model.Skill;

public class SkillsGroup
{
	@XStreamAlias("skill")
	@XStreamImplicit 
	private List <PhantomSkill> skills;
	private Map <Integer, List<PhantomSkill>> rnd_skills;
	private int chanceCastToMonster; 
	private int chanceGroupCast; 
	
	public SkillsGroup(int mobChance, int chance_group)
	{
		skills = new ArrayList <PhantomSkill>();
		rnd_skills = new HashMap <Integer, List<PhantomSkill>>();
		chanceCastToMonster = mobChance;
		chanceGroupCast = chance_group;
	}
	
	public SkillsGroup()
	{
		skills = new ArrayList <PhantomSkill>();
		rnd_skills = new HashMap <Integer, List<PhantomSkill>>();
	}

	public void addSkill(PhantomSkill skill)
	{
		skills.add(skill);
	}
	
	public void addAllSkill(List <PhantomSkill> _skills)
	{
		skills.addAll(_skills);
	}
	
	public boolean isContains(Skill skill)
	{
		if (skills == null || skills.isEmpty())
			return false;
		
		for(PhantomSkill sk : skills)
			if (sk.getId() == skill.getId())
				return true;
		return false;
	}
	
	public PhantomSkill getRandomSkill()
	{
		if (skills == null || skills.isEmpty())
			return null;
		
		return Rnd.get(skills);
	}
	
	public List <PhantomSkill> getAllSkills()
	{
		return skills;
	}
	
	public void setChanceCastToMonster(int chance)
	{
		chanceCastToMonster = chance;
	}
	
	public int getChanceCastToMonster()
	{
		return chanceCastToMonster;
	}
	
	public int getGroupChanceCast()
	{
		return chanceGroupCast;
	}

	public void addRndSkill(int tmp_rnd, List<PhantomSkill> rnd_skill)
	{
		rnd_skills.put(tmp_rnd, rnd_skill);
	}
	
	public Map<Integer, List<PhantomSkill>> getRndSkill()
	{
		return rnd_skills;
	}
	
}
