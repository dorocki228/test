package  l2s.Phantoms.objects.Ai;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.thoughtworks.xstream.converters.SingleValueConverter;

import  l2s.Phantoms.enums.Condition;
import  l2s.Phantoms.objects.PCondition;

public class SkillConditionConverter implements SingleValueConverter 
{
   public Map <Condition,PCondition> fromString(String str_condition) 
   {
  	Map <Condition,PCondition> condition = new HashMap <Condition,PCondition>();
  	
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
				return condition;
			}
			return condition;
   }
   

	@SuppressWarnings("unchecked")
	public String toString(Object condition) 
   {
  	 String str="";
  	 for (Entry <Condition,PCondition> cond : ((Map <Condition,PCondition>) condition).entrySet())
  	 {
  			switch (cond.getKey())
  			{
  				case MIN_CP:
  				case MIN_HP:
  				case MIN_MP:
  				case MASS:
  				case MIN_DISTANCE:
  				case MAX_DISTANCE:
  				case CHARGING:
  					 str = str + cond.getKey().name()+":" + cond.getValue().IntParameter();
  					break;
  				case ONLY_MONSTER:
  				case ONLY_PLAYER:
  				case TARGET_FIGHTER:
  				case TARGET_MAGE:
  					 str = str + cond.getKey().name()+":true";
  					break;
  				case TARGET_SKILL_EFFECT:
  				case SELF_SKILL_EFFECT:
  					str = str + cond.getKey().name()+":" + cond.getValue().getList().toString().replace("[", "(").replace("]", ")").replace(" ", "");
  					break;
  				default:
  					break;
  			}
  			str=str+";";
  	 }
    return str.isBlank() ? null : str;
   }
   
	@SuppressWarnings("rawtypes")
	public boolean canConvert(Class type) 
   {
      return type.equals(PCondition.class);
   }	
}