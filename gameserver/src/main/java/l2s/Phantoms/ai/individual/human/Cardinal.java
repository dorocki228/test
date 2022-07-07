package  l2s.Phantoms.ai.individual.human;

import  l2s.Phantoms.ai.abstracts.PhantomDefaultAI;
import  l2s.Phantoms.ai.tasks.HealerTask;
import  l2s.Phantoms.enums.PhantomType;
import  l2s.gameserver.model.Creature;
import  l2s.gameserver.model.Player;
import  l2s.gameserver.stats.Stats;

public class Cardinal extends PhantomDefaultAI
{
	@Override
	public void doCast()
	{
		Player phantom = getActor();
		if (castSituationSkill(phantom))
			return;
		if (phantom.phantom_params.getPhantomPartyAI() != null || phantom.getParty() != null)
		{
			// бафаем какой-нибудь баф на группу или на себя
			if (castBuffSkillOnPartyMember())
				return;
			if (castPartyHealSkill()) // 30% каст пати хилки
				return;
		}
		if (phantom.getPhantomType() == PhantomType.PHANTOM_CLAN_MEMBER)
			if (castResurrectSkill())
				return;
		if (phantom.phantom_params.getResTarget() != null)
			return;
		// хил по выбранному таргенту
		Creature target = phantom.phantom_params.getLockedHealerTarget();
		if (target != null)
		{
			if (phantom.getCurrentHpPercents() < 60)
				target = phantom;
			if (target.calcStat(Stats.HEAL_EFFECTIVNESS, 100.) / 100. >= 1)
				// кастуем хил
				if (castHealSkill(target))
					return;
		}
		// если мы никого не подхилили, то по саб таргету дебафаем/жгем МР
		if (phantom.phantom_params.getPhantomPartyAI() != null)
		{
			target = phantom.phantom_params.getSubTarget();
			if (target == null || target.isDead() || phantom.getDistance(target.getLoc()) > 400 || (target.isPlayer() && !target.isInZoneBattle() && target.getPlayer().getPvpFlag() == 0) || target.isInvisible())
			{
				phantom.phantom_params.setSubTarget(null);
				target = null;
			}
		}
		else// если игра соло\олимп ищем цель самостоятельно
			target = phantom.phantom_params.getLockedTarget();
		if (target != null)
		{
			if (target.isPlayer() && !target.getAbnormalList().containEffectFromSkills(DEBUFF_RESIST))
			{
				/*if (target.getPlayer().getSummonList().getSummon() != null)
					if (castControlSkill(target.getPlayer().getSummonList().getSummon(), 1395))
						return;*/
				if (castControlSkill(target))
					return;
				if (castDebuffSkill(target))
					return;
			}
				castNukeSkill(target);
		}
	}
	
	@Override
	public void startAITask(long delay)
	{
		startAITask(new HealerTask(getActor()), delay);
	}
	
	@Override
	public boolean isHealer()
	{
		return true;
	}
	
}
