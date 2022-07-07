package  l2s.Phantoms.objects.TrafficScheme;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("summon_pet")
public class Pet
{
	@XStreamAlias("pet_id")
	@XStreamAsAttribute
	private int pet_id;
	
	@XStreamAlias("mount")
	@XStreamAsAttribute
	private boolean mount;
	
	public Pet()
	{}
	
	public Pet(int item_id, boolean mount)
	{
		this.setPetId(item_id);
		this.setMount(mount);
	}

	public int getPetId()
	{
		return pet_id;
	}

	public void setPetId(int pet_id)
	{
		this.pet_id = pet_id;
	}
	
	
	public boolean getMount()
	{
		return mount;
	}

	public void setMount(boolean m)
	{
		this.mount = m;
	}
	
  @Override
  public String toString() 
  {
      return " pet id:" +pet_id + " mount:" + mount;
  }


}