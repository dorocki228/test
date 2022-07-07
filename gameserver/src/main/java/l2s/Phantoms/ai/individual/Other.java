package  l2s.Phantoms.ai.individual;


import  l2s.Phantoms.ai.abstracts.PhantomDefaultAI;
import  l2s.gameserver.model.Player;

/*
 * затычка для неизвесных класов
 */
public class Other extends PhantomDefaultAI
{
	@Override
	public void doCast()
	{}
	
	@Override
	public void startAITask(long delay)
	{
		Player phantom = getActor();
		_log.warn("Other :"+phantom);
	}
	
	@Override
	public boolean isNuker()
	{
		return true;
	}
}