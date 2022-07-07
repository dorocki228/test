package l2s.gameserver.ai;

import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

import l2s.commons.lang.reference.HardReference;
import l2s.commons.threading.RunnableImpl;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Skill.SkillTargetType;
import l2s.gameserver.model.World;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.utils.Location;

public class PlayableAI extends CharacterAI {
	private final AtomicInteger thinking;
	protected Object _intention_arg0;
	protected Object _intention_arg1;
	protected Skill _skill;
	protected Skill _replacedSkill;
	private AINextAction _nextAction;
	private Object _nextAction_arg0;
	private Object _nextAction_arg1;
	private boolean _nextAction_arg2;
	private boolean _nextAction_arg3;
	protected boolean _forceUse;
	private boolean _dontMove;
	protected ScheduledFuture<?> _followTask;
	private boolean updateMovePath = false;

	public PlayableAI(Playable actor) {
		super(actor);
		thinking = new AtomicInteger(0);
		_intention_arg0 = null;
		_intention_arg1 = null;
	}

	@Override
	public void changeIntention(CtrlIntention intention, Object arg0, Object arg1) {
		super.changeIntention(intention, arg0, arg1);
		_intention_arg0 = arg0;
		_intention_arg1 = arg1;
	}

	@Override
	public void setIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
		_intention_arg0 = null;
		_intention_arg1 = null;
		super.setIntention(intention, arg0, arg1);
	}

	@Override
	protected void onIntentionCast(Skill skill, Creature target)
	{
		super.onIntentionCast(_skill = skill, target);
	}

	@Override
	public void setNextAction(AINextAction action, Object arg0, Object arg1, boolean arg2, boolean arg3)
	{
		_nextAction = action;
		_nextAction_arg0 = arg0;
		_nextAction_arg1 = arg1;
		_nextAction_arg2 = arg2;
		_nextAction_arg3 = arg3;
	}

	public boolean setNextIntention()
	{
		AINextAction nextAction = _nextAction;
		Object nextAction_arg0 = _nextAction_arg0;
		Object nextAction_arg2 = _nextAction_arg1;
		boolean nextAction_arg3 = _nextAction_arg2;
		boolean nextAction_arg4 = _nextAction_arg3;
		Playable actor = getActor();

		if(nextAction == null)
			return false;

		if(nextAction == AINextAction.CAST)
		{
			if(actor.isActionsDisabled(false) || actor.isCastingNow())
				return false;
		}
		else if(actor.isActionsDisabled())
		{
			return false;
		}

		switch(nextAction)
		{
			case ATTACK:
			{
                if(!(nextAction_arg0 instanceof Creature))
                    return false;

                Creature target = (Creature) nextAction_arg0;
				_forceUse = nextAction_arg3;
				_dontMove = nextAction_arg4;
				clearNextAction();
				setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				break;
			}
			case CAST:
			{
                if(!(nextAction_arg0 instanceof Skill && nextAction_arg2 instanceof Creature))
                    return false;

				Skill skill = (Skill) nextAction_arg0;
				Creature target = (Creature) nextAction_arg2;
				_forceUse = nextAction_arg3;
				_dontMove = nextAction_arg4;

				if(skill.getNextAction() == Skill.NextAction.ATTACK && !actor.equals(target) && (target.isAutoAttackable(actor) || _forceUse))
					setNextAction(AINextAction.ATTACK, target, null, _forceUse, false);
				else
					clearNextAction();

				if(skill.checkCondition(actor, target, _forceUse, _dontMove, true))
				{
					setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
				}
				else if(!setNextIntention())
				{
					setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
					actor.sendActionFailed();
				}
				break;
			}
			case MOVE:
			{
                if(!(nextAction_arg0 instanceof Location && nextAction_arg2 instanceof Integer))
                    return false;

				Location loc = (Location) nextAction_arg0;
				Integer offset = (Integer) nextAction_arg2;
				clearNextAction();
				actor.moveToLocation(loc, offset, nextAction_arg3);
				break;
			}
			case REST:
			{
				actor.sitDown(null);
				break;
			}
			case INTERACT:
			{
                if(!(nextAction_arg0 instanceof GameObject))
                    return false;

				GameObject object = (GameObject) nextAction_arg0;
				clearNextAction();
				onIntentionInteract(object);
				break;
			}
			case PICKUP:
			{
                if(!(nextAction_arg0 instanceof GameObject))
                    return false;

				GameObject object = (GameObject) nextAction_arg0;
				clearNextAction();
				onIntentionPickUp(object);
				break;
			}
			case EQUIP:
			{
                if(!(nextAction_arg0 instanceof ItemInstance))
                    return false;

				ItemInstance item = (ItemInstance) nextAction_arg0;
				if(item.isEquipable())
					actor.useItem(item, nextAction_arg3, nextAction_arg4);
				clearNextAction();
				if(getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
					return false;
				break;
			}
			case COUPLE_ACTION:
			{
                if(!(nextAction_arg0 instanceof Creature && nextAction_arg2 instanceof Integer))
                    return false;

				Creature target = (Creature) nextAction_arg0;
				int socialId = (int) nextAction_arg2;
				_forceUse = nextAction_arg3;
				_nextAction = null;
				clearNextAction();
				onIntentionCoupleAction((Player) target, socialId);
				break;
			}
			default:
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public void clearNextAction()
	{
		_nextAction = null;
		_nextAction_arg0 = null;
		_nextAction_arg1 = null;
		_nextAction_arg2 = false;
		_nextAction_arg3 = false;
		updateMovePath = false;
	}

	@Override
	public AINextAction getNextAction()
	{
		return _nextAction;
	}

	@Override
	public Object[] getNextActionArgs()
	{
		return new Object[] { _nextAction_arg0, _nextAction_arg1 };
	}

	@Override
	protected void onEvtFinishCasting(Skill skill, Creature target, boolean success)
	{
		if(!setNextIntention())
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
	}

	@Override
	protected void onEvtReadyToAct()
	{
		if(!setNextIntention())
			onEvtThink();
	}

	@Override
	protected void onEvtArrived()
	{
		if(!setNextIntention())
		{
			if(getIntention() == CtrlIntention.AI_INTENTION_INTERACT || getIntention() == CtrlIntention.AI_INTENTION_PICK_UP)
				onEvtThink();
			else
				changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
		}
	}

	@Override
	protected void onEvtArrivedTarget()
	{
		switch(getIntention())
		{
			case AI_INTENTION_ATTACK:
			{
				thinkAttack(false);
				break;
			}
			case AI_INTENTION_CAST:
			{
				thinkCast(false);
				break;
			}
			case AI_INTENTION_FOLLOW:
			{
				thinkFollow();
				break;
			}
			default:
			{
				onEvtThink();
				break;
			}
		}
	}

	@Override
	protected final void onEvtThink() {
		Playable actor = getActor();
		CtrlIntention intention = getIntention();
		if (intention == CtrlIntention.AI_INTENTION_CAST) {
			if (actor.isActionsDisabled(false) || actor.isCastingNow())
				return;
		} else if (actor.isActionsDisabled())
			return;
		try {
			if (thinking.getAndIncrement() > 1)
				return;

			switch (intention) {
				case AI_INTENTION_ACTIVE: {
					thinkActive();
					break;
				}
				case AI_INTENTION_ATTACK: {
					thinkAttack(true);
					break;
				}
				case AI_INTENTION_CAST: {
					thinkCast(true);
					break;
				}
				case AI_INTENTION_PICK_UP: {
					thinkPickUp();
					break;
				}
				case AI_INTENTION_INTERACT: {
					thinkInteract();
					break;
				}
				case AI_INTENTION_FOLLOW: {
					thinkFollow();
					break;
				}
				case AI_INTENTION_COUPLE_ACTION: {
					thinkCoupleAction((Player) _intention_arg0, (Integer) _intention_arg1, false);
					break;
				}
			}
		} catch (Exception e) {
			AbstractAI._log.error("", e);
		} finally {
			thinking.decrementAndGet();
		}
	}

	protected void thinkActive()
	{}

	protected static boolean isThinkImplyZ(Playable actor, GameObject target)
	{
		if(actor.isFlying() || actor.isInWater())
			return true;

		if(target != null)
		{
			if(target.isDoor())
				return false;

			if(target.isCreature())
			{
				Creature creature = (Creature) target;

				if(creature.isInWater() || creature.isFlying())
					return true;
			}
		}
		return false;
	}

	protected void thinkFollow()
	{
		Playable actor = getActor();
		Creature target = (Creature) _intention_arg0;

		if(target == null || target.isAlikeDead())
		{
			clientActionFailed();
			return;
		}

		//Находимся слишком далеко от цели
		if (actor.getDistance(target) > 4000) {
			clientActionFailed();
			return;
		}

		//Уже следуем за этой целью
		if (actor.isFollowing() && actor.getFollowTarget() == target) {
			clientActionFailed();
			return;
		}

		int clientClipRange = actor.getPlayer() != null && actor.getPlayer().getNetConnection() != null ? actor.getPlayer().getNetConnection().getPawnClippingRange() : GameClient.DEFAULT_PAWN_CLIPPING_RANGE;
		boolean incZ = isThinkImplyZ(actor, target);
		int dist = (int) (!incZ ? actor.getDistance(target) : actor.getDistance3D(target));
		int followRange = actor.getActingRange() + target.getActingRange();
		final double minDistance = actor.getMinDistance(target);
		followRange += minDistance;

		if (dist > clientClipRange) {
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			clientActionFailed();
			clientStopMoving();
			return;
		}

		if (actor.isServitor()) {
			var owner = actor.getPlayer();
			if (!Objects.equals(target.getPlayer(), owner) && dist > 1500) {
				setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				clientActionFailed();
				clientStopMoving();
				return;
			}
		}

		if (dist <= followRange + 75 || actor.isMovementDisabled())
			clientActionFailed();

		if (_followTask != null) {
			_followTask.cancel(false);
			_followTask = null;
		}
		_followTask = scheduleThinkFollowTask();
	}

	protected ScheduledFuture<?> scheduleThinkFollowTask() {
		return ThreadPoolManager.getInstance().schedule(new ThinkFollow(getActor()), 250);
	}

	@Override
	protected void onIntentionInteract(GameObject object) {
		Playable actor = getActor();
		if (actor.isActionsDisabled()) {
			setNextAction(AINextAction.INTERACT, object, null, false, false);
			clientActionFailed();
			return;
		}
		clearNextAction();
		changeIntention(CtrlIntention.AI_INTENTION_INTERACT, object, null);
		onEvtThink();
	}

	@Override
	protected void onIntentionCoupleAction(Player player, Integer socialId)
	{
		_nextAction = null;
		clearNextAction();
		changeIntention(CtrlIntention.AI_INTENTION_COUPLE_ACTION, player, socialId);
		onEvtThink();
	}

	protected void thinkInteract() {
		Playable actor = getActor();
		GameObject target = (GameObject) _intention_arg0;
		if (target == null) {
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			return;
		}

		boolean incZ = isThinkImplyZ(actor, target);
		int dist = (int) (!incZ ? actor.getDistance(target) : actor.getDistance3D(target));
		final double minDistance = actor.getMinDistance(target);
		final int actRange = (int) (target.getActingRange() + minDistance);

		if (dist <= actRange) 
		{
			if (actor.isPlayer())
				((Player) actor).doInteract(target);
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		} else 
		{
			final int moveIndent = CharacterAI.getIndentRange(actRange);
			ThreadPoolManager.getInstance().execute(new RunnableImpl(){
				@Override
				public void runImpl()
				{
					actor.moveToRelative(target, moveIndent, actRange);
				}

			});
			setNextAction(AINextAction.INTERACT, target, null, false, false);
		}
	}

	@Override
	protected void onIntentionPickUp(GameObject object)
	{
		Playable actor = getActor();
		if(actor.isActionsDisabled())
		{
			setNextAction(AINextAction.PICKUP, object, null, false, false);
			clientActionFailed();
			return;
		}
		clearNextAction();
		changeIntention(CtrlIntention.AI_INTENTION_PICK_UP, object, null);
		onEvtThink();
	}

	protected void thinkPickUp()
	{
		Playable actor = getActor();
		GameObject target = (GameObject) _intention_arg0;
		if(target == null)
		{
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			return;
		}
		if(actor.isInRange(target, 30L) && Math.abs(actor.getZ() - target.getZ()) < 50)
		{
			if(actor.isPlayer() || actor.isPet())
				actor.doPickupItem(target);
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
		else
			ThreadPoolManager.getInstance().execute(() -> {
				actor.moveToLocation(target.getLoc(), 10, true);
				setNextAction(AINextAction.PICKUP, target, null, false, false);
			});
	}

	protected void thinkAttack(boolean checkRange)
	{
		Playable actor = getActor();
		Player player = actor.getPlayer();
		if(player == null) {
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			return;
		}
		if (actor.isActionsDisabled() || actor.isAttackingDisabled()) {
			actor.sendActionFailed();
			return;
		}
		Creature attack_target = getAttackTarget();
		if (attack_target == null || attack_target.isDead()) {
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			actor.sendActionFailed();
			return;
		}

		int clientClipRange = player.getNetConnection() != null ? player.getNetConnection().getPawnClippingRange() : GameClient.DEFAULT_PAWN_CLIPPING_RANGE;
		boolean incZ = isThinkImplyZ(actor, attack_target);
		int dist = (int) (incZ == false ? actor.getDistance(attack_target) : actor.getDistance3D(attack_target));

		if (dist >= clientClipRange) {
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			actor.sendActionFailed();
			return;
		}

		if (actor.isFakeDeath())
			actor.breakFakeDeath();

		boolean isPosessed = actor.isServitor() && (((Servitor) actor).isDepressed());

		if ((attack_target == null) || (attack_target.isDead()) || ((!isPosessed) && (_forceUse ? !attack_target.isAttackable(actor) : !attack_target.isAutoAttackable(actor)))) {
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			actor.sendActionFailed();
			return;
		}

		if (!checkRange) {
			clientStopMoving();
			actor.doAttack(attack_target);
			return;
		}

		int atkRange = actor.getPhysicalAttackRange();
		final double minDistance = actor.getMinDistance(attack_target);
		atkRange += minDistance;

		final boolean useActAsAtkRange = attack_target.isDoor();
		final int collisions = (int) (actor.getColRadius() + attack_target.getColRadius());
		
		if (dist <= atkRange + 12) 
		{
			if (!GeoEngine.canSeeTarget(actor, attack_target, incZ)) {
				actor.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
				setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				actor.sendActionFailed();
				return;
			}
			clientStopMoving();
			actor.doAttack(attack_target);
			return;
		}else
		if(!_dontMove)
		{
			final int moveIndent = CharacterAI.getIndentRange(atkRange) + (useActAsAtkRange ? 0 : collisions);
			final int moveRange = Math.max(moveIndent, atkRange + (useActAsAtkRange ? 0 : collisions));
			ThreadPoolManager.getInstance().execute(new RunnableImpl(){
				@Override
				public void runImpl()
				{
					actor.moveToRelative(attack_target, moveIndent, moveRange);
				}

			});
		}
		else
		{
			actor.sendActionFailed();
		}

		//ThreadPoolManager.getInstance().execute(new ExecuteFollow(attack_target, atkRange, null));
	}

	protected boolean thinkCast(boolean checkRange) {
		Playable actor = getActor();
		Creature target = getCastTarget();

		if (_skill.getSkillType() == Skill.SkillType.CRAFT || _skill.isToggle() && _skill.getHitTime() <= 0) {
			if (_skill.checkCondition(actor, target, _forceUse, _dontMove, true))
				actor.doCast(_skill.getEntry(), target, _forceUse);
			return true;
		}

		if (target == null || target.isDead() != _skill.getCorpse() && !_skill.isNotTargetAoE()) {
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			actor.sendActionFailed();
			return false;
		}

		boolean isGroundSkill = _skill.getTargetType() == SkillTargetType.TARGET_GROUND;
		final Location groundLoc;
		if (isGroundSkill) {
			if (!actor.isPlayer()) {
				setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				actor.sendActionFailed();
				return false;
			}
			groundLoc = actor.getPlayer().getGroundSkillLoc();
			if (groundLoc == null) {
				setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				actor.sendActionFailed();
				return false;
			}
		} else {
			groundLoc = null;
		}

		boolean noRangeSkill = _skill.getCastRange() == -1 || !checkRange;
		boolean incZ = isThinkImplyZ(actor, target);
		int dist = (int) (!incZ ? actor.getDistance(target) : actor.getDistance3D(target));
		int castRange = Math.max(16, actor.getMagicalAttackRange(_skill));
		final double minDistance = actor.getMinDistance(target);
		castRange += minDistance;

		if (actor.isFakeDeath())
			actor.breakFakeDeath();

		if (noRangeSkill || dist <= castRange + 12) {
			boolean canSee = isGroundSkill || _skill.getSkillType() == Skill.SkillType.TAKECASTLE || GeoEngine.canSeeTarget(actor, target, actor.isFlying());
			if (!canSee) {
				actor.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
				setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				actor.sendActionFailed();
				return false;
			}

			if (_skill.getNextAction() == Skill.NextAction.ATTACK && !actor.equals(target) && (target.isAutoAttackable(actor) || _forceUse))
				setNextAction(AINextAction.ATTACK, target, null, _forceUse, false);
			else
				clearNextAction();

			if (_skill.checkCondition(actor, target, _forceUse, _dontMove, true)) {
				clientStopMoving();
				actor.doCast(_skill.getEntry(), target, _forceUse);
				return true;
			} else if (!setNextIntention()) {
				setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				actor.sendActionFailed();
			}
		} else if (!_dontMove) 
		{
			final int collisions = (int) (actor.getColRadius()+target.getColRadius());
			
			final int moveIndent = CharacterAI.getIndentRange(castRange)+(target.isDoor() ? 0 : collisions);
			final int moveRange = Math.max(moveIndent, castRange+(target.isDoor() ? 0 : collisions));
			ThreadPoolManager.getInstance().execute(new RunnableImpl()
			{
				@Override
				public void runImpl()
				{
					actor.moveToRelative(target, moveIndent, moveRange);
				}
			});
		} else {
			if (!isGroundSkill)
				actor.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			actor.sendActionFailed();
		}

		return false;
	}

	protected void thinkCoupleAction(Player target, Integer socialId, boolean cancel)
	{}

	@Override
	protected void onEvtDead(Creature killer)
	{
		clearNextAction();
		super.onEvtDead(killer);
	}

	@Override
	protected void onEvtFakeDeath()
	{
		clearNextAction();
		super.onEvtFakeDeath();
	}

	@Override
	public void Attack(GameObject target, boolean forceUse, boolean dontMove)
	{
		Playable actor = getActor();
		if(target.isCreature() && (actor.isActionsDisabled() || actor.isAttackingDisabled()))
		{
			setNextAction(AINextAction.ATTACK, target, null, forceUse, false);
			actor.sendActionFailed();
			return;
		}

		if(!forceUse && getIntention() == CtrlIntention.AI_INTENTION_CAST && _skill.getTargetType() == SkillTargetType.TARGET_ONE && getCastTarget() == target)
			return;

		_dontMove = dontMove;
		_forceUse = forceUse;
		clearNextAction();
		setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
	}

	@Override
	public boolean Cast(Skill skill, Creature target, boolean forceUse, boolean dontMove)
	{
		Playable actor = getActor();
		if(skill.isCanUseWhileAbnormal() && (actor.isMMuted() || actor.isPMuted() || actor.isStunned() || actor.isSleeping() || actor.isDecontrolled() || actor.isFrozen()))
		{
			actor.altUseSkill(skill, target);
			return true;
		}
		if(actor.getAbnormalList().containsEffects(1570))
		{
			clientActionFailed();
			return false;
		}
		if(skill.altUse() || skill.isToggle() && skill.getHitTime() <= 0)
		{
			if(skill.isToggle() && !skill.checkCondition(actor, target, forceUse, dontMove, true))
			{
				clientActionFailed();
				return false;
			}
			if((skill.isToggle() || skill.isHandler()) && !skill.isCanUseWhileAbnormal() && (actor.isStunned() || actor.isSleeping() || actor.isDecontrolled() || actor.isFrozen()))
			{
				clientActionFailed();
				return false;
			}
			actor.altUseSkill(skill, target);
			return true;
		}
		else
		{
			if(!actor.isActionsDisabled(false) && !actor.isCastingNow())
			{
				_forceUse = forceUse;
				_dontMove = dontMove;
				CtrlIntention intention = getIntention();
				clearNextAction();
				updateMovePath = intention == CtrlIntention.AI_INTENTION_CAST && _skill != null && skill != _skill;
				setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
				return true;
			}
			if(!skill.isHandler() && !actor.isSkillDisabled(skill))
			{
				setNextAction(AINextAction.CAST, skill, target, forceUse, dontMove);
				clientActionFailed();
				return true;
			}
			clientActionFailed();
			return false;
		}
	}

	@Override
	public Playable getActor() {
		return (Playable) super.getActor();
	}

	public enum AINextAction {
		ATTACK,
		CAST,
		MOVE,
		REST,
		PICKUP,
		EQUIP,
		INTERACT,
		COUPLE_ACTION
	}

	protected static class ThinkFollow extends RunnableImpl {
		private final HardReference<? extends Playable> _actorRef;

		public ThinkFollow(Playable actor) {
			_actorRef = actor.getRef();
		}

		@Override
		public void runImpl() {
			Playable actor = _actorRef.get();

			if (actor == null)
				return;

			PlayableAI actorAI = (PlayableAI) actor.getAI();
			if (actorAI.getIntention() != CtrlIntention.AI_INTENTION_FOLLOW)
				return;

			Creature target = (Creature) actorAI._intention_arg0;
			if (target == null || target.isAlikeDead()) {
				actorAI.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				return;
			}

			int clientClipRange = actor.getPlayer() != null && actor.getPlayer().getNetConnection() != null ? actor.getPlayer().getNetConnection().getPawnClippingRange() : GameClient.DEFAULT_PAWN_CLIPPING_RANGE;
			boolean incZ = isThinkImplyZ(actor, target);
			int dist = (int) (!incZ ? actor.getDistance(target) : actor.getDistance3D(target));
			int followRange = actor.getActingRange();
			final double minDistance = actor.getMinDistance(target);
			followRange += minDistance;

			if (dist > clientClipRange || dist > 2 << World.SHIFT_BY) {
				actorAI.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				actorAI.clientStopMoving();
				return;
			}

			Player player = actor.getPlayer();
			if (player == null || player.isLogoutStarted() || (actor.isPet() || actor.isSummon()) && player.getServitor(actor.getObjectId()) == null) {
				actorAI.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				actorAI.clientStopMoving();
				return;
			}
			/*if (!actor.isAfraid() && dist > followRange + 75 && (!actor.isFollowing() || actor.getFollowTarget() != target))
				actor.moveToLocation(target.getLoc(), followRange, false);*/

			final int followIndent = Math.min(clientClipRange, target.getActingRange());
			final int collisions = (int) (actor.getColRadius() + target.getColRadius());
			
			if(dist > followRange && (!actor.isFollowing() || actor.getFollowTarget() != target))
			{
				actor.moveToRelative(target, followIndent + collisions, followRange + collisions);
			}
			
			actorAI._followTask = actorAI.scheduleThinkFollowTask();
		}
	}

}
