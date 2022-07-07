package  l2s.Phantoms.ai.individual.human;

import  l2s.Phantoms.ai.abstracts.PhantomDefaultAI;
import  l2s.Phantoms.ai.tasks.FighterTask;
import  l2s.gameserver.model.Creature;
import  l2s.gameserver.model.Player;
import  l2s.gameserver.stats.Stats;

public class PhoenixKnight extends PhantomDefaultAI
{
	@Override
	public void doCast()
	{
		Player phantom = getActor();
		if (phantom.getLevel() >= 83 && !phantom.isInCombat() && !phantom.isAttackingNow()) // сумон феникса на 83 лвле
		{
			// Вызываем самона
			/*if (actor.getSummonList().getSummon() == null && !actor.isCastingNow())
				if (castSummonSkill())
					return;*/
			/*if (actor.getSummonList().getSummon() != null && !actor.getSummonList().getSummon().isDead() && !actor.getSummonList().getSummon().getAbnormalList().containEffectFromSkills(CHECK_BUFF_SUMMON) && actor.getOlympiadGame() == null)
			{
				Summonbuffs(actor.getSummonList().getSummon());
			}*/
		}
		if (castPartyIcon(5562, 785))// проверка и баф пати иконы
			return;

		Creature target = phantom.phantom_params.getLockedTarget();
		if (target != null && target.isPlayer()) 
		{
			/*if (phantom.getSummonList().getSummon() != null && !phantom.getSummonList().getSummon().isDead())
				castSummonActions(target); // каст актионов сума*/
			
			if (castControlSkill(target))
				return;
		}
		// скиллы по ситуации
		if (castSituationSkill(target))
			return;
		// кастуем хил
		if (phantom.calcStat(Stats.HEAL_EFFECTIVNESS, 100.) / 100. >= 1)
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