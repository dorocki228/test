package l2s.Phantoms.ai.tasks;

import java.util.stream.Collectors;

import l2s.Phantoms.Utils.PhantomUtils;
import l2s.Phantoms.ai.abstracts.PhantomAITask;
import l2s.Phantoms.ai.abstracts.PhantomDefaultPartyAI;
import l2s.Phantoms.enums.PartyState;
import l2s.Phantoms.enums.PhantomType;
import l2s.commons.util.Rnd;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;

public class HealerTask extends PhantomAITask
{
	public HealerTask(Player ph)
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
			boolean is_need_to_get_new_target = phantom.phantom_params.getPhantomAI().isNeedToGetNewTarget();
			switch(phantom.getPhantomType())
			{
				case PHANTOM:
				case PHANTOM_HARD:
				{
					// если удалось взять таргет, то кастуем и перезапускаем таск
					if(!is_need_to_get_new_target)
					{
						fallBack(500);
						doAction();
					}
					else
					{
						getTarget();
					}
					break;
				}
				case PHANTOM_PARTY:
				case PHANTOM_CLAN_MEMBER:
				case PHANTOM_BOT_HUNTER:
				{
					// если удалось взять таргет, то кастуем и перезапускаем таск
					if(!is_need_to_get_new_target)
					{
						fallBack(Rnd.get(500, 700));
						doAction();
					}
					else
					{

						// если фантом в группе и она передвигается, то никаких действий не
						// предпринимаем
						PhantomDefaultPartyAI party_ai = phantom.phantom_params.getPhantomPartyAI();
						if(party_ai != null && party_ai.getMoving() == 1)
							return;
						getTarget();

						/*
						 * if (phantom != party_ai.getPartyAssister()) // следовать за плом { Player target = party_ai.getPartyAssister(); if (target != null && phantom.getDistance(target) >= 400)
						 * phantom.moveToLocation(target.getLoc(), Rnd.get(120, 350), true); }
						 */
					}
					break;
				}
				default:
					break;
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

		// проверим таргент
		Creature target = phantom.phantom_params.getLockedTarget();
		switch(phantom.getPhantomType())
		{
			case PHANTOM:
			case PHANTOM_HARD:
			case PHANTOM_PARTY:
				if(target != null && target.isMonster())
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
			}
			default:
				break;
		}
		if(phantom.getDistance(target) <= 100 && fallBack(99)) // меньше 100 - сваливаем 
			return true;

		//if (phantom.getAbnormalList().getEffectsBySkillId(1418) == null)
		phantom.phantom_params.getPhantomAI().doCast();

		return true;
	}

}