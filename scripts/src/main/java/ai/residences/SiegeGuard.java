package ai.residences;

import l2s.gameserver.Config;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.World;
import l2s.gameserver.model.instances.NpcInstance;

import java.util.List;


public abstract class SiegeGuard extends DefaultAI<NpcInstance>
{
	public SiegeGuard(NpcInstance actor)
	{
		super(actor);
		setMaxPursueRange(1000);
	}

	@Override
	public int getMaxPathfindFails()
	{
		return Integer.MAX_VALUE;
	}

	/*
	TODO
	@Override
	public int getMaxAttackTimeout()
	{
		return 0;
	}*/

	@Override
	protected boolean randomWalk()
	{
		return false;
	}

	@Override
	protected boolean randomAnimation()
	{
		return false;
	}

	@Override
	public boolean canSeeInSilentMove(Playable target)
	{
		// Осадные гварды могут видеть игроков в режиме Silent Move с вероятностью 10%
		return !target.isSilentMoving(10.0);
	}

	@Override
	protected boolean isAggressive()
	{
		return true;
	}

	@Override
	protected boolean isGlobalAggro()
	{
		return true;
	}

	@Override
	protected void onEvtAggression(Creature target, int aggro)
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
			return;
		if(target == null || !actor.isAutoAttackable(target))
			return;
		super.onEvtAggression(target, aggro);
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();

		if(actor.isActionsDisabled())
			return true;

		if(_def_think)
		{
			if(doTask())
				clearTasks();
			return true;
		}

		long now = System.currentTimeMillis();
		if(now - _checkAggroTimestamp > Config.AGGRO_CHECK_INTERVAL)
		{
			_checkAggroTimestamp = now;

			List<Creature> targets = World.getAroundCharacters(actor);
			while(!targets.isEmpty())
			{
				Creature target = getNearestTarget(targets);
				if(target == null)
					break;

				if(checkAggression(target))
				{
					/*actor.getAggroList().addDamageHate(target, 0, 2);

					if(target.isServitor())
						actor.getAggroList().addDamageHate(target.getPlayer(), 0, 1);

					startRunningTask(_attackAITaskDelay);
					setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);*/

					notifyEvent(CtrlEvent.EVT_AGGRESSION, target, 2);

					return true;
				}

				targets.remove(target);
			}
		}

		Location sloc = actor.getSpawnedLoc();
		if(!actor.isInRange(sloc, 250))	// Проверка на расстояние до точки спауна
		{
			teleportHome();
			return true;
		}
		return false;
	}

	@Override
	protected Creature prepareTarget()
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
			return null;

		// Новая цель исходя из агрессивности
		List<Creature> hateList = actor.getAggroList().getHateList(getMaxPursueRange());
		Creature hated = null;
		for(Creature cha : hateList)
		{
			//Не подходит, очищаем хейт
			if(!checkTarget(cha, getMaxPursueRange()))
			{
				actor.getAggroList().remove(cha, true);
				continue;
			}
			hated = cha;
			break;
		}

		if(hated != null)
		{
			setAttackTarget(hated);
			return hated;
		}

		return null;
	}

	@Override
	public boolean canAttackCharacter(Creature target)
	{
		return getActor().isAutoAttackable(target);
	}

	@Override
	protected boolean maybeMoveToHome(boolean force)
	{
		return returnHome(true);
	}
}