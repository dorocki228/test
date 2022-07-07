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
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.s2c.SayPacket2;
import l2s.gameserver.tables.GmListTable;

public class FighterTask extends PhantomAITask
{
	public FighterTask(Player ph)
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
			switch(phantom.getPhantomType())
			{
				case PHANTOM:
				case PHANTOM_HARD:
				case PHANTOM_PARTY:
				case PHANTOM_CLAN_MEMBER:
				case PHANTOM_BOT_HUNTER:
				{
					if(!is_need_to_get_new_target/*
																																			 * && !returnToAllowedLocation(getComebackDistance() + 800) || phantom.isInPeaceZone()
																																			 */)
					{
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
					if(phantom.getAroundPlayers(200, 200).stream().filter(p -> p != null && !p.isDead() && p.getAI().getAttackTarget() == phantom && p.getClan() != phantom.getClan()).collect(Collectors.toList()).size() >= 3)
					{
						randomMove(250, 400);
						return true;
					}
				break;
			}
			default:
				break;
		}
		if(phantom.getAbnormalList().containsEffects(922) && phantom.getAbnormalList().getEffectBySkillId(922).getTimeLeft() > Rnd.get(3, 6))
		{
			fallBack(400);
			return false;
		}
		//TODO
		/*if (phantom.getAbnormalList().getEffectByStackType("disarm") != null)
			{
				fallBack(400);
				return false;
			}*/
		if(phantom.phantom_params.getGmLog())
			GmListTable.broadcastToGMs(new SayPacket2(0, ChatType.HERO_VOICE, "sys:", phantom.getName() + "doAction() " + target));
		//		if (phantom.getAbnormalList().containsEffects(EffectType.Aggression) && phantom.getTarget() != null && phantom.getTarget().isCreature())
		//	target = (Creature) phantom.getTarget();
		if(phantom.getAbnormalList().containsEffects(442))
			return false;
		if(phantom.getDistance(target) <= 200)
		{
			if(Math.abs(phantom.getZ() - phantom.getImpliedTargetLoc(target).getZ()) > 200)
			{
				phantom.abortAttack(true, false);
				phantom.stopMove();
				/*	final SummonInstance sum = phantom.getSummonList().getSummon();
					if (sum != null && !sum.isDead())
					{
						sum.abortAttack(true, false);
						sum.stopMove();
					}*/
				return true;
			}
			else
			{
				phantom.getAI().Attack(target, true, false);
				/*final SummonInstance sum = phantom.getSummonList().getSummon();
				if (sum != null && !sum.isDead())
				{
					if (!target.isAttackable(sum))
						sum.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target);
					else
						if(sum.getAI().getAttackTarget()== null || sum.getAI().getAttackTarget() != target)
							sum.getAI().Attack(target, true, false);
				}*/
			}
		}
		else
			moveToCharacterRnd(phantom, target, (int) phantom.getDistance(target) / 3);
		phantom.phantom_params.getPhantomAI().doCast();
		return true;
	}

}