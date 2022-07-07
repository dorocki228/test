package  l2s.Phantoms.ai.individual.Dwarf;

import  l2s.Phantoms.ai.abstracts.PhantomDefaultAI;
import  l2s.Phantoms.ai.tasks.FighterTask;
import  l2s.gameserver.model.Creature;
import  l2s.gameserver.model.Player;


// добавить сумон осадного голема и ломание замковых стен
/*
 * Аден
 * 24180011
 * 24180006
 */
public class Maestro extends PhantomDefaultAI
{
	@Override
	public void doCast()
	{
		Player phantom = getActor();
		if (!phantom.isInCombat() && !phantom.isAttackingNow())
		{
			// Вызываем самона
		/*	if (phantom.getSummonList().getSummon() == null && !phantom.isCastingNow())
				if (castSummonSkill())
					return;*/
			/*if (phantom.getSummonList().getSummon() != null && !phantom.getSummonList().getSummon().isDead() && !phantom.getSummonList().getSummon().getAbnormalList().containEffectFromSkills(CHECK_BUFF_SUMMON) && phantom.getOlympiadGame() == null)
			{
				Summonbuffs(actor.getSummonList().getSummon());
			}*/
		}
		// изначальный шанс занюкать редким нюком
		Creature target = phantom.phantom_params.getLockedTarget();
		if (target != null && target.isPlayer()) 
		{
		/*	if (phantom.getSummonList().getSummon() != null && !phantom.getSummonList().getSummon().isDead())
				castSummonActions(target); */// каст актионов сума
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
		startAITask(new FighterTask(getActor()), delay);
	}
	
	@Override
	public boolean isMelee()
	{
		return true;
	}
	
}