package  l2s.Phantoms.objects.TrafficScheme;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import  l2s.commons.util.Rnd;

/*
 * Задержка в секундах между точками
 */
@XStreamAlias("move_delay")
public class MoveDelay
{
	@XStreamAlias("seconds")
	@XStreamAsAttribute
	private int seconds;
	
	@XStreamAlias("rnd")
	@XStreamAsAttribute
	private int rnd = 0;
	
	public MoveDelay()
	{}
	
	public MoveDelay(int sec, int rnd)
	{
		this.seconds= sec;
		this.rnd = rnd;
	}
	
	public int getSeconds()
	{
		if (seconds > 600)
			seconds = Rnd.get(2, 10)*60;
		return seconds;
	}
	
	public void setMaxSeconds()
	{
		if (seconds > 600)
			this.seconds = Rnd.get(2, 10)*60;
	}
	
	public void setSeconds(int sec)
	{
		this.seconds = sec;
		if (seconds < 0)
			seconds=0;
	}
	
	public int getRnd()
	{
		return rnd;
	}
	
	public void setRnd(int sec)
	{
		this.rnd = sec;
	}
	
  @Override
  public String toString() 
  {
      return " seconds:" +seconds + "rnd +-:" + rnd;
  }
}