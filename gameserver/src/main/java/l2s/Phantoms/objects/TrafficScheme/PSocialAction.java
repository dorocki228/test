package  l2s.Phantoms.objects.TrafficScheme;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("social_action")
public class PSocialAction
{
	@XStreamAlias("action_id")
	@XStreamAsAttribute
	private int id;
	
	public PSocialAction()
	{}
	
	public int getActionId()
	{
		return id;
	}

	public PSocialAction(int id)
	{
		this.id = id;
	}
	
  @Override
  public String toString() 
  {
      return " Action id:"+ getActionId();
  }
}