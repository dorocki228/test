package  l2s.Phantoms.templates;


import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import  l2s.Phantoms.enums.Condition;
import  l2s.Phantoms.objects.PCondition;
import  l2s.gameserver.templates.StatsSet;

public class PhantomItem
{
	@XStreamAlias("id")
	@XStreamAsAttribute
	private int id;
	@XStreamAlias("delay")
	@XStreamAsAttribute
	private int delay;
	
	@XStreamAlias("condition")
	@XStreamAsAttribute
	private Map <Condition,PCondition> condition = new HashMap <Condition,PCondition>();
	
	public PhantomItem(StatsSet set)
	{
		id = set.getInteger("id");
		delay = set.getInteger("delay");
		String str_condition = set.getString("condition");
		
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
						if (cond.length == 2)
							condition.put(Condition.valueOf(cond[0]), new PCondition(Condition.valueOf(cond[0]), cond[1]));
				}
			}
		}
	}
	
	public Map <Condition,PCondition> getCondition()
	{
		return condition;
	}
	
	public int getId()
	{
		return id;
	}
	
	public int getDelay()
	{
		return delay;
	}
}
