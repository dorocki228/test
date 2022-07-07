package  l2s.Phantoms.ai.individual.Elf;

import  l2s.Phantoms.ai.abstracts.PhantomDefaultAI;
import  l2s.Phantoms.ai.tasks.FighterTask;
import  l2s.gameserver.model.Creature;
import  l2s.gameserver.model.Player;

public class EvaTemplar extends PhantomDefaultAI
{
	@Override
	public void doCast()
	{
		Player phantom = getActor();
		if (castPartyIcon(5563, 787))
			return;
		// изначальный шанс занюкать редким нюком
		Creature target = phantom.phantom_params.getLockedTarget();
		if (target != null && target.isPlayer()) 
			if (castControlSkill(target))
				return;
		// скиллы по ситуации
		if (castSituationSkill(target))
			return;
		// кастуем хил
		if (castHealSkill(phantom))
			return;
		if (castDebuffSkill(target))
			return;
		castNukeSkill(target);
	}
	
	@Override
	public void startAITask(long delay)
	{
		startAITask(new FighterTask(getActor()), delay);
	}
	
	@Override
	public boolean isTank()
	{
		return true;
	}
	
}