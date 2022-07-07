package  l2s.Phantoms.objects.TrafficScheme;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("mount_agation")
public class Agation
{
	@XStreamAlias("agation_id")
	@XStreamAsAttribute
	private int agation_id;
	
	@XStreamAlias("agation_skill")
	@XStreamAsAttribute
	private int agation_skill;
	
	public Agation()
	{}
	
	public Agation(int agation,int agation_skill)
	{
		this.setAgationId(agation);
		this.setAgationSkill(agation_skill);
	}

	public int getAgationId()
	{
		return agation_id;
	}

	public void setAgationId(int agation)
	{
		this.agation_id = agation;
	}

  @Override
  public String toString() 
  {
      return " agation id:" +agation_id + " agation_skill:" + agation_skill;
  }

	public int getAgationSkill()
	{
		return agation_skill;
	}

	public void setAgationSkill(int agation_skill)
	{
		this.agation_skill = agation_skill;
	}
}