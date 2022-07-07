package  l2s.Phantoms.ai.individual.human;

import  l2s.Phantoms.ai.abstracts.PhantomDefaultAI;
import  l2s.Phantoms.ai.tasks.MageTask;
import  l2s.gameserver.model.Creature;
import  l2s.gameserver.model.Player;
import  l2s.gameserver.stats.Stats;

/// XXX добавить ключение активки
public class Soultaker extends PhantomDefaultAI
{
	@Override
	public void doCast()
	{
		Player phantom = getActor();
		// Вызываем самона
		if (actor.getAnyServitor() == null && !actor.isCastingNow())
			if (castSummonSkill())
				return;
		
		// изначальный шанс занюкать редким нюком
		Creature target = phantom.phantom_params.getLockedTarget();
		if (target != null && target.isPlayer()) 
			if (castControlSkill(target))
				return;
		// кастуем хил
		if (phantom.calcStat(Stats.HEAL_EFFECTIVNESS, 100.) / 100. >= 1)
			if (castHealSkill(phantom))
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
		startAITask(new MageTask(getActor()), delay);
	}
	
	@Override
	public boolean isNuker()
	{
		return true;
	}
	
}
