package  l2s.Phantoms.ai.individual.Dwarf;

import  l2s.Phantoms.ai.abstracts.PhantomDefaultAI;
import  l2s.Phantoms.ai.tasks.SpoilTask;
import  l2s.gameserver.model.Creature;
import  l2s.gameserver.model.Player;

public class FortuneSeeker extends PhantomDefaultAI
{
	@Override
	public void doCast()
	{
		Player phantom = getActor();
		// изначальный шанс занюкать редким нюком
		Creature target = phantom.phantom_params.getLockedTarget();
		if (target != null && target.isPlayer()) 
			if (castControlSkill(target))
				return;
		if (castSpoilSkill(target)) //getSpoilSkills() getSweperSkills()
			return;

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
		startAITask(new SpoilTask(getActor()), delay);
	}
	
	@Override
	public boolean isMelee()
	{
		return true;
	}
	
}