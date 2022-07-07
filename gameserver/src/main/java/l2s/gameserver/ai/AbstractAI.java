package l2s.gameserver.ai;

import l2s.commons.lang.reference.HardReference;
import l2s.commons.lang.reference.HardReferences;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.utils.Location;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractAI implements Runnable
{
	protected static final Logger _log = LogManager.getLogger(AbstractAI.class);

	protected final Creature _actor;
	private HardReference<? extends Creature> _attackTarget;
	private HardReference<? extends Creature> _castTarget;
	private CtrlIntention _intention;

	protected AbstractAI(Creature actor)
	{
		_attackTarget = HardReferences.emptyRef();
		_castTarget = HardReferences.emptyRef();
		_intention = CtrlIntention.AI_INTENTION_IDLE;
		_actor = actor;
	}

	public void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		_intention = intention;
		if(intention != CtrlIntention.AI_INTENTION_CAST && intention != CtrlIntention.AI_INTENTION_ATTACK)
			setAttackTarget(null);
	}

	public final void setIntention(CtrlIntention intention)
	{
        setIntention(intention, null, null);
	}

	public final void setIntention(CtrlIntention intention, Object arg0)
	{
        setIntention(intention, arg0, null);
	}

	public void setIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		if(intention != CtrlIntention.AI_INTENTION_CAST && intention != CtrlIntention.AI_INTENTION_ATTACK)
			setAttackTarget(null);
		Creature actor = getActor();

		if(!actor.isVisible())
		{
			if(_intention == CtrlIntention.AI_INTENTION_IDLE)
				return;
			intention = CtrlIntention.AI_INTENTION_IDLE;
		}
		actor.getListeners().onAiIntention(intention, arg0, arg1);
		switch(intention)
		{
			case AI_INTENTION_IDLE:
			{
				onIntentionIdle();
				break;
			}
			case AI_INTENTION_ACTIVE:
			{
				onIntentionActive();
				break;
			}
			case AI_INTENTION_REST:
			{
				onIntentionRest();
				break;
			}
			case AI_INTENTION_ATTACK:
			{
				if(!(arg0 instanceof Creature))
					return;

				onIntentionAttack((Creature) arg0);
				break;
			}
			case AI_INTENTION_CAST:
			{
				onIntentionCast((Skill) arg0, (Creature) arg1);
				break;
			}
			case AI_INTENTION_PICK_UP:
			{
				onIntentionPickUp((GameObject) arg0);
				break;
			}
			case AI_INTENTION_INTERACT:
			{
				onIntentionInteract((GameObject) arg0);
				break;
			}
			case AI_INTENTION_FOLLOW:
			{
				onIntentionFollow((Creature) arg0, (Integer) arg1);
				break;
			}
			case AI_INTENTION_COUPLE_ACTION:
			{
				onIntentionCoupleAction((Player) arg0, (Integer) arg1);
				break;
			}
			case AI_INTENTION_RETURN_HOME:
			{
				onIntentionReturnHome((boolean) arg0);
				break;
			}
		}
	}

	public final void notifyEvent(CtrlEvent evt)
	{
        notifyEvent(evt, new Object[0]);
	}

	public final void notifyEvent(CtrlEvent evt, Object arg0)
	{
        notifyEvent(evt, new Object[] { arg0 });
	}

	public final void notifyEvent(CtrlEvent evt, Object arg0, Object arg1)
	{
        notifyEvent(evt, new Object[] { arg0, arg1 });
	}

	public final void notifyEvent(CtrlEvent evt, Object arg0, Object arg1, Object arg2)
	{
        notifyEvent(evt, new Object[] { arg0, arg1, arg2 });
	}

	public void notifyEvent(CtrlEvent evt, Object[] args)
	{
		Creature actor = getActor();
		actor.getListeners().onAiEvent(evt, args);

		switch(evt)
		{
			case EVT_THINK:
			{
				onEvtThink();
				break;
			}
			case EVT_ATTACKED:
			{
				onEvtAttacked((Creature) args[0], (Skill) args[1], ((Number) args[2]).intValue());
				break;
			}
			case EVT_CLAN_ATTACKED:
			{
				onEvtClanAttacked((Creature) args[0], (Creature) args[1], ((Number) args[2]).intValue());
				break;
			}
			case EVT_AGGRESSION:
			{
				onEvtAggression((Creature) args[0], ((Number) args[1]).intValue());
				break;
			}
			case EVT_READY_TO_ACT:
			{
				onEvtReadyToAct();
				break;
			}
			case EVT_ARRIVED:
			{
				onEvtArrived();
				break;
			}
			case EVT_ARRIVED_TARGET:
			{
				onEvtArrivedTarget();
				break;
			}
			case EVT_ARRIVED_BLOCKED:
			{
				onEvtArrivedBlocked((Location) args[0]);
				break;
			}
			case EVT_FORGET_OBJECT:
			{
				onEvtForgetObject((GameObject) args[0]);
				break;
			}
			case EVT_DEAD:
			{
				onEvtDead((Creature) args[0]);
				break;
			}
			case EVT_FAKE_DEATH:
			{
				onEvtFakeDeath();
				break;
			}
			case EVT_FINISH_CASTING:
			{
				onEvtFinishCasting((Skill) args[0], (Creature) args[1], (boolean) args[2]);
				break;
			}
			case EVT_SEE_SPELL:
			{
				onEvtSeeSpell((Skill) args[0], (Creature) args[1], (Creature) args[2]);
				break;
			}
			case EVT_SPAWN:
			{
				onEvtSpawn();
				break;
			}
			case EVT_DESPAWN:
			{
				onEvtDeSpawn();
				break;
			}
			case EVT_TIMER:
			{
				onEvtTimer(((Number) args[0]).intValue(), args[1], args[2]);
				break;
			}
			case EVT_SCRIPT_EVENT:
			{
				onEvtScriptEvent(args[0].toString(), args[1], args[2]);
				break;
			}
			case EVT_MENU_SELECTED:
			{
				onEvtMenuSelected((Player) args[0], ((Number) args[1]).intValue(), ((Number) args[2]).intValue());
				break;
			}
			case EVT_KNOCK_DOWN:
			{
				onEvtKnockDown((Creature) args[0]);
				break;
			}
			case EVT_KNOCK_BACK:
			{
				onEvtKnockBack((Creature) args[0]);
				break;
			}
			case EVT_FLY_UP:
			{
				onEvtFlyUp((Creature) args[0]);
				break;
			}
			case EVT_TELEPORTED:
			{
				onEvtTeleported();
				break;
			}
		}
	}

	protected void clientActionFailed()
	{
		Creature actor = getActor();
		if(actor != null && actor.isPlayer())
			actor.sendActionFailed();
	}

	/**
	 * Останавливает движение
	 * 
	 * @param validate - рассылать ли ValidateLocation
	 */
	public void clientStopMoving(boolean validate)
	{
		Creature actor = getActor();
		actor.stopMove(validate);
	}
	
	/**
	 * Останавливает движение и рассылает ValidateLocation
	 */
	public void clientStopMoving()
	{
		Creature actor = getActor();
		actor.stopMove();
	}
	
	public Creature getActor()
	{
		return _actor;
	}

	public CtrlIntention getIntention()
	{
		return _intention;
	}

	public void setAttackTarget(Creature target)
	{
		_attackTarget = (HardReference<? extends Creature>) (target == null ? HardReferences.emptyRef() : target.getRef());
	}

	public Creature getAttackTarget()
	{
		return _attackTarget.get();
	}

	public void setCastTarget(Creature target)
	{
		_castTarget = (HardReference<? extends Creature>) (target == null ? HardReferences.emptyRef() : target.getRef());
	}

	public Creature getCastTarget()
	{
		return _castTarget.get();
	}

	public boolean isGlobalAI()
	{
		return false;
	}

	@Override
	public void run()
	{}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " for " + getActor();
	}

	protected abstract void onIntentionIdle();

	protected abstract void onIntentionActive();

	protected abstract void onIntentionRest();

	protected abstract void onIntentionAttack(Creature p0);

	protected abstract void onIntentionCast(Skill p0, Creature p1);

	protected abstract void onIntentionPickUp(GameObject p0);

	protected abstract void onIntentionInteract(GameObject p0);

	protected abstract void onIntentionCoupleAction(Player p0, Integer p1);

	protected abstract void onIntentionReturnHome(boolean p0);

	protected abstract void onEvtThink();

	protected abstract void onEvtAttacked(Creature p0, Skill p1, int p2);

	protected abstract void onEvtClanAttacked(Creature p0, Creature p1, int p2);

	protected abstract void onEvtAggression(Creature p0, int p1);

	protected abstract void onEvtReadyToAct();

	protected abstract void onEvtArrived();

	protected abstract void onEvtArrivedTarget();

	protected abstract void onEvtTeleported();

	protected abstract void onEvtArrivedBlocked(Location p0);

	protected abstract void onEvtForgetObject(GameObject p0);

	protected abstract void onEvtDead(Creature p0);

	protected abstract void onEvtFakeDeath();

	protected abstract void onEvtFinishCasting(Skill p0, Creature p1, boolean p2);

	protected abstract void onEvtSeeSpell(Skill p0, Creature p1, Creature p2);

	protected abstract void onEvtSpawn();

	public abstract void onEvtDeSpawn();

	protected abstract void onIntentionFollow(Creature p0, Integer p1);

	protected abstract void onEvtTimer(int p0, Object p1, Object p2);

	protected abstract void onEvtScriptEvent(String p0, Object p1, Object p2);

	protected abstract void onEvtMenuSelected(Player p0, int p1, int p2);

	protected abstract void onEvtKnockDown(Creature p0);

	protected abstract void onEvtKnockBack(Creature p0);

	protected abstract void onEvtFlyUp(Creature p0);

	protected boolean canAttackCharacter(Creature target)
	{
		return false;
	}
}
