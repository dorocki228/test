package l2s.Phantoms.ai.tasks;

import java.util.stream.Collectors;

import l2s.Phantoms.Utils.PhantomUtils;
import l2s.Phantoms.ai.abstracts.PhantomAITask;
import l2s.Phantoms.enums.PartyState;
import l2s.Phantoms.enums.PhantomType;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;

public class SummonTask extends PhantomAITask
{
	boolean _ctrlPressed = false;

	public SummonTask(Player ph)
	{
		super(ph);
	}

	@Override
	public void runImpl()
	{
		try
		{
			// если фейк зафирен, то перезапускаем таск
			if(phantom.isOutOfControl() || phantom.isStunned() || phantom.isParalyzed() || phantom.isSleeping())
				return;

			if(phantom.isDead())
			{
				if(phantom.getPhantomType() != PhantomType.PHANTOM_BOT_HUNTER && phantom.getPhantomType() != PhantomType.PHANTOM_PARTY && phantom.getPhantomType() != PhantomType.PHANTOM_CLAN_MEMBER)
					if(phantom.phantom_params.getDeadPhantomActionTask() == null || phantom.phantom_params.getDeadPhantomActionTask().isDone() || phantom.phantom_params.getDeadPhantomActionTask().isCancelled())
					{
						phantom.phantom_params.setLockedTarget(null); // убираем свои цели
						phantom.setTarget(null);
						phantom.phantom_params.startDeadPhantomActionTask(Rnd.get(8, 15) * 1000);
					}
				return;
			}
			// вне зависимости от таргета и ситуации мы ребафаемся/регенимся
			phantom.phantom_params.getPhantomAI().doBuffCast();

			
			if (phantom.phantom_params.getState() == PartyState.route && phantom.phantom_params.getPhantomAI().getRouteTask()!=null)
			{
				phantom.phantom_params.getPhantomAI().doRouteAction();
				return;
			}
			
			// если удалось взять таргет, то кастуем и перезапускаем таск
			boolean is_need_to_get_new_target = phantom.phantom_params.getPhantomAI().isNeedToGetNewTarget();
			if(!is_need_to_get_new_target/*
																																	 * && !returnToAllowedLocation(getComebackDistance() + 800) || phantom.isInPeaceZone()
																																	 */)
			{
				fallBack(250);
				doAction();
			}
			else
			{
				// если нужно брать таргет, то ищем его сначало рядом, потом все дальше
				for(int radius = 100; radius < phantom.phantom_params.getComebackDistance(); radius += 200)
				{
					if(getAndSetLockedTarget(radius))
					{
						phantom.setTarget(phantom.phantom_params.getLockedTarget());
						break;
					}
				}
				int min_distance = 200;
				int max_distance = 250;
				Creature target = phantom.phantom_params.getLockedTarget();
				if(Rnd.get(1000000) <= Config.FAKE_WALK_CHANCE)
				{
					if((target != null && target.isInRange(phantom.getLoc(), 600)) || target == null)
						randomMove(min_distance, max_distance);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public boolean doAction()
	{
		// Если выполнено действие в супер классе, значит фантом в мирной зоне и на этом
		// всё
		if(super.doAction())
			return true;
		Creature target = phantom.phantom_params.getLockedTarget();
		if(target == null)
			return false;
		switch(phantom.getPhantomType())
		{
			case PHANTOM:
			case PHANTOM_HARD:
			case PHANTOM_PARTY:
				if(target.isMonster())
					PhantomUtils.checkLevelAndSetFarmLoc(phantom.getPlayer(), target, false);
				break;
			case PHANTOM_CLAN_MEMBER:
			{
				if(phantom.phantom_params.getPhantomPartyAI() != null)
					if(phantom.getAroundPlayers(150, 200).stream().filter(p -> p != null && !p.isDead() && p.getAI().getAttackTarget() == phantom && p.getClan() != phantom.getClan()).collect(Collectors.toList()).size() >= 3)
					{
						randomMove(250, 400);
						return false;
					}
				break;
			}
			default:
				break;
		}
		//	if (phantom.getAbnormalList().containsEffects(EffectType.Aggression) && phantom.getTarget() != null && phantom.getTarget().isCreature())
		//	target = (Creature) phantom.getTarget();

		if(Math.abs(phantom.getZ() - phantom.getImpliedTargetLoc(target).getZ()) > 200)
		{
			phantom.abortAttack(true, false);
			/*final SummonInstance sum = phantom.getSummonList().getSummon();
			if (sum != null && !sum.isDead())
				sum.abortAttack(true, false);*/
			return true;
		}

		/*final Summon sum = phantom.getSummonList().getSummon();
		if (sum != null && !sum.isDead())
		{
			if (sum.getAbnormalList().containEffectFromSkillId(1496))
			{
				sum.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				return false;
			}
			else
				if(sum.getAI().getAttackTarget()== null || sum.getAI().getAttackTarget() != target)
						sum.getAI().Attack(target, false, false);
		}*/
		if(phantom.getDistance(target) >= 600)
			moveToCharacter(phantom, target, (int) Math.round(phantom.getDistance(target) / 4));
		if(phantom.getDistance(target) <= 250 && !phantom.isMoving())
			randomMove(250, 300);
		phantom.phantom_params.getPhantomAI().doCast();
		return true;
	}

}