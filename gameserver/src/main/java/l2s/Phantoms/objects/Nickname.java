package  l2s.Phantoms.objects;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("phantom")
public class Nickname
{
	@XStreamAlias("name")
	@XStreamAsAttribute
	private String name;
	
	@XStreamAlias("classId")
	@XStreamAsAttribute
	private int[] classId = new int[0];
	
	@XStreamAlias("sex")
	@XStreamAsAttribute
	private int sex;
	
	public Nickname()
	{}
	
	public Nickname(String name,int[] classId, int sex, Boolean clan_leader)
	{
		this.name = name;
		this.classId = classId;
		this.sex = sex;
	}

	public boolean containsClassId(int class_id)
	{
		if (getClassId() == null || getClassId().length == 0)
			return true;
		for (int classid : getClassId())
			if (classid == class_id)
				return true;
		
		return false;
	}

	public String getName()
	{
		return name;
	}

	public int[] getClassId()
	{
		return classId;
	}
	
	public int getSex()
	{
		return sex;
	}
}
