package l2s.gameserver.ai;

import l2s.commons.lang.reference.HardReference;
import l2s.commons.lang.reference.HardReferences;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.*;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.PositionUtils;

import java.util.concurrent.ScheduledFuture;

public class ServitorAI extends PlayableAI
{
	private HardReference<Creature> _runAwayTargetRef = HardReferences.emptyRef();

	private static final int MAX_DIST_FOR_ATTACK = 1500;
	private static final int CHECK_ATTACK_DIST_TIMER = 1000;

	private CtrlIntention _storedIntention = null;
	private Object _storedIntentionArg0 = null;
	private Object _storedIntentionArg1 = null;
	private boolean _storedForceUse = false;

	public ServitorAI(Servitor actor)
	{
		super(actor);
		addTimer(CHECK_ATTACK_DIST_TIMER, 1000);
	}

	public void storeIntention()
	{
		if (_storedIntention == null)
		{
			_storedIntention = getIntention();
			_storedIntentionArg0 = _intention_arg0;
			_storedIntentionArg1 = _intention_arg1;
			_storedForceUse = _forceUse;
		}
	}

	public boolean restoreIntention()
	{
		final CtrlIntention intention = _storedIntention;
		final Object arg0 = _storedIntentionArg0;
		final Object arg1 = _storedIntentionArg1;
		if (intention != null)
		{
			_forceUse = _storedForceUse;
			setIntention(intention, arg0, arg1);
			clearStoredIntention();

			onEvtThink();
			return true;
		}
		return false;
	}

	public void clearStoredIntention()
	{
		_storedIntention = null;
		_storedIntentionArg0 = null;
		_storedIntentionArg1 = null;
	}

	@Override
	protected void onIntentionIdle()
	{
		clearStoredIntention();
		super.onIntentionIdle();
	}

	@Override
	protected void onEvtFinishCasting(Skill skill, Creature target, boolean success) {
		if (!restoreIntention()) {
			super.onEvtFinishCasting(skill, target, success);
		}
	}

	@Override
	protected void thinkActive()
	{
		Servitor actor = getActor();
		clearNextAction();
		if(actor.isDepressed())
		{
			setAttackTarget(actor.getPlayer());
			changeIntention(CtrlIntention.AI_INTENTION_ATTACK, actor.getPlayer(), null);
			thinkAttack(true);
		}
		else if(actor.isFollowMode())
		{
			changeIntention(CtrlIntention.AI_INTENTION_FOLLOW, actor.getPlayer(), Config.FOLLOW_RANGE);
			thinkFollow();
		}
		super.thinkActive();
	}

	@Override
	protected void thinkAttack(boolean checkRange)
	{
		Servitor actor = getActor();
		if(actor.isDepressed())
			setAttackTarget(actor.getPlayer());
		super.thinkAttack(checkRange);
	}

	/*@Override
	protected boolean thinkCast(boolean checkRange)
	{
		if(super.thinkCast(checkRange))
		{
			setNextAction(AINextAction.ATTACK, getAttackTarget(), null, _forceUse, false);
			return true;
		}
		return false;
	}*/

	@Override
	protected void onEvtArrived()
	{
		if(!setNextIntention())
		{
			if(getIntention() == CtrlIntention.AI_INTENTION_INTERACT || getIntention() == CtrlIntention.AI_INTENTION_PICK_UP || getIntention() == CtrlIntention.AI_INTENTION_FOLLOW)
				onEvtThink();
			else
				changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
		}
	}

	@Override
	protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
	{
		Servitor actor = getActor();
		if(attacker != null && actor.getPlayer().isDead() && !actor.isDepressed())
			Attack(attacker, false, false);
		if(attacker != null && attacker != getActor().getPlayer())
			_runAwayTargetRef = (HardReference<Creature>) attacker.getRef();

		super.onEvtAttacked(attacker, skill, damage);
	}

	@Override
	public boolean Cast(Skill skill, Creature target, boolean forceUse, boolean dontMove)
	{
		Servitor actor = getActor();
		Player owner = actor.getPlayer();
		if(target != null && owner.getDistance(target) >= MAX_DIST_FOR_ATTACK)
		{
			if(!actor.isDead() && actor.isFollowMode())
			{
				clientActionFailed();
				owner.sendPacket(SystemMsg.THE_DISTANCE_IS_TOO_FAR_AND_SO_THE_CASTING_HAS_BEEN_STOPPED);
				setIntention(CtrlIntention.AI_INTENTION_FOLLOW, owner, Config.FOLLOW_RANGE);
				return false;
			}
		}

		storeIntention();

		return super.Cast(skill, target, forceUse, dontMove);
	}

	@Override
	public Servitor getActor() {
		return (Servitor) super.getActor();
	}

	public void notifyAttackModeChange(Servitor.AttackMode mode) {
		getActor().setAttackMode(mode);
	}

	@Override
	protected ScheduledFuture<?> scheduleThinkFollowTask() {
		return ThreadPoolManager.getInstance().schedule(new ThinkFollowForServitor(getActor()), 250);
	}

	private void tryRunAway() {
		Servitor actor = getActor();

		if (!actor.isMoving() && !actor.isDead() && !actor.isDepressed()) {
			Player owner = actor.getPlayer();
			Creature runAwayTarget = _runAwayTargetRef.get();

			if (owner != null && runAwayTarget != null && !owner.isDead() && !owner.isOutOfControl()) {
				if (runAwayTarget.isInCombat() && actor.getDistance(runAwayTarget) < (double) actor.getActingRange()) {
					int radius = actor.getActingRange();

					int heading = PositionUtils.calculateHeadingFrom(owner, actor);
					double radian = PositionUtils.convertHeadingToRadian(heading - Rnd.get(6000, 10000));

					Location ownerLoc = owner.getLoc();

					int x = (int) (ownerLoc.getX() + radius * Math.cos(radian));
					int y = (int) (ownerLoc.getY() + radius * Math.sin(radian));

					Location ne = new Location(x, y, actor.getZ()).correctGeoZ();

					actor.moveToLocation(ne, 0, true);
					return;
				}
				_runAwayTargetRef = HardReferences.emptyRef();
			} else {
				_runAwayTargetRef = HardReferences.emptyRef();
			}
		}
	}

	protected static class ThinkFollowForServitor implements Runnable {
		private final HardReference<? extends Playable> _actorRef;

		public ThinkFollowForServitor(Servitor actor) {
			_actorRef = actor.getRef();
		}

		@Override
		public void run() {
			final Servitor actor = (Servitor) _actorRef.get();
			if (actor == null)
			{
				return;
			}
			final ServitorAI actorAI = actor.getAI();
			if (actorAI.getIntention() != CtrlIntention.AI_INTENTION_FOLLOW)
			{
				if (actorAI.getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)
				{
					actor.setFollowMode(false);
				}
				return;
			}
			final Creature target = (Creature) actorAI._intention_arg0;
			if (target == null || target.isAlikeDead())
			{
				actorAI.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				return;
			}
			final int clientClipRange = (actor.getPlayer() != null && actor.getPlayer().getNetConnection() != null) ? actor.getPlayer().getNetConnection().getPawnClippingRange() : actor.getPlayer().isPhantom()? 20000:GameClient.DEFAULT_PAWN_CLIPPING_RANGE;
			final int collisions = (int) (actor.getColRadius()+target.getColRadius());
			final boolean incZ = PlayableAI.isThinkImplyZ(actor, target);
			final int dist = (int) (incZ ? actor.getDistance3D(target) : actor.getDistance(target))-collisions;
			final int followIndent = Math.min(clientClipRange, target.getActingRange());
			final int followRange = actor.getActingRange();
			if (dist > clientClipRange || dist > 2 << World.SHIFT_BY)
			{
				actorAI.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				actorAI.clientStopMoving();
				return;
			}
			final Player player = actor.getPlayer();
			if (player == null || player.isLogoutStarted() || ((actor.isPet() || actor.isSummon()) && player.getFirstServitor() != actor))
			{
				actorAI.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				actorAI.clientStopMoving();
				return;
			}
			if (dist > followRange)
			{
				if (!actor.isFollowing() || actor.getFollowTarget() != target)
				{
					actor.moveToRelative(target, CharacterAI.getIndentRange(followIndent), followRange);
				}
			}
			else
			{
				actorAI.tryRunAway();
			}
			actorAI._followTask = actorAI.scheduleThinkFollowTask();
		}
	}

	@Override
	protected void onEvtTimer(int timerId, Object arg1, Object arg2)
	{
		if(timerId == CHECK_ATTACK_DIST_TIMER)
		{
			if(getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
			{
				Servitor actor = getActor();
				Creature target = getAttackTarget();
				Player owner = actor.getPlayer();

				if(target != null && owner.getDistance(target) >= MAX_DIST_FOR_ATTACK)
				{
					if(!actor.isDead() && actor.isFollowMode())
					{
						owner.sendPacket(SystemMsg.THE_DISTANCE_IS_TOO_FAR_AND_SO_THE_CASTING_HAS_BEEN_STOPPED);
						setIntention(CtrlIntention.AI_INTENTION_FOLLOW, owner, Config.FOLLOW_RANGE);
						thinkActive();
					}
				}
			}

			addTimer(CHECK_ATTACK_DIST_TIMER, 1000);
		}
		else
			super.onEvtTimer(timerId, arg1, arg2);
	}
}
