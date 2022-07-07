package  l2s.Phantoms.ai.merchants;


import  l2s.Phantoms.ai.abstracts.PhantomDefaultAI;

public class Merchants extends PhantomDefaultAI
{
	@Override
	public void doCast()
	{}
	
	@Override
	public void startAITask(long delay)
	{
		startAITask(new MerchantsTask(getActor()), delay);
	}
	
}