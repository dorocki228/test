package  l2s.Phantoms.objects.TrafficScheme;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("cast_skill")
public class PCastSkill
{
	@XStreamAlias("skill_id")
	@XStreamAsAttribute
	private int skill_id;
	
	@XStreamAlias("skill_lvl")
	@XStreamAsAttribute
	private int skill_lvl;
	
	@XStreamAlias("delay_ms")
	@XStreamAsAttribute
	private int delay;
	
	@XStreamAlias("target_id")
	@XStreamAsAttribute
	private int target_id;
	
	public PCastSkill()
	{}
	
	public int getSkillId()
	{
		return skill_id;
	}

	public PCastSkill(int skill_id , int skill_lvl)
	{
		this.skill_id = skill_id;
		this.skill_lvl = skill_lvl;
	}
	
  @Override
  public String toString() 
  {
      return " skill_id:"+ skill_id+ " delay:"+ delay +" target_id:" + target_id;
  }

	public int getSkillDelay()
	{
		return delay;
	}

	public void setSkillDelay(int delay)
	{
		this.delay = delay;
	}

	public int getTargetId()
	{
		return target_id;
	}

	public void setTargetId(int target_id)
	{
		this.target_id = target_id;
	}
}