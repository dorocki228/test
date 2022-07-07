package  l2s.Phantoms.ai.tasks.party;


import  l2s.Phantoms.manager.PartyManager;
import  l2s.commons.threading.RunnableImpl;

public class PartyTask extends RunnableImpl
{
	private int id;
	
	public PartyTask(int id)
	{
		this.id = id;
	}
	
	@Override
	public void runImpl()
	{
		PartyManager.getInstance().getPartyAIByID(id).doAction();
	}
}
