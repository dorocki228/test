package  l2s.Phantoms.ai.tasks.party;


import  l2s.Phantoms.ai.abstracts.PhantomDefaultPartyAI;
import  l2s.Phantoms.enums.PartyState;
import  l2s.Phantoms.manager.PartyManager;
import  l2s.commons.threading.RunnableImpl;
import  l2s.gameserver.model.Player;
import  l2s.gameserver.data.xml.holder.SkillHolder;

public class CastPartyRecallTask extends RunnableImpl
{
	private int id;
	
	public CastPartyRecallTask(int id)
	{
		this.id = id;
	}
	
	@Override
	public void runImpl()
	{
		PhantomDefaultPartyAI party = PartyManager.getInstance().getPartyAIByID(id);
		party.changePartyState(PartyState.peace);
		for (Player healer : party.getHealers())
		{
			if (healer != null&& !healer.isDead() && !healer.isInPeaceZone() && healer.getSkillById(1255)!=null && !healer.isSkillDisabled(SkillHolder.getInstance().getSkillEntry(1255, 1)))
			{
				healer.getAI().Cast(SkillHolder.getInstance().getSkillEntry(1255, 1).getTemplate(), healer, false, false);
				break;
			}
		}
	}
}
