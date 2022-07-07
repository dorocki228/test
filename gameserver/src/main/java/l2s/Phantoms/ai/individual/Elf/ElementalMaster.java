package  l2s.Phantoms.ai.individual.Elf;

import  l2s.Phantoms.ai.abstracts.PhantomDefaultAI;
import  l2s.Phantoms.ai.tasks.SummonTask;
import  l2s.gameserver.model.Creature;
import  l2s.gameserver.model.Player;
import  l2s.gameserver.data.xml.holder.SkillHolder;

public class ElementalMaster extends PhantomDefaultAI
{
	@Override
	public void doCast()
	{
		Player phantom = getActor();
		// Вызываем самона
		if (actor.getAnyServitor() == null && !actor.isCastingNow())
			if (castSummonSkill())
				return;
	
		if (phantom.getAbnormalList() == null || phantom.getAbnormalList().containsEffects(1547))
			phantom.getAI().Cast(SkillHolder.getInstance().getSkillEntry(1547, 3).getTemplate(), phantom);
		// изначальный шанс занюкать редким нюком
		Creature target = phantom.phantom_params.getLockedTarget();
		if (target != null && target.isPlayer()) 
		{
			if (actor.getFirstServitor() != null && !actor.getFirstServitor().isDead())
				castSummonActions(target); // каст актионов сума
			if (castControlSkill(target))
				return;
		}
		// скиллы по ситуации
		if (castSituationSkill(target))
			return;
		if (castDebuffSkill(target))
			return;
		castNukeSkill(target);
	}
	
	@Override
	public void startAITask(long delay)
	{
		startAITask(new SummonTask(getActor()), delay);
	}
	
	@Override
	public boolean isNuker()
	{
		return true;
	}
	
}
