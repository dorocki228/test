package  l2s.Phantoms.ai.individual.human;

import  l2s.Phantoms.ai.abstracts.PhantomDefaultAI;
import  l2s.Phantoms.ai.tasks.SupportTask;
import  l2s.gameserver.model.Creature;
import  l2s.gameserver.model.Player;

public class Hierophant extends PhantomDefaultAI
{
	@Override
	public void doCast()
	{
		Player phantom = getActor();
		// изначальный шанс занюкать редким нюком
		Creature target = phantom.phantom_params.getLockedTarget();
		if (target != null && target.isPlayer()) // контроль. умен. если цель игрок (стан, рут, раш и т.д.)
			if (castControlSkill(target))
				return;
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
		startAITask(new SupportTask(getActor()), delay);
	}
	
	@Override
	public boolean isSupport()
	{
		return true;
	}
	
}
