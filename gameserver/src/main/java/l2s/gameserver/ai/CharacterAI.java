package l2s.gameserver.ai;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.*;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.c2s.ValidatePosition;
import l2s.gameserver.network.l2.s2c.DiePacket;
import l2s.gameserver.utils.Location;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;


public class CharacterAI extends AbstractAI {
	private final TIntSet _blockedTimers;
	private final List<ScheduledFuture<?>> _timers;
	private final TIntObjectMap<ScheduledFuture<?>> _tasks;

	public CharacterAI(Creature actor) {
		super(actor);
		_blockedTimers = new TIntHashSet();
		_timers = new CopyOnWriteArrayList<>();
		_tasks = new TIntObjectHashMap<>();
	}

	protected static int getIndentRange(final int range)
	{
		return (range < 300) ? (range/3*2) : (range-100);
	}
	
	@Override
	protected void onIntentionIdle() {
		clientStopMoving();
		changeIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
	}

	@Override
	protected void onIntentionActive()
	{
        clientStopMoving();
		changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
		onEvtThink();
	}

	@Override
	protected void onIntentionAttack(Creature target)
	{
		setAttackTarget(target);
        clientStopMoving();
		changeIntention(CtrlIntention.AI_INTENTION_ATTACK, target, null);
		onEvtThink();
	}

	@Override
	protected void onIntentionCast(Skill skill, Creature target)
	{
		setCastTarget(target);
		changeIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
		onEvtThink();
	}

	@Override
	protected void onIntentionFollow(Creature target, Integer offset)
	{
		changeIntention(CtrlIntention.AI_INTENTION_FOLLOW, target, offset);
		onEvtThink();
	}

	@Override
	protected void onIntentionInteract(GameObject object)
	{}

	@Override
	protected void onIntentionPickUp(GameObject item)
	{}

	@Override
	protected void onIntentionRest()
	{}

	@Override
	protected void onIntentionCoupleAction(Player player, Integer socialId)
	{}

	@Override
	protected void onIntentionReturnHome(boolean running)
	{}

	@Override
	protected void onEvtArrivedBlocked(Location blocked_at_pos) 
	{
		Creature actor = getActor();

		if (actor.isPlayer() && blocked_at_pos != null && !actor.isPhantom()) 
		{
			boolean validate = blocked_at_pos.distance3D(actor.getLoc()) > ValidatePosition.MIN_VALIDATE_DIFF
					&& ValidatePosition.isNotCastingFlySkillAndNotFalling((Player) actor);

			if (validate) {
				actor.validateLocation(0);
			}
		}
		onEvtThink();
	}

	@Override
	protected void onEvtForgetObject(GameObject object)
	{
		if(object == null)
			return;
		Creature actor = getActor();
		if(actor.isAttackingNow() && getAttackTarget() == object)
			actor.abortAttack(true, true);
		if(actor.isCastingNow() && getCastTarget() == object)
			actor.abortCast(true, true);
		if(getAttackTarget() == object)
			setAttackTarget(null);
		if(getCastTarget() == object)
			setCastTarget(null);
		if(actor.getTargetId() == object.getObjectId())
			actor.setTarget(null);
		if(actor.getFollowTarget() == object)
			actor.stopMove();
		for(Servitor servitor : actor.getServitors())
			servitor.getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		Creature actor = getActor();
		actor.abortAttack(true, true);
		actor.abortCast(true, true);
		actor.stopMove();
		actor.broadcastPacket(new DiePacket(actor));
        setIntention(CtrlIntention.AI_INTENTION_IDLE);
	}

	@Override
	protected void onEvtFakeDeath()
	{
        clientStopMoving();
        setIntention(CtrlIntention.AI_INTENTION_IDLE);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
	{}

	@Override
	protected void onEvtClanAttacked(Creature attacked_member, Creature attacker, int damage)
	{}

	public void Attack(GameObject target, boolean forceUse, boolean dontMove)
	{
        setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
	}

	public boolean Cast(Skill skill, Creature target)
	{
		return Cast(skill, target, false, false);
	}

	public boolean Cast(Skill skill, Creature target, boolean forceUse, boolean dontMove)
	{
        setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
		return true;
	}

	@Override
	protected void onEvtThink()
	{}

	@Override
	protected void onEvtAggression(Creature target, int aggro)
	{}

	@Override
	protected void onEvtFinishCasting(Skill skill, Creature target, boolean success)
	{}

	@Override
	protected void onEvtReadyToAct()
	{}

	@Override
	protected void onEvtArrived()
	{}

	@Override
	protected void onEvtArrivedTarget()
	{}

	@Override
	protected void onEvtTeleported()
	{}

	@Override
	protected void onEvtSeeSpell(Skill skill, Creature caster, Creature target)
	{}

	@Override
	protected void onEvtSpawn()
	{}

	@Override
	public void onEvtDeSpawn()
	{}

	public void stopAITask()
	{}

	public void startAITask()
	{}

	public void setNextAction(PlayableAI.AINextAction action, Object arg0, Object arg1, boolean arg2, boolean arg3)
	{}

	public void clearNextAction()
	{}

	public PlayableAI.AINextAction getNextAction()
	{
		return null;
	}

	public Object[] getNextActionArgs()
	{
		return new Object[] { null, null };
	}

	public boolean isActive()
	{
		return true;
	}

	@Override
	protected void onEvtTimer(int timerId, Object arg1, Object arg2)
	{
		stopTask(timerId);
	}

	@Override
	protected void onEvtScriptEvent(String event, Object arg1, Object arg2)
	{}

	@Override
	protected void onEvtMenuSelected(Player player, int ask, int reply)
	{}

	@Override
	protected void onEvtKnockDown(Creature attacker)
	{
		Creature actor = getActor();
		actor.stopAttackStanceTask();
        clientStopMoving();
		onEvtAttacked(attacker, null, 1);
	}

	@Override
	protected void onEvtKnockBack(Creature attacker)
	{
		Creature actor = getActor();
		actor.stopAttackStanceTask();
        clientStopMoving();
		onEvtAttacked(attacker, null, 1);
	}

	@Override
	protected void onEvtFlyUp(Creature attacker)
	{
		Creature actor = getActor();
		actor.stopAttackStanceTask();
        clientStopMoving();
		onEvtAttacked(attacker, null, 1);
	}

	public void addTimer(int timerId, long delay)
	{
        addTimer(timerId, null, null, delay);
	}

	public void addTimer(int timerId, Object arg1, long delay)
	{
        addTimer(timerId, arg1, null, delay);
	}

	public void addTimer(int timerId, Object arg1, Object arg2, long delay)
	{
		_timers.add(ThreadPoolManager.getInstance().schedule(new Timer(timerId, arg1, arg2), delay));
	}

	public void addTask(int timerId, long delay)
	{
        addTask(timerId, null, null, delay);
	}

	public void addTask(int timerId, Object arg1, long delay)
	{
        addTask(timerId, arg1, null, delay);
	}

	public void addTask(int timerId, Object arg1, Object arg2, long delay)
	{
		stopTask(timerId);
		_tasks.put(timerId, ThreadPoolManager.getInstance().schedule(new Timer(timerId, arg1, arg2), delay));
	}

	public boolean haveTask(int timerId)
	{
		ScheduledFuture<?> task = _tasks.get(timerId);
		return task != null && !task.isCancelled() && !task.isDone();
	}

	public void stopTask(int timerId)
	{
		ScheduledFuture<?> task = _tasks.remove(timerId);
		if(task != null)
		{
			task.cancel(false);
			task = null;
		}
	}

	public void stopAllTaskAndTimers()
	{
		for(ScheduledFuture<?> timer : _timers)
			timer.cancel(false);
		for(ScheduledFuture<?> task : _tasks.valueCollection())
			task.cancel(false);
		_blockedTimers.clear();
		_timers.clear();
		_tasks.clear();
	}

	public void blockTimer(int timerId)
	{
		_blockedTimers.add(timerId);
	}

	public void unblockTimer(int timerId)
	{
		_blockedTimers.remove(timerId);
	}

	protected void broadCastScriptEvent(String event, int radius)
	{
        broadCastScriptEvent(event, null, null, radius);
	}

	protected void broadCastScriptEvent(String event, Object arg1, int radius)
	{
        broadCastScriptEvent(event, arg1, null, radius);
	}

	protected void broadCastScriptEvent(String event, Object arg1, Object arg2, int radius)
	{
		List<NpcInstance> npcs = World.getAroundNpc(getActor(), radius, radius);
		for(NpcInstance npc : npcs)
			npc.getAI().notifyEvent(CtrlEvent.EVT_SCRIPT_EVENT, event, arg1, arg2);
	}

	public int getMaxHateRange()
	{
		return 0;
	}

	protected class Timer implements Runnable
	{
		private final int _timerId;
		private final Object _arg1;
		private final Object _arg2;

		public Timer(int timerId, Object arg1, Object arg2)
		{
			_timerId = timerId;
			_arg1 = arg1;
			_arg2 = arg2;
		}

		@Override
		public void run()
		{
			if(_blockedTimers.contains(_timerId))
				return;
            notifyEvent(CtrlEvent.EVT_TIMER, _timerId, _arg1, _arg2);
		}
	}

	public boolean returnHome(boolean running)
	{
		return false;
	}
}
