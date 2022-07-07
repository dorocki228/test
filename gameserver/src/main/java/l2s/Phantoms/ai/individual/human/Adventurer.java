package  l2s.Phantoms.ai.individual.human;

import  l2s.Phantoms.ai.abstracts.PhantomDefaultAI;
import  l2s.Phantoms.ai.tasks.FighterTask;
import  l2s.commons.util.Rnd;
import  l2s.gameserver.model.Creature;
import  l2s.gameserver.model.Player;
import  l2s.gameserver.utils.Location;
import  l2s.gameserver.utils.PositionUtils;

public class Adventurer extends PhantomDefaultAI
{
	@Override
	public void doCast()
	{
		Player phantom = getActor();
		if (phantom.getAbnormalList().containsEffects(922) && phantom.getAbnormalList().getEffectBySkillId(922).getTimeLeft() > Rnd.get(3, 6))
			return;

		// изначальный шанс занюкать редким нюком
		Creature target = phantom.phantom_params.getLockedTarget();
		if (target != null && target.isPlayer()) // контроль. умен. если цель игрок (стан, рут, раш и т.д.)
			if (castControlSkill(target))
				return;
		// скиллы по ситуации
		if (castSituationSkill(target))
			return;
		if (castDebuffSkill(target))
			return;
		if (target != null && PositionUtils.isInFrontOf(target, phantom))
		{
			if (target.isStunned() || target.isSleeping() || target.getTarget() != phantom || Rnd.chance(Rnd.get(3,7))|| phantom.getAbnormalList().containsEffects(922))
			{
				Location loc = getLocBehind(phantom, target);
				if (loc!=null)
				{
					phantom.abortAttack(true, false);
					phantom.moveToLocation(loc, 0, true);
					return;
				}
			}
		}
		castNukeSkill(target);
	}
	
	@Override
	public void startAITask(long delay)
	{
		startAITask(new FighterTask(getActor()), delay);
	}
	
	@Override
	public boolean isMelee()
	{
		return true;
	}
	
}