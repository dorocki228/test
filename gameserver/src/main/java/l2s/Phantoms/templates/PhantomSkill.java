package  l2s.Phantoms.templates;


import java.util.HashMap;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import  l2s.Phantoms.enums.Condition;
import  l2s.Phantoms.objects.PCondition;
import  l2s.gameserver.model.Skill.SkillTargetType;
import  l2s.gameserver.templates.StatsSet;

@XStreamAlias("skill")
public class PhantomSkill
{
	protected final Logger _log = LoggerFactory.getLogger(PhantomSkill.class);
	
	@XStreamAlias("id")
	@XStreamAsAttribute
	private int id;
	
	@XStreamAlias("id")
	@XStreamAsAttribute
	private int lvl;
	
	@XStreamAlias("name")
	@XStreamAsAttribute
	private String name;

	@XStreamAlias("ench_route")
	@XStreamAsAttribute
	private int ench_route = 0;
	
	@XStreamAlias("target")
	@XStreamAsAttribute
	private SkillTargetType target;
	
	@XStreamAlias("condition")
	@XStreamAsAttribute
	private Map <Condition,PCondition> condition = new HashMap <Condition,PCondition>();
	
	public PhantomSkill(StatsSet set)
	{
		id = set.getInteger("id");
		lvl = set.getInteger("lvl",-1);
		
		ench_route = set.getInteger("ench_route",0);

		String str_condition = set.getString("condition", "");
		
		if (str_condition != null && !str_condition.isEmpty())
		{
			String[] skill_condition = str_condition.split(";");
			if (skill_condition != null && skill_condition.length > 0)
			{
				for(String temp : skill_condition)
				{
					if (temp == null || temp.isEmpty())
						continue;
					String[] cond = temp.split(":");
					if (cond != null)
						if (cond.length == 1 || cond.length == 2)
							condition.put(Condition.valueOf(cond[0]), new PCondition(Condition.valueOf(cond[0]), cond.length == 2 ? cond[1] :null));
						else
							_log.info("skill id:"+ id+ " cond == null " + temp+" length:" + cond.length);
				}
			}
		}
			target = set.getEnum("target", SkillTargetType.class,SkillTargetType.TARGET_NONE);
	}
	
	public int ench_route()
	{
		return ench_route;
	}
	
	public Map <Condition,PCondition> getCondition()
	{
		return condition;
	}
	
	public SkillTargetType getTargetType()
	{
		return target;
	}
	
	public int getId()
	{
		return id;
	}
	public int getLvl()
	{
		return lvl;
	}
}
