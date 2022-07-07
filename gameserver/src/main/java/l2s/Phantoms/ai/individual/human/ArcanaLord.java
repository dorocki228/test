package  l2s.Phantoms.ai.individual.human;

import  l2s.Phantoms.ai.abstracts.PhantomDefaultAI;
import  l2s.Phantoms.ai.tasks.SummonTask;
import  l2s.gameserver.model.Creature;
import  l2s.gameserver.model.Player;

public class ArcanaLord extends PhantomDefaultAI
{
	@Override
	public void doCast()
	{
		Player phantom = getActor();
		// Вызываем самона
		if (actor.getAnyServitor() == null && !actor.isCastingNow())
			if (castSummonSkill())
				return;
		/*if (actor.getFirstServitor() != null && !actor.getFirstServitor().isDead())
		{
			if (!actor.getFirstServitor().getAbnormalList().containEffectFromSkills(CHECK_BUFF_SUMMON))
				Summonbuffs(actor.getFirstServitor());
		}*/
		Creature target = phantom.phantom_params.getLockedTarget();
		if (target != null && target.isPlayer()) // контроль. умен. если цель игрок (стан, рут, раш и т.д.)
		{
			if (actor.getFirstServitor() != null && !actor.getFirstServitor().isDead())
				castSummonActions(target); // каст актионов сума
			if (castControlSkill(target))
				return;
		}
		// кастуем хил
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
		startAITask(new SummonTask(getActor()), delay);
	}
	
	@Override
	public boolean isNuker()
	{
		return true;
	}
	
}
