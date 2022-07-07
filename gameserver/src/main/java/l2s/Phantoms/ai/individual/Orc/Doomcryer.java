package  l2s.Phantoms.ai.individual.Orc;

import  l2s.Phantoms.ai.abstracts.PhantomDefaultAI;
import  l2s.Phantoms.ai.tasks.SupportTask;
import  l2s.gameserver.model.Creature;
import  l2s.gameserver.model.Player;

public class Doomcryer extends PhantomDefaultAI
{
	@Override
	public void doCast()
	{
		Player phantom = getActor();
		Creature target = phantom.phantom_params.getLockedTarget();
		if (target != null && target.isPlayer()) 
			if (castControlSkill(target))
				return;
		// скиллы по ситуации
		if (castSituationSkill(target))
			return;

		if (castDebuffSkill(target))
			return;
		
		castNukeSkill(target);
		/*
		 * if (phantom.phantom_params.getPhantomPartyAI() != null) { // бафаем какой-нибудь баф на группу if (castBuffSkillOnPartyMember()) return; } PhantomPartyObject party =
		 * phantom.phantom_params.getPhantomPartyAI(); if (party != null) { Player burning_chop_target = party.getAnyTank() != null ? party.getAnyTank() : party.getAnySupport() != null ? party.getAnySupport()
		 * : party.getAnyHealer() != null ? party.getAnyHealer() : party.getAnyNuker(); if (castRareNukeSkill(burning_chop_target)) return; }
		 * 
		 * 
		 * // выбираем кого хилить - в приоритете себя skill_chance = 30; if (phantom.getCurrentHpPercents() < 70 && castHealSkill(phantom, true)) return;
		 * 
		 * // кастуем чайник if (Rnd.chance(skill_chance) && castHealSkill(target, true)) return;
		 * 
		 * // если мы никого не подхилили, то по саб таргету дебафаем/жгем МР target = phantom.phantom_params.getSubTarget(); skill_chance += 20; if (Rnd.chance(skill_chance) && castRareDebuffSkill(target))
		 * return;
		 * 
		 * skill_chance += 20; if (castDebuffSkill(target)) return;
		 * 
		 * castNukeSkill(target);
		 */
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
	
	@Override
	public boolean isDisabler()
	{
		return true;
	}
	
}