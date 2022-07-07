package l2s.gameserver.ai;

import l2s.commons.lang.reference.HardReference;
import l2s.commons.math.random.RndSelector;
import l2s.commons.math.random.RndSelector.RndNode;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.AggroList.AggroInfo;
import l2s.gameserver.model.*;
import l2s.gameserver.model.instances.DecoyInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.QuestEventType;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.skills.EffectUseType;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.taskmanager.AiTaskManager;
import l2s.gameserver.templates.skill.EffectTemplate;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.NpcUtils;
import l2s.gameserver.utils.PositionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static l2s.gameserver.Config.MAX_PURSUE_RANGE;

public class DefaultAI extends NpcAI {
	protected static final Logger _log = LoggerFactory.getLogger(DefaultAI.class);
	public static final int TaskDefaultWeight = 10000;
	protected long AI_TASK_ATTACK_DELAY = Config.AI_TASK_ATTACK_DELAY;
	protected long AI_TASK_ACTIVE_DELAY = Config.AI_TASK_ACTIVE_DELAY;
	protected long AI_TASK_DELAY_CURRENT = AI_TASK_ACTIVE_DELAY;
	private final int MAX_HATE_RANGE = 2000;
	private int _maxPursueRange;
	protected ScheduledFuture<?> _aiTask;
	protected ScheduledFuture<?> _runningTask;
	protected ScheduledFuture<?> _madnessTask;
	private final Lock _thinking = new ReentrantLock();
	protected boolean _def_think = false;
	protected long _globalAggro;
	protected long _randomAnimationEnd;
	protected int _pathfindFails;
	protected final NavigableSet<Task> _tasks = new ConcurrentSkipListSet<>(TaskComparator.getInstance());
	protected final Skill[] _damSkills;
	protected final Skill[] _dotSkills;
	protected final Skill[] _debuffSkills;
	protected final Skill[] _healSkills;
	protected final Skill[] _buffSkills;
	protected final Skill[] _stunSkills;
	protected long _lastActiveCheck;
	protected long _checkAggroTimestamp = 0L;
	protected long _attackTimeout;
	protected long _teleportTimeout;
	protected long _lastFactionNotifyTime = 0L;
	protected long _minFactionNotifyInterval = 10000;
	protected Object _intention_arg0 = null;
	protected Object _intention_arg1 = null;
	protected boolean _isGlobal;
	private final boolean _isSearchingMaster;
	private final boolean _canRestoreOnReturnHome;
	private final int MAX_PURSE_RANGE_FOR_MINIONS = 4000;
	private static final RndNode<Skill>[] EMPTY_SKILL_RND_NODES = new RndNode[0];

	public void addTaskCast(Creature target, Skill skill)
	{
		Task task = new Task();
		task.type = TaskType.CAST;
		task.target = target.getRef();
		task.skill = skill;
		_tasks.add(task);
		_def_think = true;
	}

	public void addTaskBuff(Creature target, Skill skill)
	{
		Task task = new Task();
		task.type = TaskType.BUFF;
		task.target = target.getRef();
		task.skill = skill;
		_tasks.add(task);
		_def_think = true;
	}

	public void addTaskAttack(Creature target)
	{
		Task task = new Task();
		task.type = TaskType.ATTACK;
		task.target = target.getRef();
		_tasks.add(task);
		_def_think = true;
	}

	public void addTaskAttack(Creature target, Skill skill, int weight)
	{
		Task task = new Task();
		task.type = skill.isOffensive() ? TaskType.CAST : TaskType.BUFF;
		task.target = target.getRef();
		task.skill = skill;
		task.weight = weight;
		_tasks.add(task);
		_def_think = true;
	}

	public void addTaskMove(Location loc, boolean pathfind)
	{
		Task task = new Task();
		task.type = TaskType.MOVE;
		task.loc = loc;
		task.pathfind = pathfind;
		_tasks.add(task);
		_def_think = true;
	}

	protected void addTaskMove(int locX, int locY, int locZ, boolean pathfind) {
		addTaskMove(new Location(locX, locY, locZ), pathfind);
	}

	public DefaultAI(NpcInstance actor) {
		super(actor);

		setAttackTimeout(Long.MAX_VALUE);

		_damSkills = actor.getTemplate().getDamageSkills();
		_dotSkills = actor.getTemplate().getDotSkills();
		_debuffSkills = actor.getTemplate().getDebuffSkills();
		_buffSkills = actor.getTemplate().getBuffSkills();
		_stunSkills = actor.getTemplate().getStunSkills();
		_healSkills = actor.getTemplate().getHealSkills();

		_maxPursueRange = Math.max(actor.getAggroRange(), actor.getParameter("max_pursue_range", actor.isRaid() ? Config.MAX_PURSUE_RANGE_RAID : actor.isUnderground() ? Config.MAX_PURSUE_UNDERGROUND_RANGE : MAX_PURSUE_RANGE));
		_minFactionNotifyInterval = actor.getParameter("FactionNotifyInterval", 1000);
		_isGlobal = actor.getParameter("GlobalAI", false) || isHaveWalkerRoute();
		_isSearchingMaster = actor.getParameter("searchingMaster", false);
		_canRestoreOnReturnHome = actor.getParameter("restore_on_return_home", false);
	}

	@Override
	public void changeIntention(CtrlIntention intention, Object arg0, Object arg1)
	{
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
	public void run()
	{
		if(_aiTask == null)
			return;
		if(!isGlobalAI() && System.currentTimeMillis() - _lastActiveCheck > 60000L)
		{
			_lastActiveCheck = System.currentTimeMillis();
			NpcInstance actor = getActor();
			WorldRegion region = actor == null ? null : actor.getCurrentRegion();
			if(region == null || !region.isActive())
			{
				stopAITask();
				return;
			}
		}
		onEvtThink();
	}

	@Override
	public final synchronized void startAITask()
	{
		if(_aiTask == null)
		{
			AI_TASK_DELAY_CURRENT = AI_TASK_ACTIVE_DELAY;
			_aiTask = AiTaskManager.getInstance().scheduleAtFixedRate(this, 0L, AI_TASK_DELAY_CURRENT);
		}
		super.startAITask();
	}

	protected final synchronized void switchAITask(long NEW_DELAY)
	{
		if(_aiTask != null)
		{
			if(AI_TASK_DELAY_CURRENT == NEW_DELAY)
				return;
			_aiTask.cancel(false);
		}
		AI_TASK_DELAY_CURRENT = NEW_DELAY;
		_aiTask = AiTaskManager.getInstance().scheduleAtFixedRate(this, 0L, AI_TASK_DELAY_CURRENT);
	}

	@Override
	public final synchronized void stopAITask()
	{
		if(_aiTask != null)
		{
			_aiTask.cancel(false);
			_aiTask = null;
		}
		super.stopAITask();
	}

	@Override
	public boolean isGlobalAI()
	{
		return _isGlobal;
	}

	protected boolean canSeeInSilentMove(Playable target)
	{
		return getActor().getParameter("canSeeInSilentMove", false) || !target.isSilentMoving();
	}

	protected boolean checkAggression(Creature target)
	{
		NpcInstance actor = getActor();
		if(getIntention() != CtrlIntention.AI_INTENTION_ACTIVE && getIntention() != CtrlIntention.AI_INTENTION_RETURN_HOME || !isGlobalAggro())
			return false;
		if(target.isAlikeDead())
			return false;
		if(!target.isTargetable(actor))
			return false;

		if(target.isPlayable())
		{
			if(!canSeeInSilentMove((Playable) target))
				return false;
			if(actor.getFaction().containsName("varka_silenos_clan") && target.getPlayer().getVarka() > 0)
				return false;
			if(actor.getFaction().containsName("ketra_orc_clan") && target.getPlayer().getKetra() > 0)
				return false;
			if(((Playable) target).isInNonAggroTime())
				return false;

			if(target.isPlayer())
			{
				Player player = target.getPlayer();
				if(player.isGMInvisible())
					return false;
				if(player.isInAwayingMode() && !Config.AWAY_PLAYER_TAKE_AGGRO)
					return false;
				if(!player.isActive())
					return false;
				if((actor.isMonster() || actor instanceof DecoyInstance) && (player.isInStoreMode() || player.isPrivateBuffer() || player.isInOfflineMode()))
					return false;
			}
		}

		return isInAggroRange(target) && canAttackCharacter(target) && GeoEngine.canSeeTarget(actor, target, false);
	}

	protected boolean isInAggroRange(Creature target)
	{
		NpcInstance actor = getActor();
		AggroInfo ai = actor.getAggroList().get(target);
		if(ai != null && ai.hate > 0)
		{
			if(!target.isInRangeZ(actor.getLoc(), getMaxHateRange()))
				return false;
		}
		else if(!isAggressive() || !target.isInRangeZ(actor.getLoc(), actor.getAggroRange()))
			return false;
		return true;
	}

	protected void setIsInRandomAnimation(long time)
	{
		_randomAnimationEnd = System.currentTimeMillis() + time;
	}

	protected boolean randomAnimation()
	{
		if(isHaveRandomActions())
			return false;
		NpcInstance actor = getActor();
		if(actor.getParameter("noRandomAnimation", false))
			return false;
		if(actor.hasRandomAnimation() && !actor.isActionsDisabled() && !actor.isMoving() && !actor.isInCombat() && Rnd.chance(Config.RND_ANIMATION_RATE) && !actor.isKnockDowned() && !actor.isKnockBacked() && !actor.isFlyUp())
		{
			setIsInRandomAnimation(3000L);
			actor.onRandomAnimation();
			return true;
		}
		return false;
	}

	protected boolean randomWalk()
	{
		if(isHaveWalkerRoute())
			return false;
		NpcInstance actor = getActor();
		return !actor.getParameter("noRandomWalk", false) && !actor.isMoving() && maybeMoveToHome();
	}

	protected Creature getNearestTarget(List<Creature> targets)
	{
		NpcInstance actor = getActor();
		Creature nextTarget = null;
		long minDist = Long.MAX_VALUE;
		for(int i = 0; i < targets.size(); ++i)
		{
			Creature target = targets.get(i);
			long dist = actor.getXYZDeltaSq(target.getX(), target.getY(), target.getZ());
			if(dist < minDist)
				nextTarget = target;
		}
		return nextTarget;
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor.isActionsDisabled())
			return true;
		if(_randomAnimationEnd > System.currentTimeMillis())
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
			boolean aggressive = Rnd.chance(actor.getParameter("SelfAggressive", isAggressive() ? 100 : 0));
			if(!actor.getAggroList().isEmpty() || aggressive)
			{
				int count = 0;
				List<Creature> targets = World.getAroundCharacters(actor, Math.max(actor.getAggroRange(), 1000), 250);
				while(!targets.isEmpty())
				{
					if(++count > 1000)
						return false;
					Creature target = getNearestTarget(targets);
					if(target == null)
						break;
					if((aggressive || actor.getAggroList().get(target) != null) && checkAggression(target))
					{
						actor.getAggroList().addDamageHate(target, 0, 2);
						if(target.isServitor())
							actor.getAggroList().addDamageHate(target.getPlayer(), 0, 1);
						startRunningTask(AI_TASK_ATTACK_DELAY);
                        setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
						return true;
					}
					targets.remove(target);
				}
			}
		}

		if(actor.isMinion())
		{
			NpcInstance leader = actor.getLeader();

			if(leader != null)
			{
				if(leader.isDead())
				{
					actor.decayMe();
					return true;
				}

				double distance = actor.getDistance(leader);
				if(distance > MAX_PURSE_RANGE_FOR_MINIONS || !GeoEngine.canSeeTarget(actor, leader, false))
				{
					actor.teleToLocation(leader.getRndMinionPosition());
					return true;
				}

				if(distance > leader.getActingRange())
				{
					if(leader.isRunning())
						actor.setRunning();
					else
						actor.setWalking();

					addTaskMove(leader.getRndMinionPosition(), true);
					return true;
				}
			}
		}

        if(super.thinkActive())
            return true;

		return randomAnimation() || randomWalk();
	}

	@Override
	protected void onIntentionIdle()
	{
		NpcInstance actor = getActor();
		clearTasks();
		actor.stopMove();
		actor.getAggroList().clear(true);
		setAttackTimeout(Long.MAX_VALUE);
		setAttackTarget(null);
		changeIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
	}

	@Override
	protected void onIntentionActive()
	{
		NpcInstance actor = getActor();
		actor.stopMove();
		setAttackTimeout(Long.MAX_VALUE);
		if(getIntention() != CtrlIntention.AI_INTENTION_ACTIVE)
		{
			switchAITask(AI_TASK_ACTIVE_DELAY);
			changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
		}
		onEvtThink();
	}

	@Override
	protected void onIntentionAttack(Creature target)
	{
		NpcInstance actor = getActor();
		clearTasks();
		actor.stopMove();
		setAttackTarget(target);
		setAttackTimeout(getMaxAttackTimeout() + System.currentTimeMillis());
		setGlobalAggro(0L);

		if(getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
		{
			changeIntention(CtrlIntention.AI_INTENTION_ATTACK, target, null);
			switchAITask(AI_TASK_ATTACK_DELAY);
		}

		onEvtThink();
	}

	@Override
	protected boolean canAttackCharacter(Creature target)
	{
		return getActor().isAutoAttackable(target);
	}

	protected boolean isAggressive()
	{
		return getActor().isAggressive();
	}

	protected boolean checkTarget(Creature target, int range)
	{
		NpcInstance actor = getActor();

		if(target == null || target.isAlikeDead() || !actor.isInRangeZ(target, range) || !target.isTargetable())
			return false;

		if(target.isPlayable() && ((Playable) target).isInNonAggroTime())
			return false;

		boolean hidden = target.isPlayable() && target.isInvisible(actor);

		if(!hidden && actor.isConfused())
			return true;

		if(getIntention() != CtrlIntention.AI_INTENTION_ATTACK || !canAttackCharacter(target) && !target.getAI().canAttackCharacter(actor))
			return canAttackCharacter(target);

		AggroInfo ai = actor.getAggroList().get(target);

		if(ai == null)
			return false;

		if(hidden)
		{
			ai.hate = 0;
			return false;
		}

		return ai.hate > 0;
	}

	protected void thinkAttack()
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
			return;

		if(!actor.isInRange(actor.getSpawnedLoc(), _maxPursueRange) || actor.isRaid() && (actor.isInPeaceZone() || actor.isInZoneBattle() || actor.isInSiegeZone()))
		{
			returnHome(true, true, actor.isRunning());
			return;
		}

		if(actor.isMovementDisabled())
			setAttackTimeout(getMaxAttackTimeout() + System.currentTimeMillis());

		if(doTask() && !actor.isAttackingNow() && !actor.isCastingNow())
		{
			if(!createNewTask())
			{
				if(System.currentTimeMillis() > getAttackTimeout())
                    returnHome(false);
			}
		}
	}

	@Override
	protected void onEvtSpawn()
	{
		setGlobalAggro(System.currentTimeMillis() + getActor().getParameter("globalAggro", 10000L));
		if(getActor().isMinion() && getActor().getLeader() != null)
			_isGlobal = getActor().getLeader().getAI().isGlobalAI();
	}

	@Override
	protected void onEvtReadyToAct()
	{
		onEvtThink();
	}

	@Override
	protected void onEvtArrivedTarget()
	{
		onEvtThink();
	}

	protected boolean maybeNextTask(Task currentTask)
	{
		_tasks.remove(currentTask);
		return _tasks.isEmpty();
	}

	protected boolean doTask()
	{
		if(!_def_think)
			return true;

		Task currentTask = _tasks.pollFirst();
		if(currentTask == null)
		{
			clearTasks();
			return true;
		}

		NpcInstance actor = getActor();

		if(actor.isDead() || actor.isAttackingNow() || actor.isCastingNow())
			return false;

		switch(currentTask.type)
		{
			case MOVE:
			{
				if(actor.isMovementDisabled() || !getIsMobile())
					return true;
				if(actor.isInRange(currentTask.loc, 100L))
					return maybeNextTask(currentTask);
				if(actor.isMoving())
					return false;
				if (!actor.moveToLocation(currentTask.loc, 0, currentTask.pathfind)) {
					clientStopMoving();
					_pathfindFails = 0;
					actor.teleToLocation(currentTask.loc);
					return maybeNextTask(currentTask);
				}
				break;
			}
			case ATTACK: {
				Creature target = currentTask.target.get();

				if (target == null)
					return true;

				if (!checkTarget(target, getMaxHateRange()))
					return true;

				setAttackTarget(target);

				int pAtkRng = actor.getPhysicalAttackRange();
				final double minDistance = actor.getMinDistance(target);
				pAtkRng += minDistance;
				boolean incZ = actor.isFlying() || actor.isInWater() || target.isFlying() || target.isInWater();
				int dist = (int) (!incZ ? actor.getDistance(target) : actor.getDistance3D(target));

				if (dist <= pAtkRng + 40 && GeoEngine.canSeeTarget(actor, target, false)) {
					if (actor.isAttackingDisabled())
						return false;

					clientStopMoving();
					_pathfindFails = 0;
					setAttackTimeout(getMaxAttackTimeout() + System.currentTimeMillis());
					actor.doAttack(target);
					return maybeNextTask(currentTask);
				}

				if (actor.isMoving())
					return Rnd.chance(5);

				if (actor.isMovementDisabled() || !getIsMobile())
					return true;

				tryMoveToTarget(target, pAtkRng);
			}
			case CAST: {
				Creature target = currentTask.target.get();
				if (target == null)
					return true;

				Skill skill = currentTask.skill;

				if (skill == null)
					return true;

				if (actor.isMuted(skill) || actor.isSkillDisabled(skill) || actor.isUnActiveSkill(skill.getId()))
					return true;

				boolean isAoE = skill.getTargetType() == Skill.SkillTargetType.TARGET_AURA;
				int castRange = skill.getAOECastRange();

				if (!checkTarget(target, getMaxHateRange() + castRange))
					return true;

				setCastTarget(target);

				boolean incZ = actor.isFlying() || actor.isInWater() || target.isFlying() || target.isInWater();
				int dist = (int) (!incZ ? actor.getDistance(target) : actor.getDistance3D(target));
				final double minDistance = actor.getMinDistance(target);
				castRange += minDistance;

				if (dist <= castRange + 40 && GeoEngine.canSeeTarget(actor, target, false)) {
					clientStopMoving();
					_pathfindFails = 0;
					setAttackTimeout(getMaxAttackTimeout() + System.currentTimeMillis());
					actor.doCast(skill.getEntry(), isAoE ? actor : target, !target.isPlayable());
					return maybeNextTask(currentTask);
				}

				if (actor.isMoving())
					return Rnd.chance(10);

				if (actor.isMovementDisabled() || !getIsMobile())
					return true;

				tryMoveToTarget(target, castRange);
			}
			case BUFF:
			{
				Creature target = currentTask.target.get();

				if (target == null)
					return true;

				Skill skill = currentTask.skill;

				if (skill == null)
					return true;

				if (actor.isMuted(skill) || actor.isSkillDisabled(skill) || actor.isUnActiveSkill(skill.getId()))
					return true;

				if (skill.getTargetType() == Skill.SkillTargetType.TARGET_SELF) {
					actor.doCast(currentTask.skill.getEntry(), actor, false);
					return maybeNextTask(currentTask);
				}

				if (target == null || target.isAlikeDead() || !actor.isInRange(target, 2000L))
					return true;

				boolean isAoE = skill.getTargetType() == Skill.SkillTargetType.TARGET_AURA;
				int castRange = skill.getAOECastRange();

				boolean incZ = actor.isFlying() || actor.isInWater() || target.isFlying() || target.isInWater();
				int dist = (int) (!incZ ? actor.getDistance(target) : actor.getDistance3D(target));
				final double minDistance = actor.getMinDistance(target);
				castRange += minDistance;

				if (dist <= castRange + 40 && GeoEngine.canSeeTarget(actor, target, false)) {
					clientStopMoving();
					_pathfindFails = 0;
					actor.doCast(skill.getEntry(), isAoE ? actor : target, !target.isPlayable());
					return maybeNextTask(currentTask);
				}

				if (actor.isMoving())
					return Rnd.chance(10);

				if (actor.isMovementDisabled() || !getIsMobile())
					return true;

				tryMoveToTarget(target, castRange);
			}
		}
		return false;
	}

	protected boolean createNewTask()
	{
		return false;
	}

	protected boolean defaultNewTask()
	{
		clearTasks();

		NpcInstance actor = getActor();
		Creature target;

		if(actor == null || (target = prepareTarget()) == null)
			return false;

		double distance = actor.getDistance(target);

		return chooseTaskAndTargets(null, target, distance);
	}

	@Override
	protected void onEvtThink()
	{
		NpcInstance actor = getActor();
		if(actor == null || actor.isActionsDisabled() || actor.isAfraid())
			return;
		if(_randomAnimationEnd > System.currentTimeMillis())
			return;
		if(!_thinking.tryLock())
			return;
		try
		{
			if(!Config.BLOCK_ACTIVE_TASKS && getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)
				thinkActive();
			else if(getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
				thinkAttack();
			else if(getIntention() == CtrlIntention.AI_INTENTION_RETURN_HOME)
				thinkReturnHome();
		}
		finally
		{
			_thinking.unlock();
		}
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		NpcInstance actor = getActor();
		int transformer = actor.getParameter("transformOnDead", 0);
		int chance = actor.getParameter("transformChance", 100);
		if(transformer > 0 && Rnd.chance(chance))
		{
			int count = Rnd.get(1, actor.getParameter("transformCountMax", 1));
			long despawn = TimeUnit.SECONDS.toMillis(actor.getParameter("transformDespawnSec", 0));

			for(int i = 0; i < count; i++)
			{
				NpcInstance npc = NpcUtils.spawnSingle(transformer, Location.coordsRandomize(actor.getLoc(), 50, 100), actor.getReflection(), despawn);
				if(killer != null && killer.isPlayable())
				{
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, 100);
					killer.setTarget(npc);
					killer.sendPacket(npc.makeStatusUpdate(9, 10));
				}
			}
		}
		super.onEvtDead(killer);
	}

	@Override
	protected void onEvtClanAttacked(Creature attacked, Creature attacker, int damage)
	{
		if(damage <= 0)
			return;

		notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, damage / 2);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
	{
		NpcInstance actor = getActor();

		if(attacker == null || actor.isDead())
			return;

		if(attacker.isConfused())
			return;

		Player player = attacker.getPlayer();

		if(player != null)
		{
			List<QuestState> quests = player.getQuestsForEvent(actor, QuestEventType.ATTACKED_WITH_QUEST);
			if(quests != null)
				for(QuestState qs : quests)
					qs.getQuest().notifyAttack(actor, qs);
		}

		if(damage <= 0)
			return;

		if(!canAttackCharacter(attacker))
			return;

		Creature myTarget = attacker;

		if(attacker.isServitor())
		{
			Player summoner = attacker.getPlayer();
			if(summoner != null)
				if(_isSearchingMaster)
					myTarget = summoner;
				else
					actor.getAggroList().addDamageHate(summoner, 0, 1);
		}
		else if(attacker.isSymbolInstance())
			myTarget = attacker.getPlayer();

		if(myTarget == null)
			myTarget = attacker;

		actor.getAggroList().addDamageHate(myTarget, 0, (int) myTarget.calcStat(Stats.DAMAGE_HATE_BONUS, damage));

		isWaitingToReturnHome = false;
		isReturnHome = false;

		if(getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
		{
			if(!actor.isRunning())
				startRunningTask(AI_TASK_ATTACK_DELAY);

            setIntention(CtrlIntention.AI_INTENTION_ATTACK, myTarget);
		}
		notifyFriends(attacker, skill, damage);
	}

	@Override
	protected void onEvtAggression(Creature attacker, int aggro)
	{
		NpcInstance actor = getActor();

		if(attacker == null || actor.isDead())
			return;

		if(attacker.isConfused())
			return;

		Creature myTarget = attacker;

		if(aggro > 0)
		{
			if(attacker.isServitor())
			{
				Player summoner = attacker.getPlayer();
				if(summoner != null)
					if(_isSearchingMaster)
						myTarget = summoner;
					else
						actor.getAggroList().addDamageHate(summoner, 0, 1);
			}
			else if(attacker.isSymbolInstance())
				myTarget = attacker.getPlayer();
		}

		if(myTarget == null)
			myTarget = attacker;

		actor.getAggroList().addDamageHate(myTarget, 0, aggro);

		Creature oldTarget = getAttackTarget();
		Creature newTarget = prepareTarget();

		isWaitingToReturnHome = false;
		isReturnHome = false;

		if(newTarget == null)
			clientStopMoving();
		else if(newTarget != oldTarget)
		{
			startRunningTask(AI_TASK_ATTACK_DELAY);
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, newTarget);
		}
	}

	protected boolean maybeMoveToHome()
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
			return false;
		boolean randomWalk = actor.hasRandomWalk();
		Location sloc = actor.getSpawnedLoc();
		if(randomWalk && (!Config.RND_WALK || !Rnd.chance(Config.RND_WALK_RATE)))
			return false;
		boolean isInRange = actor.isInRangeZ(sloc, Config.MAX_DRIFT_RANGE);
		if(!randomWalk && isInRange)
			return false;
		Location pos = Location.findPointToStay(actor, sloc, 0, Config.MAX_DRIFT_RANGE);
		actor.setWalking();
		if(!actor.moveToLocation(pos, 0, true) && !isInRange && !(actor instanceof DecoyInstance))
			ThreadPoolManager.getInstance().execute(() -> actor.teleToLocation(sloc.x, sloc.y, GeoEngine.getHeight(sloc, actor.getGeoIndex())));
		return true;
	}

	public boolean returnHomeAndRestore(boolean running)
	{
		if(returnHome(running))
		{
			if(canRestoreOnReturnHome())
			{
				NpcInstance actor = getActor();
				actor.setCurrentHpMp(actor.getMaxHp(), actor.getMaxMp());
			}
			return true;
		}
		return false;
	}

	public boolean returnHome(boolean running)
	{
		return returnHome(true, Config.ALWAYS_TELEPORT_HOME, running);
	}

	protected boolean teleportHome()
	{
		return returnHome(true, true, false);
	}

	protected boolean returnHome(boolean clearAggro, boolean teleport, boolean running)
	{
		if(!teleport && getIntention() == CtrlIntention.AI_INTENTION_RETURN_HOME)
			return false;

		NpcInstance actor = getActor();
		if(!actor.hasRandomWalk() && !actor.isRaid() || actor instanceof DecoyInstance)
			return false;

		clearTasks();
		actor.stopMove();

		if(clearAggro)
			actor.getAggroList().clear(true);

		setAttackTimeout(Long.MAX_VALUE);
		setAttackTarget(null);

		Location sloc = actor.getSpawnedLoc();

		if(teleport)
		{
			changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
			ThreadPoolManager.getInstance().execute(() -> actor.teleToLocation(sloc.x, sloc.y, GeoEngine.getHeight(sloc, actor.getGeoIndex())));

			_pathfindFails = 0;
		}
		else
			setIntention(CtrlIntention.AI_INTENTION_RETURN_HOME, running);
		return true;
	}

	@Override
	protected void onIntentionReturnHome(boolean running)
	{
		NpcInstance actor = getActor();
		if(running || isHaveWalkerRoute())
			actor.setRunning();
		else
			actor.setWalking();
		changeIntention(CtrlIntention.AI_INTENTION_RETURN_HOME, null, null);
		onEvtThink();
	}

	private boolean isReturnHome;

	private void thinkReturnHome()
	{
		clearTasks();
		NpcInstance actor = getActor();
		Location spawnLoc = actor.getSpawnedLoc();

		if(actor.isMinion())
		{
			NpcInstance leader = actor.getLeader();

			if(leader != null)
			{
				if(leader.isDead())
				{
					actor.decayMe();
					return;
				}
			}
		}

		if(_pathfindFails > getMaxPathfindFails())
		{
			teleportHome();
			return;
		}

		if(actor.isInRange(spawnLoc, Math.min(_maxPursueRange, 100)))
		{

			isReturnHome = false;
			isWaitingToReturnHome = false;

			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
		else
		{

			if(isReturnHome && actor.isMoving())
				return;

			Location cur = actor.getLoc();
			Location loc = getReturnHomeLocation(false);

			if(!GeoEngine.canMoveToCoord(cur.x, cur.y, cur.z, loc.x, loc.y, loc.z, actor.getGeoIndex()) || !actor.moveToLocation(loc, 0, true))
				++_pathfindFails;

			isReturnHome = true;
		}
	}

	private boolean isWaitingToReturnHome;

	@Override
	protected void onEvtArrived()
	{

		if(getIntention() == CtrlIntention.AI_INTENTION_RETURN_HOME && !isWaitingToReturnHome)
		{
			NpcInstance actor = getActor();

			Location moveBackLoc = getReturnHomeLocation(true);

			actor.moveToLocation(moveBackLoc, 0, true);

			isWaitingToReturnHome = true;

			addTimer(9999, Rnd.get(3000, 20000));

			return;
		}
		else
			onEvtThink();

		super.onEvtArrived();
	}

	private Location getReturnHomeLocation(boolean backward)
	{
		NpcInstance actor = getActor();
		Location spawnLoc = actor.getSpawnedLoc();
		Location currentLoc = actor.getLoc();

		Location result = new Location();

		if(spawnLoc.distance(currentLoc) < 512)
			return spawnLoc;

		int h = PositionUtils.calculateHeadingFrom(currentLoc.x, currentLoc.y, spawnLoc.x, spawnLoc.y);
		double angle = PositionUtils.convertHeadingToDegree(h);
		double radian = Math.toRadians(angle + (backward ? 90 : -90));
		double range = backward ? 32 : 128;

		result.setX((int) (currentLoc.x - range * Math.sin(radian)));
		result.setY((int) (currentLoc.y + range * Math.cos(radian)));
		result.setZ(spawnLoc.z);
		result.correctGeoZ();

		return result;
	}

	@Override
	protected void onEvtTimer(int timerId, Object arg1, Object arg2) {
		if (timerId == 9999) {
			isReturnHome = false;
			isWaitingToReturnHome = false;
		} else {
			super.onEvtTimer(timerId, arg1, arg2);
		}
	}

	protected boolean canRestoreOnReturnHome()
	{
		return _canRestoreOnReturnHome;
	}

	protected Creature prepareTarget()
	{
		NpcInstance actor = getActor();

		if(actor.isConfused())
			return getAttackTarget();

		if(Rnd.chance(actor.getParameter("isMadness", 0)))
		{
			Creature randomHated = actor.getAggroList().getRandomHated(getMaxHateRange());

			if(randomHated != null && Math.abs(actor.getZ() - randomHated.getZ()) < 1000)
			{
				setAttackTarget(randomHated);
				if(_madnessTask == null)
				{
					actor.startConfused();
					_madnessTask = ThreadPoolManager.getInstance().schedule(new MadnessTask(), 10000L);
				}
				return randomHated;
			}
		}

		List<Creature> hateList = actor.getAggroList().getHateList(-1);
		Creature hated = null;

		for(Creature cha : hateList)
		{
			if(checkTarget(cha, getMaxHateRange()))
			{
				hated = cha;
				break;
			}
			actor.getAggroList().remove(cha, true);
		}

		if(hated != null)
		{
			setAttackTarget(hated);
			return hated;
		}

		return null;
	}

	protected boolean canUseSkill(Skill skill, Creature target, double distance)
	{
		NpcInstance actor = getActor();
		if(skill == null || skill.isNotUsedByAI())
			return false;
		if(skill.getTargetType() == Skill.SkillTargetType.TARGET_SELF && target != actor)
			return false;
		int castRange = skill.getAOECastRange();
		if(castRange <= 200 && distance > 200.0)
			return false;
		if(actor.isSkillDisabled(skill) || actor.isMuted(skill) || actor.isUnActiveSkill(skill.getId()))
			return false;
		double mpConsume2 = skill.getMpConsume2();
		if(skill.isMagic())
			mpConsume2 = actor.calcStat(Stats.MP_MAGIC_SKILL_CONSUME, mpConsume2, target, skill);
		else
			mpConsume2 = actor.calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsume2, target, skill);
		return actor.getCurrentMp() >= mpConsume2 && !target.getAbnormalList().containsEffects(skill.getId());
	}

	protected boolean canUseSkill(Skill sk, Creature target)
	{
		return canUseSkill(sk, target, 0.0);
	}

	protected Skill[] selectUsableSkills(Creature target, double distance, Skill[] skills)
	{
		if(skills == null || skills.length == 0 || target == null)
			return null;
		Skill[] ret = null;
		int usable = 0;
		for(Skill skill : skills)
			if(canUseSkill(skill, target, distance))
			{
				if(ret == null)
					ret = new Skill[skills.length];
				ret[usable++] = skill;
			}
		if(ret == null || usable == skills.length)
			return ret;
		if(usable == 0)
			return null;
		ret = Arrays.copyOf(ret, usable);
		return ret;
	}

	protected static Skill selectTopSkillByDamage(Creature actor, Creature target, double distance, Skill[] skills)
	{
		if(skills == null || skills.length == 0)
			return null;
		if(skills.length == 1)
			return skills[0];
		Skill oneTargetSkill = null;
		for(Skill skill : skills)
			if(skill.oneTarget() && (oneTargetSkill == null || skill.getCastRange() >= distance && distance / oneTargetSkill.getCastRange() < distance / skill.getCastRange()))
				oneTargetSkill = skill;
		if(oneTargetSkill != null && oneTargetSkill.getCastRange() > 300 && distance < 200.0)
			oneTargetSkill = null;

		RndNode<Skill>[] nodes = EMPTY_SKILL_RND_NODES;

		for(Skill skill2 : skills) {
			if (!skill2.oneTarget()) {
				double weight = skill2.getSimpleDamage(actor, target) / 10.0 + distance / skill2.getCastRange() * 100.0;
				if (weight < 1.0)
					weight = 1.0;
				nodes = ArrayUtils.add(nodes, RndNode.create(skill2, (int) weight));
			}
			else {
				double weight = Math.max(skill2.getSimpleDamage(actor, target) * skill2.getAOECastRange() / distance, 1);
				nodes = ArrayUtils.add(nodes, RndNode.create(skill2, (int) weight));
			}
		}

		Skill aoeSkill = RndSelector.createAndClean(nodes).select();
		if(aoeSkill == null)
			return oneTargetSkill;
		if(oneTargetSkill == null)
			return aoeSkill;
		if(Rnd.chance(90))
			return oneTargetSkill;
		return aoeSkill;
	}

	protected static Skill selectTopSkillByDebuff(Creature actor, Creature target, double distance, Skill[] skills)
	{
		if(skills == null || skills.length == 0)
			return null;

		RndNode<Skill>[] nodes = EMPTY_SKILL_RND_NODES;

		boolean isRaid = actor.isRaid();
		boolean isRaidMinion = actor.isMinion() && ((NpcInstance) actor).getLeader().isRaid();

		debuffs: for(Skill skill : skills)
		{
			//Затычка для рб
			if(isRaid || isRaidMinion)
			{
				for(EffectTemplate eff : skill.getEffectTemplates(EffectUseType.NORMAL))
				{
					switch(eff.getEffectType())
					{
						case Bluff:
						case i_dispel_all:
						case i_dispel_by_category:
						case i_dispel_by_slot:
						case i_dispel_by_slot_myself:
						case i_dispel_by_slot_probability:
						case p_block_move:
						{
							continue debuffs;
						}
					}
				}
			}

			if(skill.getSameByAbnormalType(target) == null)
			{
				double weight;
				if((weight = 100.0 * skill.getAOECastRange() / distance) <= 0.0)
					weight = 1.0;
				nodes = ArrayUtils.add(nodes, RndNode.create(skill, (int) weight));
			}
		}

		return RndSelector.createAndClean(nodes).select();
	}

	protected static Skill selectTopSkillByBuff(Creature target, Skill[] skills)
	{
		if(skills == null || skills.length == 0)
			return null;
		if(skills.length == 1)
			return skills[0];

		RndNode<Skill>[] nodes = EMPTY_SKILL_RND_NODES;

		for(Skill skill : skills) {
			if(skill.getSameByAbnormalType(target) == null)
			{
				double weight;
				if((weight = skill.getPower()) <= 0.0)
					weight = 1.0;
				nodes = ArrayUtils.add(nodes, RndNode.create(skill, (int) weight));
			}
		}

		return RndSelector.createAndClean(nodes).select();
	}

	protected static Skill selectTopSkillByHeal(Creature target, Skill[] skills)
	{
		if(skills == null || skills.length == 0)
			return null;
		double hpReduced = target.getMaxHp() - target.getCurrentHp();
		if(hpReduced < 1.0)
			return null;
		if(skills.length == 1)
			return skills[0];

		RndNode<Skill>[] nodes = EMPTY_SKILL_RND_NODES;

		for(Skill skill : skills)
		{
			double weight;
			if((weight = Math.abs(skill.getPower() - hpReduced)) <= 0.0)
				weight = 1.0;
			nodes = ArrayUtils.add(nodes, RndNode.create(skill, (int) weight));
		}

		return RndSelector.createAndClean(nodes).select();
	}
	protected void addDesiredSkill(Map<Skill, Integer> skillMap, Creature target, double distance, Skill[] skills)
	{
		if(skills == null || skills.length == 0 || target == null)
			return;
		for(Skill sk : skills)
            addDesiredSkill(skillMap, target, distance, sk);
	}

	protected void addDesiredSkill(Map<Skill, Integer> skillMap, Creature target, double distance, Skill skill)
	{
		if(skill == null || target == null || !canUseSkill(skill, target))
			return;
		int weight = (int) -Math.abs(skill.getAOECastRange() - distance);
		if(skill.getAOECastRange() >= distance)
			weight += 1000000;
		else if(skill.isNotTargetAoE() && skill.getTargets(getActor(), target, false).isEmpty())
			return;
		skillMap.put(skill, weight);
	}

	protected void addDesiredHeal(Map<Skill, Integer> skillMap, Skill[] skills)
	{
		if(skills == null || skills.length == 0)
			return;
		NpcInstance actor = getActor();
		double hpReduced = actor.getMaxHp() - actor.getCurrentHp();
		double hpPercent = actor.getCurrentHpPercents();
		if(hpReduced < 1.0)
			return;
		for(Skill sk : skills)
			if(canUseSkill(sk, actor) && sk.getPower() <= hpReduced)
			{
				int weight = (int) sk.getPower();
				if(hpPercent < 50.0)
					weight += 1000000;
				skillMap.put(sk, weight);
			}
	}

	protected void addDesiredBuff(Map<Skill, Integer> skillMap, Skill[] skills)
	{
		if(skills == null || skills.length == 0)
			return;
		NpcInstance actor = getActor();
		for(Skill sk : skills)
			if(canUseSkill(sk, actor))
				skillMap.put(sk, 1000000);
	}

	protected Skill selectTopSkill(Map<Skill, Integer> skillMap)
	{
		if(skillMap == null || skillMap.isEmpty())
			return null;
		int topWeight = Integer.MIN_VALUE;
		for(Skill next : skillMap.keySet())
		{
			int nWeight;
			if((nWeight = skillMap.get(next)) > topWeight)
				topWeight = nWeight;
		}
		if(topWeight == Integer.MIN_VALUE)
			return null;
		Skill[] skills = new Skill[skillMap.size()];
		int nWeight = 0;
		for(Map.Entry<Skill, Integer> e : skillMap.entrySet())
		{
			if(e.getValue() < topWeight)
				continue;
			skills[nWeight++] = e.getKey();
		}
		return skills[Rnd.get(nWeight)];
	}

	protected boolean chooseTaskAndTargets(Skill skill, Creature target, double distance)
	{
		NpcInstance actor = getActor();

		if(skill != null)
		{
			if(actor.isMovementDisabled() && distance > skill.getAOECastRange() + 60)
			{
				target = null;

				if(skill.isOffensive())
				{
					List<Creature> targets = new ArrayList<>();
					for(Creature cha : actor.getAggroList().getHateList(getMaxHateRange()))
						if(checkTarget(cha, skill.getAOECastRange() + 60))
						{
							if(!canUseSkill(skill, cha))
								continue;

							targets.add(cha);
						}

					if(!targets.isEmpty())
						target = targets.get(Rnd.get(targets.size()));
				}
			}
			if(target == null)
				return false;

			if(skill.isOffensive())
				addTaskCast(target, skill);
			else
				addTaskBuff(target, skill);

			return true;
		}
		else
		{

			if(actor.isMovementDisabled() && distance > actor.getPhysicalAttackRange() + 40)
			{
				target = null;

				List<Creature> targets = new ArrayList<>();

				for(Creature cha : actor.getAggroList().getHateList(getMaxHateRange()))
				{
					if(!checkTarget(cha, actor.getPhysicalAttackRange() + 40))
						continue;

					targets.add(cha);
				}

				if(!targets.isEmpty())
					target = targets.get(Rnd.get(targets.size()));
			}

			if(target == null)
				return false;

            addTaskAttack(target);
			return true;
		}
	}

	@Override
	public boolean isActive()
	{
		return super.isActive() && _aiTask != null;
	}

	protected void clearTasks()
	{
		_def_think = false;
		_tasks.clear();
	}

	protected void startRunningTask(long interval)
	{
		NpcInstance actor = getActor();
		if(_runningTask == null && !actor.isRunning())
			_runningTask = ThreadPoolManager.getInstance().schedule(new RunningTask(), interval);
	}

	protected boolean isGlobalAggro()
	{
		if(_globalAggro == 0L)
			return true;
		if(_globalAggro <= System.currentTimeMillis())
		{
			_globalAggro = 0L;
			return true;
		}
		return false;
	}

	public void setGlobalAggro(long value)
	{
		_globalAggro = value;
	}

	protected boolean defaultThinkBuff(int rateSelf)
	{
		return defaultThinkBuff(rateSelf, 0);
	}

	protected void notifyFriends(Creature attacker, Skill skill, int damage)
	{
		if(damage <= 0)
			return;

		NpcInstance actor = getActor();

		if(System.currentTimeMillis() - _lastFactionNotifyTime > _minFactionNotifyInterval)
		{
			_lastFactionNotifyTime = System.currentTimeMillis();
			if(actor.isMinion())
			{
				NpcInstance master = actor.getLeader();
				if(master != null)
				{
					if(!master.isDead() && master.isVisible())
						master.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, attacker, skill, damage);
					MinionList minionList = master.getMinionList();
					if(minionList != null)
						for(NpcInstance minion : minionList.getAliveMinions())
							if(minion != actor)
								minion.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, attacker, skill, damage);
				}
			}
			if(actor.hasMinions())
			{
				MinionList minionList2 = actor.getMinionList();
				if(minionList2.hasAliveMinions())
					for(NpcInstance minion2 : minionList2.getAliveMinions())
						minion2.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, attacker, skill, damage);
			}

			for(NpcInstance npc : activeFactionTargets())
				npc.getAI().notifyEvent(CtrlEvent.EVT_CLAN_ATTACKED, actor, attacker, damage);
		}
	}

	protected List<NpcInstance> activeFactionTargets()
	{
		NpcInstance actor = getActor();
		if(actor.getFaction().isNone())
			return Collections.emptyList();
		int range = actor.getFaction().getRange();
		List<NpcInstance> npcFriends = new ArrayList<>();
		for(NpcInstance npc : World.getAroundNpc(actor))
			if(!npc.isDead() && npc.isInRangeZ(actor, range) && npc.isInFaction(actor))
				npcFriends.add(npc);
		return npcFriends;
	}

	protected boolean defaultThinkBuff(int rateSelf, int rateFriends)
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
			return true;
		if(!Rnd.chance(rateSelf))
		{
			if(Rnd.chance(rateFriends))
				for(NpcInstance npc : activeFactionTargets())
				{
					double targetHp = npc.getCurrentHpPercents();
					Skill[] skills = targetHp < 50.0 ? selectUsableSkills(actor, 0.0, _healSkills) : selectUsableSkills(actor, 0.0, _buffSkills);
					if(skills != null)
					{
						if(skills.length == 0)
							continue;
						Skill skill = skills[Rnd.get(skills.length)];
						addTaskBuff(actor, skill);
						return true;
					}
				}
			return false;
		}
		double actorHp = actor.getCurrentHpPercents();
		Skill[] skills2 = actorHp < 50.0 ? selectUsableSkills(actor, 0.0, _healSkills) : selectUsableSkills(actor, 0.0, _buffSkills);
		if(skills2 == null || skills2.length == 0)
			return false;
		Skill skill2 = skills2[Rnd.get(skills2.length)];
		addTaskBuff(actor, skill2);
		return true;
	}

	protected boolean defaultFightTask()
	{
		clearTasks();
		NpcInstance actor = getActor();

		if(actor.isDead() || actor.isAMuted())
			return false;

		Creature target;

		if((target = prepareTarget()) == null)
			return false;

		double distance = actor.getDistance(target);
		double targetHp = target.getCurrentHpPercents();
		double actorHp = actor.getCurrentHpPercents();

		Skill[] dam = Rnd.chance(getRateDAM()) ? selectUsableSkills(target, distance, _damSkills) : null;
		Skill[] dot = Rnd.chance(getRateDOT()) ? selectUsableSkills(target, distance, _dotSkills) : null;
		Skill[] debuff = targetHp > 10.0 ? Rnd.chance(getRateDEBUFF()) ? selectUsableSkills(target, distance, _debuffSkills) : null : null;
		Skill[] stun = Rnd.chance(getRateSTUN()) ? selectUsableSkills(target, distance, _stunSkills) : null;
		Skill[] heal = actorHp < 50.0 ? Rnd.chance(getRateHEAL()) ? selectUsableSkills(actor, 0.0, _healSkills) : null : null;
		Skill[] buff = Rnd.chance(getRateBUFF()) ? selectUsableSkills(actor, 0.0, _buffSkills) : null;

		RndNode<Skill[]> rndNode = RndNode.create(dam, getRateDAM());
		RndNode<Skill[]> rndNode1 = RndNode.create(dot, getRateDOT());
		RndNode<Skill[]> rndNode2 = RndNode.create(debuff, getRateDEBUFF());
		RndNode<Skill[]> rndNode3 = RndNode.create(heal, getRateHEAL());
		RndNode<Skill[]> rndNode4 = RndNode.create(buff, getRateBUFF());
		RndNode<Skill[]> rndNode5 = RndNode.create(stun, getRateSTUN());
		RndNode<Skill[]> rndNode6 = RndNode.create(Skill.EMPTY_ARRAY, getRatePHYS());

		RndSelector<Skill[]> rndSelector =
				actor.isAMuted()
						? RndSelector.create(rndNode, rndNode1, rndNode2, rndNode3, rndNode4, rndNode5)
						: RndSelector.create(rndNode, rndNode1, rndNode2, rndNode3, rndNode4, rndNode5, rndNode6);

		Skill[] selected = rndSelector.select();
		boolean chance = getActor().isRaid() ? Rnd.chance(55) : Rnd.chance(33);
		if(selected != null && selected.length != 0 && chance)
		{
			if(selected == dam || selected == dot)
				return chooseTaskAndTargets(selectTopSkillByDamage(actor, target, distance, selected), target, distance);

			if(selected == debuff || selected == stun)
				return chooseTaskAndTargets(selectTopSkillByDebuff(actor, target, distance, selected), target, distance);

			if(selected == buff)
				return chooseTaskAndTargets(selectTopSkillByBuff(actor, selected), actor, distance);

			if(selected == heal)
				return chooseTaskAndTargets(selectTopSkillByHeal(actor, selected), actor, distance);
		}

		return chooseTaskAndTargets(null, target, distance);
	}

	public int getRatePHYS()
	{
		return 100;
	}

	public int getRateDOT()
	{
		return 0;
	}

	public int getRateDEBUFF()
	{
		return 0;
	}

	public int getRateDAM()
	{
		return 0;
	}

	public int getRateSTUN() {
		return 0;
	}

	public int getRateBUFF() {
		return 0;
	}

	public int getRateHEAL() {
		return 0;
	}

	public boolean getIsMobile() {
		return !getActor().getParameter("isImmobilized", false);
	}

	public int getMaxPathfindFails() {
		return 5;
	}

	public int getMaxAttackTimeout() {
		return 15000;
	}

	protected long getAttackTimeout() {
		return _attackTimeout;
	}

	public void setAttackTimeout(long time) {
		_attackTimeout = time;
	}

	public int getMaxTeleportTimeout() {
		return 10000;
	}

	protected long getTeleportTimeout() {
		return _teleportTimeout;
	}

	public void setTeleportTimeout(long time) {
		_teleportTimeout = time;
	}

	public void setMaxPursueRange(int value) {
		_maxPursueRange = value;
	}

	@Override
	public int getMaxHateRange() {
		return Math.max(getActor().getAggroRange(), MAX_HATE_RANGE);
	}

	protected boolean tryMoveToTarget(Creature target, int range) {
		NpcInstance actor = getActor();

		if (!actor.isInRange(actor.getSpawnedLoc(), _maxPursueRange) || actor.isRaid() && (actor.isInPeaceZone() || actor.isInZoneBattle() || actor.isInSiegeZone())) {
			returnHomeAndRestore(actor.isRunning());
			return false;
		}

		if (!actor.moveToLocation(target.getLoc(), range, true))
			++_pathfindFails;

		if (_pathfindFails >= getMaxPathfindFails()) {
			if (getTeleportTimeout() == Long.MAX_VALUE)
				setTeleportTimeout(getMaxTeleportTimeout() + System.currentTimeMillis());

			Location loc = GeoEngine.moveCheckForAI(target.getLoc(), actor.getLoc(), actor.getGeoIndex());
			if (!GeoEngine.canMoveToCoord(actor.getX(), actor.getY(), actor.getZ(), loc.x, loc.y, loc.z, actor.getGeoIndex()))
				loc = target.getLoc();
			if (target.isPlayable()) {
				AggroInfo hate = actor.getAggroList().get(target);
				if (hate == null || !actor.getReflection().isMain() && actor.isRaid()) {
					_pathfindFails = 0;
					returnHome(false);
					return false;
				}
			}
			if (actor.isInRange(target, _maxPursueRange) && GeoEngine.canSeeTarget(actor, target, false)) {
				_pathfindFails = 0;
				setTeleportTimeout(Long.MAX_VALUE);
				actor.teleToLocation(loc);
				return true;
			}

			if (getMaxTeleportTimeout() < System.currentTimeMillis()) {
				_pathfindFails = 0;
				setTeleportTimeout(Long.MAX_VALUE);
				returnHome(false);
				return false;
			}
		}
		return false;
	}

	public enum TaskType {
		MOVE,
		ATTACK,
		CAST,
		BUFF
	}

	public static class Task {
		public TaskType type;
		public Skill skill;
		public HardReference<? extends Creature> target;
		public Location loc;
		public boolean pathfind;
		public int weight;

		public Task() {
			weight = 10000;
		}
	}

	private static class TaskComparator implements Comparator<Task> {
		private static final Comparator<Task> instance = new TaskComparator();

		public static final Comparator<Task> getInstance() {
			return instance;
		}

		@Override
		public int compare(Task o1, Task o2) {
			if (o1 == null || o2 == null)
				return 0;
			return o2.weight - o1.weight;
		}

	}

	protected class RunningTask implements Runnable {
		@Override
		public void run() {
			getActor().setRunning();
			_runningTask = null;
		}
	}

	protected class MadnessTask implements Runnable {
		@Override
		public void run() {
			getActor().stopConfused();
			_madnessTask = null;
		}
	}
}
