package  l2s.Phantoms.objects.TrafficScheme;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("target")
public class TargetNpc
{
	@XStreamAlias("npc_id")
	@XStreamAsAttribute
	private int npc_id;
	
	public TargetNpc()
	{}
	
	public TargetNpc(int npc_id)
	{
		this.setNpcId(npc_id);
	}

	public int getNpcId()
	{
		return npc_id;
	}

	public void setNpcId(int npc_id)
	{
		this.npc_id = npc_id;
	}
	
  @Override
  public String toString() 
  {
      return " npc id:" +npc_id;
  }
}