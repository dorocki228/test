package  l2s.Phantoms.objects.TrafficScheme;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import  l2s.commons.util.Rnd;

@XStreamAlias("trader")
public class Trader
{
	@XStreamAlias("count")
	@XStreamAsAttribute
	private int count;
	
	public Trader()
	{
		count = 1;
	}
	
	public int getCount()
	{
		return count;
	}

	public void incCount()
	{
		this.count = count +1;
	}

	
  @Override
  public String toString() 
  {
      return " count:" +count ;
  }

	public int getRndCount()
	{
		return getCount() + Rnd.get(getCount()*-1,getCount())+Rnd.get(5);
	}

}