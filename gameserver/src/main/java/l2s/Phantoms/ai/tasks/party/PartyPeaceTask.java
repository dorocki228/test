package  l2s.Phantoms.ai.tasks.party;


import  l2s.Phantoms.enums.PartyState;
import  l2s.Phantoms.manager.PartyManager;
import  l2s.commons.threading.RunnableImpl;

public class PartyPeaceTask extends RunnableImpl
{
	private int id;
	
	public PartyPeaceTask(int id)
	{
		this.id = id;
	}
	
	@Override
	public void runImpl()
	{
		PartyManager.getInstance().getPartyAIByID(id).changePartyState(PartyState.battle);
		//PartyManager.getInstance().getPartyAIByID(id).spawnPartyBattle();
	}
}
