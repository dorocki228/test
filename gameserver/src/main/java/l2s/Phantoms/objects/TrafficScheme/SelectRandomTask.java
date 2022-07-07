package  l2s.Phantoms.objects.TrafficScheme;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("select_random_task")
public class SelectRandomTask
{
	@XStreamAlias("priority_task_group")
	@XStreamAsAttribute
	private int priority_task_group;
	
	
	public SelectRandomTask()
	{}
	
	public SelectRandomTask(int item_id)
	{
		this.setId(item_id);
	}
	
	public int getId()
	{
		return priority_task_group;
	}

	public void setId(int id)
	{
		this.priority_task_group = id;
	}


}