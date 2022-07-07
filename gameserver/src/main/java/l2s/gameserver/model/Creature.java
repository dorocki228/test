package l2s.gameserver.model;

import static l2s.gameserver.Config.MOVE_TASK_QUANTUM_NPC;
import static l2s.gameserver.Config.MOVE_TASK_QUANTUM_PC;
import static l2s.gameserver.geodata.GeoMove.getIntersectPoint;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import gve.zones.model.GveZone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

import l2s.Phantoms.enums.PhantomType;
import l2s.commons.lang.reference.HardReference;
import l2s.commons.lang.reference.HardReferences;
import l2s.commons.listener.Listener;
import l2s.commons.threading.RunnableImpl;
import l2s.commons.util.Rnd;
import l2s.commons.util.concurrent.atomic.AtomicState;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CharacterAI;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.ai.PlayableAI;
import l2s.gameserver.ai.PlayableAI.AINextAction;
import l2s.gameserver.data.xml.holder.LevelBonusHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.data.xml.holder.TransformTemplateHolder;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.geodata.GeoMove;
import l2s.gameserver.model.GameObjectTasks.NotifyAITask;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.model.actor.basestats.CreatureBaseStats;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.actor.instances.creature.AbnormalList;
import l2s.gameserver.model.actor.listener.CharListenerList;
import l2s.gameserver.model.actor.recorder.CharStatsChangeRecorder;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.base.Sex;
import l2s.gameserver.model.base.SpecialEffectState;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.base.TransformType;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.StaticObjectInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.quest.QuestEventType;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.model.reference.L2Reference;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ActionFailPacket;
import l2s.gameserver.network.l2.s2c.AttackPacket;
import l2s.gameserver.network.l2.s2c.AutoAttackStartPacket;
import l2s.gameserver.network.l2.s2c.AutoAttackStopPacket;
import l2s.gameserver.network.l2.s2c.ChangeMoveTypePacket;
import l2s.gameserver.network.l2.s2c.ExAbnormalStatusUpdateFromTargetPacket;
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo;
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo.MagicAttackType;
import l2s.gameserver.network.l2.s2c.ExRotation;
import l2s.gameserver.network.l2.s2c.ExShowChannelingEffectPacket;
import l2s.gameserver.network.l2.s2c.ExTeleportToLocationActivate;
import l2s.gameserver.network.l2.s2c.FlyToLocationPacket;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.MTLPacket;
import l2s.gameserver.network.l2.s2c.MTPPacket;
import l2s.gameserver.network.l2.s2c.MagicSkillCanceled;
import l2s.gameserver.network.l2.s2c.MagicSkillLaunchedPacket;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.SetupGaugePacket;
import l2s.gameserver.network.l2.s2c.StatusUpdatePacket;
import l2s.gameserver.network.l2.s2c.StopMovePacket;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.network.l2.s2c.TeleportToLocationPacket;
import l2s.gameserver.network.l2.s2c.ValidateLocationPacket;
import l2s.gameserver.network.l2.s2c.updatetype.IUpdateTypeComponent;
import l2s.gameserver.permission.ActionPermissionComponent;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.skills.AbnormalType;
import l2s.gameserver.skills.EffectType;
import l2s.gameserver.skills.EffectUseType;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.TimeStamp;
import l2s.gameserver.stats.Calculator;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.stats.StatFunctions;
import l2s.gameserver.stats.StatTemplate;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.stats.funcs.Func;
import l2s.gameserver.stats.triggers.RunnableTrigger;
import l2s.gameserver.stats.triggers.TriggerInfo;
import l2s.gameserver.stats.triggers.TriggerType;
import l2s.gameserver.tables.GmListTable;
import l2s.gameserver.taskmanager.LazyPrecisionTaskManager;
import l2s.gameserver.taskmanager.RegenTaskManager;
import l2s.gameserver.templates.CreatureTemplate;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.templates.player.transform.TransformTemplate;
import l2s.gameserver.utils.Constants;
import l2s.gameserver.utils.EffectsComparator;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.PositionUtils;
import l2s.gameserver.utils.Util;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Creature extends GameObject {
	private static final Logger _log;
	public static final double HEADINGS_IN_PI = 10430.378350470453;
	public static final int INTERACTION_DISTANCE = 200;
	private final ActionPermissionComponent actionPermissionComponent;
	private Skill _castingSkill;
	private boolean _isCriticalBlowCastingSkill;
	private long _castInterruptTime;
	private long _animationEndTime;
	private int _castInterval;
	public Future<?> _skillTask;
	private Future<?> _skillLaunchedTask;
	private Future<?> _doCastTask;
	protected Future<?> _stanceTask;
	protected Runnable _stanceTaskRunnable;
	protected long _stanceEndTime;
	private Future<?> _deleteTask;
	public static final int CLIENT_BAR_SIZE = 352;
	private int _lastCpBarUpdate;
	private int _lastHpBarUpdate;
	private int _lastMpBarUpdate;
	protected double _currentCp;
	private double _currentHp;
	protected double _currentMp;
	protected boolean _isAttackAborted;
	protected long _attackEndTime;
	protected long _attackReuseEndTime;
	private int _poleAttackCount;
	private static final double[] POLE_VAMPIRIC_MOD;
	protected final Map<Integer, SkillEntry> _skills;
	protected Map<TriggerType, Set<TriggerInfo>> _triggers;
	protected IntObjectMap<TimeStamp> _skillReuses;
	protected volatile AbnormalList _effectList;
	protected volatile CharStatsChangeRecorder<? extends Creature> _statsRecorder;
	private final Set<AbnormalEffect> _abnormalEffects;
	private final AtomicBoolean isDead;
	protected AtomicBoolean isTeleporting;
	private boolean _fakeDeath;
	private boolean _isBlessedByNoblesse;
	private boolean _isSalvation;
	private boolean _meditated;
	private boolean _lockedTarget;
	private boolean _blocked;
	private final List<Abnormal> _deathImmunityEffects;
	private final AtomicState _afraid;
	private final AtomicState _muted;
	private final AtomicState _pmuted;
	private final AtomicState _amuted;
	private final AtomicState _paralyzed;
	private final AtomicState _moveBlocked;
	private final AtomicState _sleeping;
	private final AtomicState _stunned;
	private final AtomicState _immobilized;
	private final AtomicState _confused;
	private boolean _frozen;
	private final AtomicState _knockDowned;
	private final AtomicState _knockBacked;
	private final AtomicState _flyUp;
	private final AtomicState _healBlocked;
	private final AtomicState _damageBlocked;
	private final AtomicState _buffImmunity;
	private final AtomicState _debuffImmunity;
	private final AtomicState _effectImmunity;
	protected AtomicState _deathImmunity;
	private final AtomicState _distortedSpace;
	private final AtomicState _invisible;
	private final List<Abnormal> _invisibleEffects;
	protected SpecialEffectState _undying;
	private final AtomicBoolean _undyingFlag;
	private final AtomicState _invul;
	private final List<Abnormal> _invulEffects;
	private volatile HardReference<? extends Creature> _effectImmunityException;
	private volatile HardReference<? extends Creature> _damageBlockedException;
	private final AtomicState _weaponEquipBlocked;
	private boolean _flying;
	private volatile HardReference<? extends GameObject> target;
	private volatile HardReference<? extends Creature> _castingTarget;
	private int _rndCharges;
	private int _heading;
	private final Calculator[] _calculators;
	private CreatureTemplate _template;
	protected volatile CharacterAI _ai;
	protected String _name;
	protected String _title;
	protected TeamType _team;
	private boolean _isRegenerating;
	private final Lock regenLock;
	private Future<?> _regenTask;
	private Runnable _regenTaskRunnable;
	private final List<Zone> _zones;
	private final ReadWriteLock zonesLock;
	private final Lock zonesRead;
	private final Lock zonesWrite;
	protected volatile CharListenerList listeners;
	private final Lock statusListenersLock;
	protected HardReference<? extends Creature> reference;
	private boolean _isInTransformUpdate;
	private TransformTemplate _visualTransform;
	private boolean _isTargetable;
	protected CreatureBaseStats _baseStats;
	private Future<?> _updateEffectIconsTask;
	private final TIntSet _unActiveSkills;
	private Fraction fraction;
	private boolean _doCast;
	private final Location movingDestTempPos;
	private final List<List<Location>> _targetRecorder;
	private volatile boolean isOnlyPathFind;
	private List<Location> moveList;
	private Location destination;
	private Location _endLoc;
	private int _offset;
	private boolean _forestalling;
	private Location _flyLoc;
	private long _followTimestamp, _startMoveTime;
	private int _previousSpeed;
	private double greed;
	private final List<ScheduledFuture<?>> reApplyTasks;

	public Creature(int objectId, CreatureTemplate template) {
		super(objectId);
		_lastCpBarUpdate = -1;
		_lastHpBarUpdate = -1;
		_lastMpBarUpdate = -1;
		_currentCp = 0.0;
		_currentHp = 1.0;
		_currentMp = 1.0;
		_poleAttackCount = 0;
		_skills = new ConcurrentSkipListMap<>();
		_skillReuses = new CHashIntObjectMap<>();
		_abnormalEffects = new CopyOnWriteArraySet<>();
		isDead = new AtomicBoolean();
		isTeleporting = new AtomicBoolean();
		_deathImmunityEffects = new ArrayList<>();
		_afraid = new AtomicState();
		_muted = new AtomicState();
		_pmuted = new AtomicState();
		_amuted = new AtomicState();
		_paralyzed = new AtomicState();
		_moveBlocked = new AtomicState();
		_sleeping = new AtomicState();
		_stunned = new AtomicState();
		_immobilized = new AtomicState();
		_confused = new AtomicState();
		_knockDowned = new AtomicState();
		_knockBacked = new AtomicState();
		_flyUp = new AtomicState();
		_healBlocked = new AtomicState();
		_damageBlocked = new AtomicState();
		_buffImmunity = new AtomicState();
		_debuffImmunity = new AtomicState();
		_effectImmunity = new AtomicState();
		_deathImmunity = new AtomicState();
		_distortedSpace = new AtomicState();
		_invisible = new AtomicState();
		_invisibleEffects = new ArrayList<>();
		_undying = SpecialEffectState.FALSE;
		_undyingFlag = new AtomicBoolean(false);
		_invul = new AtomicState();
		_invulEffects = new ArrayList<>();
		_effectImmunityException = HardReferences.emptyRef();
		_damageBlockedException = HardReferences.emptyRef();
		_weaponEquipBlocked = new AtomicState();
		movingDestTempPos = new Location();
		_targetRecorder = new ArrayList<>();
		target = HardReferences.emptyRef();
		_castingTarget = HardReferences.emptyRef();
		_rndCharges = 0;
		_team = TeamType.NONE;
		regenLock = new ReentrantLock();
		_zones = new ArrayList<>();
		zonesLock = new ReentrantReadWriteLock();
		zonesRead = zonesLock.readLock();
		zonesWrite = zonesLock.writeLock();
		statusListenersLock = new ReentrantLock();
		_isInTransformUpdate = false;
		_visualTransform = null;
		_isTargetable = true;
		_baseStats = null;
		_flyLoc = null;
		_unActiveSkills = new TIntHashSet();
		_template = template;
		_calculators = new Calculator[Stats.NUM_STATS];
		StatFunctions.addPredefinedFuncs(this);
		reference = new L2Reference<>(this);
		if(!isObservePoint())
			GameObjectsStorage.put(this);
		actionPermissionComponent = ActionPermissionComponent.create();
		greed = 1.0;
		reApplyTasks = new CopyOnWriteArrayList<>();
	}

	@Override
	public HardReference<? extends Creature> getRef()
	{
		return reference;
	}

	public boolean isAttackAborted()
	{
		return _isAttackAborted;
	}

	public final void abortAttack(boolean force, boolean message)
	{
		if(isAttackingNow())
		{
			_attackEndTime = 0L;
			if(force)
				_isAttackAborted = true;
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			if(isPlayer() && message)
			{
				sendActionFailed();
                sendPacket(new SystemMessage(2268).addName(this));
			}

		}
	}

	public final void abortCast(boolean force, boolean message)
	{
		boolean cancelled = false;
		if(isCastingNow() && (force || canAbortCast()))
		{
			Skill castingSkill = getCastingSkill();
			if(castingSkill != null && castingSkill.isAbortable())
			{
				Future<?> skillTask = _skillTask;
				Future<?> skillLaunchedTask = _skillLaunchedTask;
				Future<?> doCastTask = _doCastTask;

				clearCastVars();
				if(doCastTask != null)
					doCastTask.cancel(true);
				if(skillTask != null)
					skillTask.cancel(false);
				if(skillLaunchedTask != null)
					skillLaunchedTask.cancel(false);
				cancelled = true;

                Creature castingTarget = getCastingTarget();
                castingSkill.onAbortCast(this, castingTarget);
			}
		}
		if(cancelled)
		{
            broadcastPacket(new MagicSkillCanceled(getObjectId()));
			getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			if(isPlayer() && message)
                sendPacket(SystemMsg.YOUR_CASTING_HAS_BEEN_INTERRUPTED);
		}
	}

	private final boolean canAbortCast()
	{
		return _castInterruptTime > System.currentTimeMillis();
	}

	private double reflectDamage(Creature attacker, Skill skill, double damage)
	{
		if(isDead() || damage <= 0.0 || !attacker.checkRange(attacker, this) || getCurrentHp() + getCurrentCp() <= damage)
			return 0.0;
		boolean bow = attacker.getBaseStats().getAttackType() == WeaponTemplate.WeaponType.BOW;
		double resistReflect = 1.0 - attacker.calcStat(Stats.RESIST_REFLECT_DAM, 0.0, null, null) * 0.01;
		double value = 0.0;
		double chanceValue = 0.0;
		if(skill != null)
		{
			if(skill.isMagic())
			{
				chanceValue = calcStat(Stats.REFLECT_AND_BLOCK_MSKILL_DAMAGE_CHANCE, 0.0, attacker, skill);
				value = calcStat(Stats.REFLECT_MSKILL_DAMAGE_PERCENT, 0.0, attacker, skill);
			}
			else if(skill.isPhysic())
			{
				chanceValue = calcStat(Stats.REFLECT_AND_BLOCK_PSKILL_DAMAGE_CHANCE, 0.0, attacker, skill);
				value = calcStat(Stats.REFLECT_PSKILL_DAMAGE_PERCENT, 0.0, attacker, skill);
			}
		}
		else
		{
			chanceValue = calcStat(Stats.REFLECT_AND_BLOCK_DAMAGE_CHANCE, 0.0, attacker, null);
			if(bow)
				value = calcStat(Stats.REFLECT_BOW_DAMAGE_PERCENT, 0.0, attacker, null);
			else
				value = calcStat(Stats.REFLECT_DAMAGE_PERCENT, 0.0, attacker, null);
		}
		if(chanceValue > 0.0 && Rnd.chance(chanceValue))
			chanceValue = damage;
		else
			chanceValue = 0.0;
		if(value > 0.0 || chanceValue > 0.0)
		{
			value = (value / 100.0 * damage + chanceValue) * resistReflect;
			if(Config.REFLECT_DAMAGE_CAPPED_BY_PDEF)
			{
				int xPDef = attacker.getPDef(this);
				if(xPDef > 0)
					value = Math.min(value, xPDef);
			}
			return value;
		}
		return 0.0;
	}

	private void absorbDamage(Creature target, Skill skill, double damage)
	{
		if(target.isDead())
			return;
		if(damage <= 0.0)
			return;
		boolean bow = getBaseStats().getAttackType() == WeaponTemplate.WeaponType.BOW;
		double absorb = 0.0;
		if(skill != null)
		{
			if(skill.isMagic())
				absorb = calcStat(Stats.ABSORB_MSKILL_DAMAGE_PERCENT, 0.0, this, skill);
			else
				absorb = calcStat(Stats.ABSORB_PSKILL_DAMAGE_PERCENT, 0.0, this, skill);
		}
		else if(skill == null && !bow)
			absorb = calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0.0, this, null);
		else if(skill == null && bow)
			absorb = calcStat(Stats.ABSORB_BOW_DAMAGE_PERCENT, 0.0, this, null);
		double poleMod = POLE_VAMPIRIC_MOD[Math.max(0, Math.min(_poleAttackCount, POLE_VAMPIRIC_MOD.length - 1))];
		absorb *= poleMod;
		boolean damageBlocked = target.isDamageBlocked(this, null);
		if(absorb > 0.0 && !damageBlocked && Rnd.chance(Config.ALT_VAMPIRIC_CHANCE) && !target.isServitor() && !target.isInvul())
		{
			double limit = calcStat(Stats.HP_LIMIT, null, null) * getMaxHp() / 100.0;
			if(getCurrentHp() < limit)
                setCurrentHp(Math.min(_currentHp + damage * absorb / 100.0, limit), false);
		}
		absorb = poleMod * calcStat(Stats.ABSORB_DAMAGEMP_PERCENT, 0.0, target, null);
		if(absorb > 0.0 && !damageBlocked && Rnd.chance(Config.ALT_VAMPIRIC_MP_CHANCE) && !target.isServitor() && !target.isInvul())
		{
			double limit = calcStat(Stats.MP_LIMIT, null, null) * getMaxMp() / 100.0;
			if(getCurrentMp() < limit)
                setCurrentMp(Math.min(_currentMp + damage * absorb / 100.0, limit));
		}
	}

	public double absorbToEffector(Creature attacker, double damage)
	{
		if(damage == 0.0)
			return 0.0;
		double transferToEffectorDam = calcStat(Stats.TRANSFER_TO_EFFECTOR_DAMAGE_PERCENT, 0.0);
		if(transferToEffectorDam > 0.0)
		{
			Collection<Abnormal> effects = getAbnormalList().getEffects();
			if(effects.isEmpty())
				return damage;
			for(Abnormal effect : effects)
			{
				if(effect.getEffectType() != EffectType.AbsorbDamageToEffector)
					continue;
				Creature effector = effect.getEffector();
				if(effector == this || effector.isDead() || !isInRange(effector, 1200L))
					return damage;
				Player thisPlayer = getPlayer();
				Player effectorPlayer = effector.getPlayer();
				if(thisPlayer == null || effectorPlayer == null)
					return damage;
				if(thisPlayer != effectorPlayer && (!thisPlayer.isOnline() || !thisPlayer.isInParty() || thisPlayer.getParty() != effectorPlayer.getParty()))
					return damage;
				double transferDamage = damage * transferToEffectorDam * 0.01;
				damage -= transferDamage;
				effector.reduceCurrentHp(transferDamage, effector, null, false, false, !attacker.isPlayable(), false, true, false, true);
			}
		}
		return damage;
	}

	private double reduceDamageByMp(Creature attacker, double damage)
	{
		if(damage == 0.0)
			return 0.0;
		double mpDam = damage * calcStat(Stats.TRANSFER_TO_MP_DAMAGE_PERCENT, 0.0) / 100.0;
		if(mpDam > 0.0)
		{
			if(mpDam < getCurrentMp())
			{
				reduceCurrentMp(mpDam, null);
                sendPacket(new SystemMessagePacket(SystemMsg.ARCANE_SHIELD_DECREASED_YOUR_MP_BY_S1_INSTEAD_OF_HP).addNumber((int) mpDam));
				return 0.0;
			}
			damage = mpDam - getCurrentMp();
            sendPacket(SystemMsg.MP_BECAME_0_AND_THE_ARCANE_SHIELD_IS_DISAPPEARING);
            setCurrentMp(0.0);
			getAbnormalList().stopEffects(EffectType.AbsorbDamageToMp);
		}
		return damage;
	}

	public Servitor getServitorForTransfereDamage(double damage)
	{
		return null;
	}

	public double getDamageForTransferToServitor(double damage)
	{
		return 0.0;
	}

	public SkillEntry addSkill(SkillEntry newSkillEntry)
	{
		if(newSkillEntry == null)
			return null;
		SkillEntry oldSkillEntry = _skills.get(newSkillEntry.getId());
		if(Objects.equals(newSkillEntry, oldSkillEntry))
			return oldSkillEntry;
		_skills.put(newSkillEntry.getId(), newSkillEntry);
		Skill newSkill = newSkillEntry.getTemplate();
		if(oldSkillEntry != null)
		{
			Skill oldSkill = oldSkillEntry.getTemplate();
			if(oldSkill.isToggle() && oldSkill.getLevel() > newSkill.getLevel())
				getAbnormalList().stopEffects(oldSkill);
			removeStatsOwner(oldSkill);
			removeTriggers(oldSkill);
			if(isPlayer())
				oldSkill.removeSkill(getPlayer());
		}
		addTriggers(newSkill);
		addStatFuncs(newSkill.getStatFuncs());
		if(isPlayer())
			newSkill.addSkill(getPlayer());
		return oldSkillEntry;
	}

	public Calculator[] getCalculators()
	{
		return _calculators;
	}

	public final void addStatFunc(Func f)
	{
		if(f == null)
			return;
		int stat = f.stat.ordinal();
		synchronized (_calculators)
		{
			if(_calculators[stat] == null)
				_calculators[stat] = new Calculator(f.stat, this);
			_calculators[stat].addFunc(f);
		}
	}

	public final void addStatFuncs(Func[] funcs)
	{
		for(Func f : funcs)
			addStatFunc(f);
	}

	public final void removeStatFunc(Func f)
	{
		if(f == null)
			return;
		int stat = f.stat.ordinal();
		synchronized (_calculators)
		{
			if(_calculators[stat] != null)
				_calculators[stat].removeFunc(f);
		}
	}

	public final void removeStatFuncs(Func[] funcs)
	{
		for(Func f : funcs)
			removeStatFunc(f);
	}

	public final boolean removeStatsOwner(Object owner)
	{
		boolean result = false;

		synchronized (_calculators) {
			for (Calculator calculator : _calculators)
				if (calculator != null)
					if (calculator.removeOwner(owner))
						result = true;
		}

		return result;
	}

	public void altOnMagicUse(Creature aimingTarget, Skill skill) {
		if (isAlikeDead() || skill == null)
			return;
		int magicId = skill.getDisplayId();
		int level = Math.max(1, getSkillDisplayLevel(skill.getId()));
		List<Creature> targets = skill.getTargets(this, aimingTarget, true);
		if (!skill.isNotBroadcastable()) {
			int[] objectIds = Util.objectToIntArray(targets);
			broadcastPacket(new MagicSkillLaunchedPacket(getObjectId(), magicId, level, objectIds));
		}
		double mpConsume2 = skill.getMpConsume2();
		if (mpConsume2 > 0.0) {
			double mpConsume2WithStats;
			if (skill.isMagic())
				mpConsume2WithStats = calcStat(Stats.MP_MAGIC_SKILL_CONSUME, mpConsume2, aimingTarget, skill);
			else
				mpConsume2WithStats = calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsume2, aimingTarget, skill);
			if (_currentMp < mpConsume2WithStats) {
				sendPacket(SystemMsg.NOT_ENOUGH_MP);
				return;
			}
			reduceCurrentMp(mpConsume2WithStats, null);
		}
		callSkill(skill, targets, false, false);
	}

	public final void forceUseSkill(Skill skill, Creature target)
	{
		if (skill == null)
			return;
		if (target == null) {
			target = skill.getAimingTarget(this, getTarget());
			if (target == null)
				return;
		}
		List<Creature> targets = skill.getTargets(this, target, true);
		int displaySkillLevel = Math.max(1, getSkillDisplayLevel(skill.getId()));
		if (!skill.isNotBroadcastable()) {
			broadcastPacket(new MagicSkillUse(this, target, skill.getDisplayId(), displaySkillLevel, 0, 0L));
			int[] objectIds = Util.objectToIntArray(targets);
			broadcastPacket(new MagicSkillLaunchedPacket(getObjectId(), skill.getDisplayId(), displaySkillLevel, objectIds));
		}
		callSkill(skill, targets, false, false);
	}

	public void altUseSkill(Skill skill, Creature target) {
		if (skill == null)
			return;
		if (isUnActiveSkill(skill.getId()))
			return;
		if (isSkillDisabled(skill))
			return;
		if (target == null) {
			target = skill.getAimingTarget(this, getTarget());
			if (target == null)
				return;
		}

		getListeners().onMagicUse(skill, target, true);
		if (!isPhantom()) // отключим фантомам потребление итемов
		if (!hasClubCard() || !ArrayUtils.contains(Constants.CLUB_CARD_NO_CONSUME_SKILL_IDS, skill.getId())) 
		{
			if (!skill.isHandler() && isPlayable() && skill.getItemConsumeId() > 0 && skill.getItemConsume() > 0L)
				if (skill.isItemConsumeFromMaster()) 
				{
					Player master = getPlayer();
					if (master == null || !master.consumeItem(skill.getItemConsumeId(), skill.getItemConsume(), false))
						return;
				} else if (!consumeItem(skill.getItemConsumeId(), skill.getItemConsume(), false))
					return;
		}
		if (skill.getReferenceItemId() > 0 && !consumeItemMp(skill.getReferenceItemId(), skill.getReferenceItemMpConsume()))
			return;
		if (skill.getSoulsConsume() > getConsumedSouls())
			return;
		if (skill.getSoulsConsume() > 0)
			setConsumedSouls(getConsumedSouls() - skill.getSoulsConsume(), null);
		int level = Math.max(1, getSkillDisplayLevel(skill.getId()));
		long reuseDelay = Formulas.calcSkillReuseDelay(this, skill);
		if (!skill.isToggle() && !skill.isNotBroadcastable()) {
			MagicSkillUse msu = new MagicSkillUse(this, target, skill.getDisplayId(), level, skill.getHitTime(), reuseDelay);
			msu.setReuseSkillId(skill.getReuseSkillId());
			broadcastPacket(msu);
		}
		disableSkill(skill, reuseDelay);
		altOnMagicUse(target, skill);
	}

	public void sendReuseMessage(Skill skill) {
	}

	public void broadcastPacket(L2GameServerPacket... packets) {
		sendPacket(packets);
		broadcastPacketToOthers(packets);
	}

	public void broadcastPacket(List<L2GameServerPacket> packets) {
		sendPacket(packets);
		broadcastPacketToOthers(packets);
	}

	public void broadcastPacketToOthers(L2GameServerPacket... packets) {
		if (!isVisible() || packets.length == 0)
			return;
		for (Player target : World.getAroundObservers(this)) {
			if (target.isInObserverMode()) {
				target.sendPacket(packets);
				continue;
			}
			for (IBroadcastPacket packet : packets) {
				if (packet.isInPacketRange(this, target))
					target.sendPacket(packet);
			}
		}
	}

	public void broadcastPacketToOthers(List<L2GameServerPacket> packets) {
		if (!isVisible() || packets.size() == 0)
			return;
		for (Player target : World.getAroundObservers(this)) {
			if (target.isInObserverMode()) {
				target.sendPacket(packets);
				continue;
			}
			for (IBroadcastPacket packet : packets) {
				if (packet.isInPacketRange(this, target))
					target.sendPacket(packet);
			}
		}
	}

	public StatusUpdatePacket makeStatusUpdate(int... fields) {
		StatusUpdatePacket su = new StatusUpdatePacket(getObjectId());
		for (int field : fields)
			switch (field) {
				case 9: {
					su.addAttribute(field, (int) getCurrentHp());
					break;
				}
				case 10: {
					su.addAttribute(field, getMaxHp());
					break;
				}
				case 11: {
					su.addAttribute(field, (int) getCurrentMp());
					break;
				}
				case 12:
				{
					su.addAttribute(field, getMaxMp());
					break;
				}
				case 27:
				{
					su.addAttribute(field, getKarma());
					break;
				}
				case 33:
				{
					su.addAttribute(field, (int) getCurrentCp());
					break;
				}
				case 34:
				{
					su.addAttribute(field, getMaxCp());
					break;
				}
				case 26:
				{
					su.addAttribute(field, getPvpFlag());
					break;
				}
			}
		return su;
	}

	public void broadcastStatusUpdate()
	{
		if(!needStatusUpdate())
			return;
		StatusUpdatePacket su = makeStatusUpdate(10, 12, 9, 11);
        broadcastPacket(su);
	}

	public int calcHeading(int x_dest, int y_dest)
	{
		return (int) (Math.atan2(getY() - y_dest, getX() - x_dest) * 10430.378350470453) + 32768;
	}

	public final double calcStat(Stats stat)
	{
		return calcStat(stat, null, null);
	}

	public final double calcStat(Stats stat, double init)
	{
		return calcStat(stat, init, null, null);
	}

	public final double calcStat(Stats stat, double init, Creature target, Skill skill)
	{
		int id = stat.ordinal();
		Calculator c = _calculators[id];
		if(c == null)
			return init;

		return c.calc(this, target, skill, init);
	}

	public final double calcStat(Stats stat, Creature target, Skill skill)
	{
		int id = stat.ordinal();
		double value = stat.getInit();
		Calculator c = _calculators[id];
		if(c != null)
			return c.calc(this, target, skill, value);
		return value;
	}

	public int calculateAttackDelay()
	{
		return Formulas.calcPAtkSpd(getPAtkSpd());
	}

	public void callSkill(Skill skill, List<Creature> targets, boolean useActionSkills, boolean trigger)
	{
		try
		{
			Creature castingTarget = getCastingTarget();
			if(useActionSkills && _triggers != null)
			{
				if(skill.isOffensive())
				{
					if(skill.isMagic())
                        useTriggers(castingTarget, TriggerType.OFFENSIVE_MAGICAL_SKILL_USE, null, skill, 0.0);
					else if(skill.isPhysic())
                        useTriggers(castingTarget, TriggerType.OFFENSIVE_PHYSICAL_SKILL_USE, null, skill, 0.0);
				}
				else if(skill.isMagic())
                    useTriggers(castingTarget, TriggerType.SUPPORT_MAGICAL_SKILL_USE, null, skill, 0.0);
                useTriggers(this, TriggerType.ON_CAST_SKILL, null, skill, 0.0);
			}
			Player player = getPlayer();
			for(Creature target : targets)
			{
				if(target == null)
					continue;
				target.getListeners().onMagicHit(skill, this);
				if(player == null || !target.isNpc())
					continue;
				NpcInstance npc = (NpcInstance) target;
				List<QuestState> ql = player.getQuestsForEvent(npc, QuestEventType.MOB_TARGETED_BY_SKILL);
				if(ql == null)
					continue;
				for(QuestState qs : ql)
					qs.getQuest().notifySkillUse(npc, skill, qs);
			}
            useTriggers(castingTarget, TriggerType.ON_END_CAST, null, skill, 0.0);
			skill.onEndCast(this, targets);
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
	}

	public void useTriggers(GameObject target, TriggerType type, Skill ex, Skill owner, double damage)
	{
        useTriggers(target, null, type, ex, owner, owner, damage);
	}

	public void useTriggers(GameObject target, List<Creature> targets, TriggerType type, Skill ex, Skill owner, double damage)
	{
        useTriggers(target, targets, type, ex, owner, owner, damage);
	}

	public void useTriggers(GameObject target, TriggerType type, Skill ex, Skill owner, StatTemplate triggersOwner, double damage)
	{
        useTriggers(target, null, type, ex, owner, triggersOwner, damage);
	}

	public void useTriggers(GameObject target, List<Creature> targets, TriggerType type, Skill ex, Skill owner, StatTemplate triggersOwner, double damage)
	{
		Set<TriggerInfo> triggers = null;
		switch(type)
		{
			case ON_START_CAST:
			case ON_TICK_CAST:
			case ON_END_CAST:
			case ON_FINISH_CAST:
			case ON_START_EFFECT:
			case ON_EXIT_EFFECT:
			case ON_FINISH_EFFECT:
			case ON_REVIVE:
			{
				if(triggersOwner != null)
				{
					triggers = new CopyOnWriteArraySet<>();
					for(TriggerInfo t : triggersOwner.getTriggerList())
						if(t.getType() == type)
							triggers.add(t);
					break;
				}
				break;
			}
			case ON_CAST_SKILL:
			{
				if(_triggers.get(type) != null)
				{
					triggers = new CopyOnWriteArraySet<>();
					for(TriggerInfo t : _triggers.get(type))
					{
						int skillID = t.getArgs() == null || t.getArgs().isEmpty() ? -1 : Integer.parseInt(t.getArgs());
						if(skillID == -1 || skillID == owner.getId())
							triggers.add(t);
					}
					break;
				}
				break;
			}
			default:
			{
				if(_triggers != null)
				{
					triggers = _triggers.get(type);
					break;
				}
				break;
			}
		}
		if(triggers != null && !triggers.isEmpty())
			for(TriggerInfo t : triggers)
			{
				SkillEntry skillEntry = t.getSkill();
				if(skillEntry != null && !skillEntry.equals(ex))
					useTriggerSkill(target == null ? getTarget() : target, targets, t, owner, damage);
			}
	}

	public void useTriggerSkill(GameObject target, List<Creature> targets, TriggerInfo trigger, Skill owner, double damage)
	{
		SkillEntry skillEntry = trigger.getSkill();
		if(skillEntry == null)
			return;
		Skill skill = skillEntry.getTemplate();
		if(skill == null)
			return;
		Creature aimTarget = skill.getAimingTarget(this, target);
		if(aimTarget != null && trigger.isIncreasing())
		{
			int increasedTriggerLvl = 0;
			for(Abnormal effect : aimTarget.getAbnormalList().getEffects())
			{
				if(effect.getSkill().getId() != skill.getId())
					continue;
				increasedTriggerLvl = effect.getSkill().getLevel();
				break;
			}
			if(increasedTriggerLvl == 0)
				for(Servitor servitor : getServitors())
					for(Abnormal effect2 : servitor.getAbnormalList().getEffects())
					{
						if(effect2.getSkill().getId() != skill.getId())
							continue;
						increasedTriggerLvl = effect2.getSkill().getLevel();
						break;
					}
			if(increasedTriggerLvl > 0)
			{
				Skill newSkill = SkillHolder.getInstance().getSkill(skill.getId(), increasedTriggerLvl + 1);
				if(newSkill != null)
					skill = newSkill;
				else
					skill = SkillHolder.getInstance().getSkill(skill.getId(), increasedTriggerLvl);
			}
		}
		if(skill.getReuseDelay() > 0 && isSkillDisabled(skill))
			return;
		if(!Rnd.chance(trigger.getChance()))
			return;
		Creature realTarget = target != null && target.isCreature() ? (Creature) target : null;
		if(trigger.checkCondition(this, realTarget, aimTarget, owner, damage) && skill.checkCondition(this, aimTarget, true, true, true, false, trigger.getType()))
		{
			if(targets == null)
				targets = skill.getTargets(this, aimTarget, false);
			if(!skill.isNotBroadcastable() && !isCastingNow() && trigger.getType() != TriggerType.IDLE)
				for(Creature cha : targets)
                    broadcastPacket(new MagicSkillUse(this, cha, skill.getDisplayId(), skill.getDisplayLevel(), 0, 0L));
			callSkill(skill, targets, false, true);
			disableSkill(skill, skill.getReuseDelay());
		}
	}

	protected void triggerCancelEffects(TriggerInfo trigger)
	{
		SkillEntry skillEntry = trigger.getSkill();
		if(skillEntry == null)
			return;
		getAbnormalList().stopEffects(skillEntry.getTemplate());
	}

	public boolean checkReflectSkill(Creature attacker, Skill skill)
	{
		if(this == attacker)
			return false;
		if(isDead() || attacker.isDead())
			return false;
		if(!skill.isReflectable())
			return false;
		if(isInvul() || attacker.isInvul() || !skill.isOffensive())
			return false;
		if(skill.isMagic() && skill.getSkillType() != Skill.SkillType.MDAM)
			return false;
		if(Rnd.chance(calcStat(skill.isMagic() ? Stats.REFLECT_MAGIC_SKILL : Stats.REFLECT_PHYSIC_SKILL, 0.0, attacker, skill)))
		{
            sendPacket(new SystemMessage(1998).addName(attacker));
			attacker.sendPacket(new SystemMessage(1999).addName(this));
			return true;
		}
		return false;
	}

	public boolean checkReflectDebuff(Creature effector, Skill skill)
	{
		return this != effector && !isDead() && !effector.isDead() && !effector.isTrap() && skill.isReflectable() && !isInvul() && !effector.isInvul() && skill.isOffensive() && !isDebuffImmune() && Rnd.chance(calcStat(skill.isMagic() ? Stats.REFLECT_MAGIC_DEBUFF : Stats.REFLECT_PHYSIC_DEBUFF, 0.0, effector, skill));
	}

	public void doCounterAttack(Skill skill, Creature attacker, boolean blow)
	{
		if(isDead())
			return;
		if(isDamageBlocked(attacker, null) || attacker.isDamageBlocked(this, null))
			return;
		if(skill == null || skill.hasEffects(EffectUseType.NORMAL) || skill.isMagic() || !skill.isOffensive() || skill.getCastRange() > 200)
			return;
		if(Rnd.chance(calcStat(Stats.COUNTER_ATTACK, 0.0, attacker, skill)))
		{
			double damage = 1189 * getPAtk(attacker) / Math.max(attacker.getPDef(this), 1);
			attacker.sendPacket(new SystemMessagePacket(SystemMsg.C1_IS_PERFORMING_A_COUNTERATTACK).addName(this));
			if(blow)
			{
                sendPacket(new SystemMessagePacket(SystemMsg.C1_IS_PERFORMING_A_COUNTERATTACK).addName(this));
                sendPacket(new SystemMessagePacket(SystemMsg.C1_HAS_DONE_S3_POINTS_OF_DAMAGE_TO_C2).addName(this).addName(attacker).addNumber((int) damage).addHpChange(getObjectId(), attacker.getObjectId(), (int) -damage));
				attacker.reduceCurrentHp(damage, this, skill, true, true, false, false, false, false, true);
			}
			else
                sendPacket(new SystemMessagePacket(SystemMsg.C1_IS_PERFORMING_A_COUNTERATTACK).addName(this));
            sendPacket(new SystemMessagePacket(SystemMsg.C1_HAS_DONE_S3_POINTS_OF_DAMAGE_TO_C2).addName(this).addName(attacker).addNumber((int) damage).addHpChange(getObjectId(), attacker.getObjectId(), (int) -damage));
			attacker.reduceCurrentHp(damage, this, skill, true, true, false, false, false, false, true);
		}
	}

	public void disableSkill(Skill skill, long delay)
	{
		disableSkill(skill, delay, false);
	}

	public void disableSkill(Skill skill, long delay, boolean addedOnEquip)
	{
		TimeStamp v = new TimeStamp(skill, delay, addedOnEquip);
		v.setGlobalReuse(skill.isGlobalReuse());
		_skillReuses.put(skill.getReuseHash(), v);
	}

	public abstract boolean isAutoAttackable(Creature p0);

	public void doAttack(Creature target)
	{
		if(target == null || isAMuted() || isAttackingNow() || isAlikeDead() || target.isDead() || !isInRange(target, 2000L))
			return;
		getListeners().onAttack(target);
		int sAtk = calculateAttackDelay();
		int ssGrade = 0;
		int attackReuseDelay = 0;
		boolean ssEnabled = false;
		if(isNpc())
		{
			attackReuseDelay = ((NpcTemplate) getTemplate()).getBaseReuseDelay();
			NpcTemplate.ShotsType shotType = ((NpcTemplate) getTemplate()).getShots();
			if(shotType != NpcTemplate.ShotsType.NONE && shotType != NpcTemplate.ShotsType.BSPIRIT && shotType != NpcTemplate.ShotsType.SPIRIT)
				ssEnabled = true;
		}
		else
		{
			WeaponTemplate weaponItem = getActiveWeaponTemplate();
			if(weaponItem != null)
			{
				attackReuseDelay = weaponItem.getAttackReuseDelay();
				ssGrade = weaponItem.getGrade().extOrdinal();
			}
			ssEnabled = getChargedSoulshotPower() > 0.0;
		}
		if(attackReuseDelay > 0)
		{
			int reuse = (int) (1500 * getReuseModifier(target) * 666.0 * getBaseStats().getPAtkSpd() / 293.0 / getPAtkSpd());

			if(reuse > 0)
			{
                sendPacket(new SetupGaugePacket(this, SetupGaugePacket.Colors.RED, reuse));
				_attackReuseEndTime = reuse + System.currentTimeMillis() - 75L;
				if(reuse > sAtk)
					ThreadPoolManager.getInstance().schedule(new GameObjectTasks.NotifyAITask(this, CtrlEvent.EVT_READY_TO_ACT), reuse);
			}
		}
		_attackEndTime = sAtk + System.currentTimeMillis() - 50L;
		_isAttackAborted = false;
		AttackPacket attack = new AttackPacket(this, target, ssEnabled, ssGrade);
        setHeading(PositionUtils.calculateHeadingFrom(this, target));
		switch(getBaseStats().getAttackType())
		{
			case BOW:
			{
				doAttackHitByBow(attack, target, sAtk);
				break;
			}
			case POLE:
			{
				doAttackHitByPole(attack, target, sAtk);
				break;
			}
			case DUAL:
			case DUALFIST:
			case DUALDAGGER:
			case DUALBLUNT:
			{
				doAttackHitByDual(attack, target, sAtk);
				break;
			}
			default: {
				doAttackHitSimple(attack, target, sAtk);
				break;
			}
		}
		if (attack.hasHits())
			broadcastPacket(attack);
	}

	private void doAttackHitSimple(AttackPacket attack, Creature target, int sAtk) {
		int attackcountmax = (int) Math.round(calcStat(Stats.ATTACK_TARGETS_COUNT, 0.0, target, null));
		if (attackcountmax > 0 && !isInPeaceZone()) {
			int angle = 90;
			int range = getPhysicalAttackRange() + 40;
			int attackedCount = 1;
			for (Creature t : getAroundCharacters(range, 200)) {
				if (attackedCount > attackcountmax)
					break;

				if (t == target || t.isDead() || !PositionUtils.isFacing(this, t, angle) || !t.isAutoAttackable(this) || (getPvpFlag() != 0 || t.getPvpFlag() != 0) && getPvpFlag() == 0)
					continue;

				doAttackHitSimple0(attack, t, 1.0, false, sAtk, false);
				++attackedCount;
			}
		}
		doAttackHitSimple0(attack, target, 1.0, true, sAtk, true);
	}

	private void doAttackHitSimple0(AttackPacket attack, Creature target, double multiplier, boolean unchargeSS, int sAtk, boolean notify)
	{
		int damage1 = 0;
		boolean shld1 = false;
		boolean crit1 = false;
		boolean miss1 = Formulas.calcHitMiss(this, target);
		if(!miss1)
		{
			Formulas.AttackInfo info = Formulas.calcPhysDam(this, target, null, false, false, attack._soulshot, false);
			if(info != null)
			{
				damage1 = (int) (info.damage * multiplier);
				shld1 = info.shld;
				crit1 = info.crit;
				miss1 = info.miss;
			}
		}
		ThreadPoolManager.getInstance().schedule(new GameObjectTasks.HitTask(this, target, damage1, crit1, miss1, attack._soulshot, shld1, unchargeSS, notify, sAtk), sAtk / 2);
		attack.addHit(target, damage1, miss1, crit1, shld1);
	}

	private void doAttackHitByBow(AttackPacket attack, Creature target, int sAtk)
	{
		boolean miss1 = Formulas.calcHitMiss(this, target);
		reduceArrowCount();
		boolean crit1 = false;
		boolean shld1 = false;
		int damage1 = 0;
		if(!miss1)
		{
			Formulas.AttackInfo info = Formulas.calcPhysDam(this, target, null, false, false, attack._soulshot, false);
			if(info != null)
			{
				damage1 = (int) info.damage;
				shld1 = info.shld;
				crit1 = info.crit;
				miss1 = info.miss;
			}
		}
		ThreadPoolManager.getInstance().schedule(new GameObjectTasks.HitTask(this, target, damage1, crit1, miss1, attack._soulshot, shld1, true, true, sAtk), sAtk / 2);
		attack.addHit(target, damage1, miss1, crit1, shld1);
	}

	private void doAttackHitByDual(AttackPacket attack, Creature target, int sAtk)
	{
		int damage1 = 0;
		boolean shld1 = false;
		boolean crit1 = false;
		boolean miss1 = Formulas.calcHitMiss(this, target);
		boolean miss2 = Formulas.calcHitMiss(this, target);
		if(!miss1)
		{
			Formulas.AttackInfo info = Formulas.calcPhysDam(this, target, null, true, false, attack._soulshot, false);
			if(info != null)
			{
				damage1 = (int) info.damage;
				shld1 = info.shld;
				crit1 = info.crit;
				miss1 = info.miss;
			}
		}
		boolean crit2 = false;
		boolean shld2 = false;
		int damage2 = 0;
		if(!miss2)
		{
			Formulas.AttackInfo info = Formulas.calcPhysDam(this, target, null, true, false, attack._soulshot, false);
			if(info != null)
			{
				damage2 = (int) info.damage;
				shld2 = info.shld;
				crit2 = info.crit;
				miss2 = info.miss;
			}
		}
		ThreadPoolManager.getInstance().schedule(new GameObjectTasks.HitTask(this, target, damage1, crit1, miss1, attack._soulshot, shld1, true, false, sAtk / 2), sAtk / 4);
		ThreadPoolManager.getInstance().schedule(new GameObjectTasks.HitTask(this, target, damage2, crit2, miss2, attack._soulshot, shld2, false, true, sAtk), sAtk / 2);
		attack.addHit(target, damage1, miss1, crit1, shld1);
		attack.addHit(target, damage2, miss2, crit2, shld2);
	}

	private void doAttackHitByPole(AttackPacket attack, Creature target, int sAtk) {
		int angle = (int) calcStat(Stats.POLE_ATTACK_ANGLE, 120.0, target, null);
		int range = getPhysicalAttackRange();
		int attackcountmax = (int) Math.round(calcStat(Stats.POLE_TARGET_COUNT, 0.0, target, null));
		if (isBoss())
			attackcountmax += 27;
		else if (isRaid())
			attackcountmax += 12;
		else if (isMonster() && getLevel() > 0)
			attackcountmax += (int) (getLevel() / 7.5);
		_poleAttackCount = 1;
		double mult = 1.0;
		if (!isInPeaceZone())
			for (Creature t : getAroundCharacters(range, 200)) {
				if (_poleAttackCount > attackcountmax)
					break;
				if (t == target || t.isDead())
					continue;
				if (!PositionUtils.isFacing(this, t, angle))
					continue;
				if (!t.isAutoAttackable(this) || (getPvpFlag() != 0 || t.getPvpFlag() != 0) && getPvpFlag() == 0)
					continue;
				doAttackHitSimple0(attack, t, mult, false, sAtk, false);
				mult *= Config.ALT_POLE_DAMAGE_MODIFIER;
				++_poleAttackCount;
			}
		_poleAttackCount = 0;
		doAttackHitSimple0(attack, target, 1.0, true, sAtk, true);
	}

	public long getAnimationEndTime()
	{
		return _animationEndTime;
	}

	public void doCast(SkillEntry skillEntry, Creature target, boolean forceUse)
	{
		if(skillEntry == null)
			return;
		Skill skill = skillEntry.getTemplate();
		if(skill.getReferenceItemId() > 0 && !consumeItemMp(skill.getReferenceItemId(), skill.getReferenceItemMpConsume()))
			return;
		if(target == null)
			target = skill.getAimingTarget(this, getTarget());
		if(target == null)
			return;
		//TODO: FIXME!!
		if (!this.isPhantom())// отключим фантомам потребление итемов
		if(skill.isHandler() && skill.getCastRange() > 0 && this != target) 
		{
			if(skill.getItemConsume() > 0 && !consumeItem(skill.getItemConsumeId(), skill.getItemConsume(), false)) {
				sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
				return;
			}
		}
		getListeners().onMagicUse(skill, target, false);
		Location groundLoc = null;
		if(skill.getTargetType() == Skill.SkillTargetType.TARGET_GROUND)
		{
			if(isPlayer())
			{
				groundLoc = getPlayer().getGroundSkillLoc();
				if(groundLoc != null)
                    setHeading(PositionUtils.calculateHeadingFrom(getX(), getY(), groundLoc.getX(), groundLoc.getY()), true);
			}
		}
		else if(this != target)
            setHeading(PositionUtils.calculateHeadingFrom(this, target));
		int magicId = skill.getId();
		int level = Math.max(1, getSkillDisplayLevel(magicId));
		int skillTime = skill.isSkillTimePermanent() ? skill.getHitTime() : Formulas.calcSkillCastSpd(this, skill, skill.getHitTime());
		int skillInterruptTime = skill.isSkillTimePermanent() ? skill.getSkillInterruptTime() : Formulas.calcSkillCastSpd(this, skill, skill.getSkillInterruptTime());
		_animationEndTime = System.currentTimeMillis() + skillTime;
		if(skill.isMagic() && !skill.isSkillTimePermanent() && getChargedSpiritshotPower() > 0.0)
		{
			skillTime = (int) (skillTime * 0.7);
			skillInterruptTime = (int) (skillInterruptTime * 0.7);
		}
		int minCastTimePhysical = Math.min(Config.SKILLS_CAST_TIME_MIN_PHYSICAL, skill.getHitTime());
		int minCastTimeMagical = Math.min(Config.SKILLS_CAST_TIME_MIN_MAGICAL, skill.getHitTime());
		if(!skill.isSkillTimePermanent())
			if(skill.isMagic() && skillTime < minCastTimeMagical)
			{
				skillTime = minCastTimeMagical;
				skillInterruptTime = 0;
			}
			else if(!skill.isMagic() && skillTime < minCastTimePhysical)
			{
				skillTime = minCastTimePhysical;
				skillInterruptTime = 0;
			}
		boolean criticalBlow = skill.calcCriticalBlow(this, target);
		long reuseDelay = Math.max(0L, Formulas.calcSkillReuseDelay(this, skill));
		if(!skill.isNotBroadcastable())
		{
			MagicSkillUse msu = new MagicSkillUse(this, target, skill.getDisplayId(), level, skillTime, reuseDelay);
			msu.setReuseSkillId(skill.getReuseSkillId());
			msu.setGroundLoc(groundLoc);
			msu.setCriticalBlow(criticalBlow);
			if(isServitor())
			{
				Servitor.UsedSkill servitorUsedSkill = ((Servitor) this).getUsedSkill();
				if(servitorUsedSkill != null && servitorUsedSkill.getSkill() == skill)
				{
					msu.setServitorSkillInfo(servitorUsedSkill.getActionId());
					((Servitor) this).setUsedSkill(null);
				}
			}
            broadcastPacket(msu);
		}
		disableSkill(skill, reuseDelay);
		if(skill.getTargetType() == Skill.SkillTargetType.TARGET_HOLY)
			target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, this, 1);
		double mpConsume1 = skill.getMpConsume1();
		if(mpConsume1 > 0.0 && _currentMp < mpConsume1)
		{
            sendPacket(SystemMsg.NOT_ENOUGH_MP);
			onCastEndTime(null, false);
			return;
		}
		if(!hasClubCard() || !ArrayUtils.contains(Constants.CLUB_CARD_NO_CONSUME_SKILL_IDS, skill.getId())){
			if(!skill.isHandler() && isPlayable() && skill.getItemConsumeId() > 0 && skill.getItemConsume() > 0L)
				if(skill.isItemConsumeFromMaster()) {
					Player master = getPlayer();
					if(master == null)
						return;

					if(ItemFunctions.getItemCount(master, skill.getItemConsumeId()) < skill.getItemConsume()) {
						master.sendPacket(SystemMsg.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
						return;
					}
				} else if(ItemFunctions.getItemCount((Playable) this, skill.getItemConsumeId()) < skill.getItemConsume()) {
					sendPacket(SystemMsg.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
					return;
				}
		}
		if(isPlayer())
			if(skill.getSkillType() == Skill.SkillType.PET_SUMMON)
                sendPacket(SystemMsg.SUMMONING_YOUR_PET);
			else
                sendPacket(new SystemMessagePacket(SystemMsg.YOU_USE_S1).addSkillName(magicId, level));
		if(mpConsume1 > 0.0)
			reduceCurrentMp(mpConsume1, null);
		if(!hasClubCard() || !ArrayUtils.contains(Constants.CLUB_CARD_NO_CONSUME_SKILL_IDS, skill.getId())) {
			if(!skill.isHandler() && isPlayable() && skill.getItemConsumeId() > 0 && skill.getItemConsume() > 0L)
				if(skill.isItemConsumeFromMaster()) {
					Player master = getPlayer();
					if(master != null)
						master.consumeItem(skill.getItemConsumeId(), skill.getItemConsume(), true);
				} else
					consumeItem(skill.getItemConsumeId(), skill.getItemConsume(), true);
		}

		Location flyLoc = null;
		switch(skill.getFlyType())
		{
			case DUMMY:
				if(getFlyLocation(target, skill) == null)
				{
					sendPacket(SystemMsg.CANNOT_SEE_TARGET);
					return;
				}
				break;
			case CHARGE:
			{
				flyLoc = getFlyLocation(target, skill);
				if(flyLoc != null)
				{
					broadcastPacket(new FlyToLocationPacket(this, flyLoc, skill.getFlyType(), skill.getFlySpeed(), skill.getFlyDelay(), skill.getFlyAnimationSpeed()));
				}
				else
				{
					sendPacket(SystemMsg.CANNOT_SEE_TARGET);
					return;
				}
				break;
			}
			case WARP_BACK:
			case WARP_FORWARD:
			{
				flyLoc = getFlyLocation(this, skill);
				if(flyLoc != null)
				{
					broadcastPacket(new FlyToLocationPacket(this, flyLoc, skill.getFlyType(), skill.getFlyRadius() / skillTime * 1000, skill.getFlyDelay(), skill.getFlyAnimationSpeed()));
				}
				break;
			}
		}
		if(flyLoc != null)
			_flyLoc = flyLoc;

		_castingSkill = skill;
		_castInterruptTime = System.currentTimeMillis() + skillInterruptTime;
		if(criticalBlow)
			_isCriticalBlowCastingSkill = true;
		setCastingTarget(target);
		int tickInterval = skill.getTickInterval() > 0 ? skill.getTickInterval() : skillTime;
		_castInterval = skillTime;
		_skillLaunchedTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.MagicLaunchedTask(this, forceUse), skillInterruptTime);
		_skillTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.MagicUseTask(this, forceUse), tickInterval);
		if(skillTime > 0) {
			long doCast = tickInterval * 50 / 100;
			_doCast = true;
			_doCastTask = ThreadPoolManager.getInstance().schedule(new DoCastTask(this), doCast);
		}
		skill.onStartCast(skillEntry, this, skill.getTargets(this, target, forceUse));
        useTriggers(target, TriggerType.ON_START_CAST, null, skill, 0.0);
	}

	public Location getFlyLocation(GameObject target, Skill skill)
	{
		if(target != null && target != this)
		{
			int heading = target.getHeading();
			if(!skill.isFlyDependsOnHeading())
				heading = PositionUtils.calculateHeadingFrom(target, this);
			double radian = PositionUtils.convertHeadingToDegree(heading) + skill.getFlyPositionDegree();
			if(radian > 360.0)
				radian -= 360.0;
			radian = 3.141592653589793 * radian / 180.0;
			Location loc = new Location(target.getX() + (int) (Math.cos(radian) * 40.0), target.getY() + (int) (Math.sin(radian) * 40.0), target.getZ());
			if(isFlying())
			{
				if(isInFlyingTransform() && (loc.z <= 0 || loc.z >= 6000))
					return null;
				if(GeoEngine.moveCheckInAir(getX(), getY(), getZ(), loc.x, loc.y, loc.z, getColRadius(), getGeoIndex()) == null)
					return null;
			}
			else {
				loc.correctGeoZ();
				if (!GeoEngine.canMoveToCoord(getX(), getY(), getZ(), loc.x, loc.y, loc.z, getGeoIndex())) {
					loc = Location.findPointToStay(target, 30);
					if (!GeoEngine.canMoveToCoord(getX(), getY(), getZ(), loc.x, loc.y, loc.z, getGeoIndex())) {
						final List<Location> flyList = GeoEngine.MoveList(getX(), getY(), getZ(), loc.x, loc.y, getGeoIndex(), false);
						return flyList.isEmpty() ? null : flyList.get(flyList.size() - 1).geo2world();
					}
				}
			}
			return loc;
		}
		int x1 = 0;
		int y1 = 0;
		int z1 = 0;
		if(skill.getFlyType() == FlyToLocationPacket.FlyType.THROW_UP)
		{
			z1 = getZ() + skill.getFlyRadius();
		}
		else
		{
			double radian2 = PositionUtils.convertHeadingToRadian(getHeading());
			x1 = -(int) (Math.sin(radian2) * skill.getFlyRadius());
			y1 = (int) (Math.cos(radian2) * skill.getFlyRadius());
		}
		if(isFlying())
			return GeoEngine.moveCheckInAir(getX(), getY(), getZ(), getX() + x1, getY() + y1, getZ() + z1, getColRadius(), getGeoIndex());
		return GeoEngine.moveCheck(getX(), getY(), getZ(), getX() + x1, getY() + y1, getGeoIndex());
	}

	public final void doDie(Creature killer)
	{
		if(!isDead.compareAndSet(false, true))
			return;
		onDeath(killer);
	}

	protected void onDeath(Creature killer)
	{
		if(killer != null)
		{
			Player killerPlayer = killer.getPlayer();
			if(killerPlayer != null)
				killerPlayer.getListeners().onKillIgnorePetOrSummon(this);
			killer.getListeners().onKill(this);
			if(isPlayer() && killer.isPlayable())
				_currentCp = 0.0;
		}
		setTarget(null);
		abortCast(true, false);
		abortAttack(true, false);
        stopMove();
		stopAttackStanceTask();
		stopRegeneration();
		_currentHp = 0.0;
		if(isPlayable())
		{
			TIntSet effectsToRemove = new TIntHashSet();
			if(isBlessedByNoblesse() || isSalvation())
			{
				if(isSalvation() && isPlayer() && !getPlayer().isInOlympiadMode())
					getPlayer().reviveRequest(getPlayer(), 100.0, false);
				for(Abnormal e : getAbnormalList().getEffects())
				{
					int skillId = e.getSkill().getId();
					if(e.getEffectType() == EffectType.BlessNoblesse || e.getSkill().getId() == 2168)
						effectsToRemove.add(skillId);
					else
					{
						if(e.getEffectType() != EffectType.AgathionResurrect)
							continue;
						if(isPlayer())
							getPlayer().setAgathionRes(true);
						effectsToRemove.add(skillId);
					}
				}
			}
			//			else
			//				for(final Abnormal e : getAbnormalList().getEffects())
			//					if(!e.getSkill().isPreservedOnDeath())
			//						effectsToRemove.add(e.getSkill().getId());
			getAbnormalList().stopEffects(effectsToRemove);

			getAbnormalList().clearDebuffs();
		}
		if(isPlayer())
			getPlayer().sendUserInfo(true);
		broadcastStatusUpdate();
		ThreadPoolManager.getInstance().execute(new GameObjectTasks.NotifyAITask(this, CtrlEvent.EVT_DEAD, killer, null, null));
		if(killer != null)
			killer.useTriggers(this, TriggerType.ON_KILL, null, null, 0.0);
		getListeners().onDeath(killer);
	}

	protected void onRevive()
	{
        useTriggers(this, TriggerType.ON_REVIVE, null, null, 0.0);
	}

	public void enableSkill(Skill skill)
	{
		_skillReuses.remove(skill.getReuseHash());
	}

	public Set<AbnormalEffect> getAbnormalEffects()
	{
		return _abnormalEffects;
	}

	public AbnormalEffect[] getAbnormalEffectsArray()
	{
		return _abnormalEffects.toArray(new AbnormalEffect[0]);
	}

	public int getPAccuracy()
	{
		return (int) Math.round(calcStat(Stats.P_ACCURACY_COMBAT, 0.0, null, null));
	}

	public int getMAccuracy()
	{
		return (int) calcStat(Stats.M_ACCURACY_COMBAT, 0.0, null, null);
	}

	public Collection<SkillEntry> getAllSkills()
	{
		return _skills.values();
	}

	public final Stream<SkillEntry> getAllSkillsStream()
	{
		return _skills.values().stream();
	}

	@Deprecated
	public final SkillEntry[] getAllSkillsArray()
	{
		Collection<SkillEntry> vals = _skills.values();
		return vals.toArray(new SkillEntry[0]);
	}

	public final double getAttackSpeedMultiplier()
	{
		return 1.1 * getPAtkSpd() / getBaseStats().getPAtkSpd();
	}

	public int getBuffLimit()
	{
		return (int) calcStat(Stats.BUFF_LIMIT, Config.ALT_BUFF_LIMIT, null, null);
	}

	public Skill getCastingSkill()
	{
		return _castingSkill;
	}

	public boolean isCriticalBlowCastingSkill()
	{
		return _isCriticalBlowCastingSkill;
	}

	public int getPCriticalHit(Creature target)
	{
		return (int) Math.round(calcStat(Stats.BASE_P_CRITICAL_RATE, getBaseStats().getPCritRate(), target, null));
	}

	public int getMCriticalHit(Creature target, Skill skill)
	{
		return (int) Math.round(calcStat(Stats.BASE_M_CRITICAL_RATE, getBaseStats().getMCritRate(), target, skill));
	}

	public double getMagicCriticalDmg(Creature target, Skill skill)
	{
		return 0.01 * calcStat(Stats.M_CRITICAL_DAMAGE, target, skill);
	}

	public double getCurrentCp()
	{
		return _currentCp;
	}

	public final double getCurrentCpRatio()
	{
		return getCurrentCp() / getMaxCp();
	}

	public final double getCurrentCpPercents()
	{
		return getCurrentCpRatio() * 100.0;
	}

	public final boolean isCurrentCpFull()
	{
		return getCurrentCp() >= getMaxCp();
	}

	public final boolean isCurrentCpZero()
	{
		return getCurrentCp() < 1.0;
	}

	public double getCurrentHp()
	{
		return _currentHp;
	}

	public final double getCurrentHpRatio()
	{
		return getCurrentHp() / getMaxHp();
	}

	public final double getCurrentHpPercents()
	{
		return getCurrentHpRatio() * 100.0;
	}

	public final boolean isCurrentHpFull()
	{
		return getCurrentHp() >= getMaxHp();
	}

	public final boolean isCurrentHpZero()
	{
		return getCurrentHp() < 1.0;
	}

	public double getCurrentMp()
	{
		return _currentMp;
	}

	public final double getCurrentMpRatio()
	{
		return getCurrentMp() / getMaxMp();
	}

	public final double getCurrentMpPercents()
	{
		return getCurrentMpRatio() * 100.0;
	}

	public final boolean isCurrentMpFull()
	{
		return getCurrentMp() >= getMaxMp();
	}

	public final boolean isCurrentMpZero()
	{
		return getCurrentMp() < 1.0;
	}

	public int getINT()
	{
		return (int) calcStat(Stats.STAT_INT, getBaseStats().getINT(), null, null);
	}

	public int getSTR()
	{
		return (int) calcStat(Stats.STAT_STR, getBaseStats().getSTR(), null, null);
	}

	public int getCON()
	{
		return (int) calcStat(Stats.STAT_CON, getBaseStats().getCON(), null, null);
	}

	public int getMEN()
	{
		return (int) calcStat(Stats.STAT_MEN, getBaseStats().getMEN(), null, null);
	}

	public int getDEX()
	{
		return (int) calcStat(Stats.STAT_DEX, getBaseStats().getDEX(), null, null);
	}

	public int getWIT()
	{
		return (int) calcStat(Stats.STAT_WIT, getBaseStats().getWIT(), null, null);
	}

	public int getPEvasionRate(Creature target)
	{
		return (int) Math.round(calcStat(Stats.P_EVASION_RATE, 0.0, target, null));
	}

	public int getMEvasionRate(Creature target)
	{
		return (int) calcStat(Stats.M_EVASION_RATE, 0.0, target, null);
	}

	public List<Creature> getAroundCharacters(int radius, int height)
	{
		if(!isVisible())
			return Collections.emptyList();
		return World.getAroundCharacters(this, radius, height);
	}

	public List<NpcInstance> getAroundNpc(int range, int height)
	{
		if(!isVisible())
			return Collections.emptyList();
		return World.getAroundNpc(this, range, height);
	}

	public boolean knowsObject(GameObject obj)
	{
		return World.getAroundObjectById(this, obj.getObjectId()) != null;
	}

	public final SkillEntry getKnownSkill(int skillId)
	{
		return _skills.get(skillId);
	}
  //TODO
	public final Skill getSkillById(Integer skillId)
	{
	 SkillEntry s = _skills.get(skillId);
	 if (s!=null)
		 return s.getTemplate();
		return null;
	}
	
	public final int getMagicalAttackRange(Skill skill)
	{
		if(skill != null)
			return (int) calcStat(Stats.MAGIC_ATTACK_RANGE, skill.getCastRange(), null, skill);
		return getBaseStats().getAtkRange();
	}

	public int getMAtk(Creature target, Skill skill)
	{
		if(skill != null && skill.getMatak() > 0)
			return skill.getMatak();
		return (int) Math.round(calcStat(Stats.MAGIC_ATTACK, getBaseStats().getMAtk(), target, skill));
	}

	public int getMAtkSpd()
	{
		return (int) calcStat(Stats.MAGIC_ATTACK_SPEED, getBaseStats().getMAtkSpd(), null, null);
	}

	public int getMaxCp()
	{
		return Math.max(1, (int) calcStat(Stats.MAX_CP, getBaseStats().getCpMax(), null, null));
	}

	public int getMaxHp()
	{
		return Math.max(1, (int) calcStat(Stats.MAX_HP, getBaseStats().getHpMax(), null, null));
	}

	public int getMaxMp()
	{
		return Math.max(1, (int) calcStat(Stats.MAX_MP, getBaseStats().getMpMax(), null, null));
	}

	public int getMDef(Creature target, Skill skill)
	{
		double mDef = calcStat(Stats.MAGIC_DEFENCE, getBaseStats().getMDef(), target, skill);
		return (int) Math.max(mDef, getBaseStats().getMDef() / 2.0);
	}

	public double getMinDistance(GameObject obj)
	{
		double distance = getColRadius();
		if(obj != null && obj.isCreature())
			distance += obj.getColRadius();
		return distance;
	}

	@Override
	public String getName()
	{
		return StringUtils.defaultString(_name);
	}

	public int getPAtk(Creature target)
	{
		return (int) calcStat(Stats.POWER_ATTACK, getBaseStats().getPAtk(), target, null);
	}

	public int getPAtkSpd()
	{
		return (int) calcStat(Stats.POWER_ATTACK_SPEED, getBaseStats().getPAtkSpd(), null, null);
	}

	public int getPDef(Creature target)
	{
		double pDef = calcStat(Stats.POWER_DEFENCE, getBaseStats().getPDef(), target, null);
		return (int) Math.max(pDef, getBaseStats().getPDef() / 2.0);
	}

	public int getPhysicalAttackRange()
	{
		return (int) calcStat(Stats.POWER_ATTACK_RANGE, 0, null, null);
	}

	public int getRandomDamage()
	{
		WeaponTemplate weaponItem = getActiveWeaponTemplate();
		if(weaponItem == null)
			return getBaseStats().getRandDam();
		return weaponItem.getRandomDamage();
	}

	public double getReuseModifier(Creature target)
	{
		return calcStat(Stats.ATK_REUSE, 1.0, target, null);
	}

	public final int getShldDef()
	{
		return (int) calcStat(Stats.SHIELD_DEFENCE, getBaseStats().getShldDef(), null, null);
	}

	public final int getSkillDisplayLevel(int skillId)
	{
		SkillEntry skill = _skills.get(skillId);
		if(skill == null)
			return -1;
		return skill.getDisplayLevel();
	}

	public int getSkillLevel(int skillId)
	{
		return getSkillLevel(skillId, -1);
	}

	public final int getSkillLevel(int skillId, int def)
	{
		SkillEntry skill = _skills.get(skillId);
		if(skill == null)
			return def;
		return skill.getLevel();
	}

	public GameObject getTarget()
	{
		return target.get();
	}

	public final int getTargetId()
	{
		GameObject target = getTarget();
		return target == null ? -1 : target.getObjectId();
	}

	public CreatureTemplate getTemplate()
	{
		return _template;
	}

	protected void setTemplate(CreatureTemplate template)
	{
		_template = template;
	}

	public String getTitle()
	{
		return StringUtils.defaultString(_title);
	}

	public double headingToRadians(int heading)
	{
		return (heading - 32768) / 10430.378350470453;
	}

	public final boolean isAlikeDead()
	{
		return _fakeDeath || isDead();
	}

	public final boolean isAttackingNow()
	{
		return _attackEndTime > System.currentTimeMillis();
	}

	public final boolean isBlessedByNoblesse()
	{
		return _isBlessedByNoblesse;
	}

	public final boolean isSalvation()
	{
		return _isSalvation;
	}

	public boolean isEffectImmune(Creature effector)
	{
		Creature exception = _effectImmunityException.get();
		return (exception == null || exception != effector) && _effectImmunity.get();
	}

	public boolean isBuffImmune()
	{
		return _buffImmunity.get();
	}

	public boolean isDebuffImmune()
	{
		return _debuffImmunity.get() || isPeaceNpc();
	}

	public boolean isDead()
	{
		return _currentHp < 0.5 || isDead.get();
	}

	@Override
	public final boolean isFlying()
	{
		return _flying;
	}

	public final boolean isInCombat()
	{
		return System.currentTimeMillis() < _stanceEndTime;
	}

	public boolean isMageClass()
	{
		return getBaseStats().getMAtk() > 3.0;
	}

	public final boolean isRunning()
	{
		return _running;
	}

	@Deprecated
	public boolean isSkillDisabled(Skill skill)
	{
		TimeStamp sts = _skillReuses.get(skill.getReuseHash());
		if(sts == null)
			return false;
		if(sts.hasNotPassed())
			return true;
		_skillReuses.remove(skill.getReuseHash());
		return false;
	}

	public boolean isSkillDisabled(SkillEntry skill)
	{
		int reuseHash = skill.getTemplate().getReuseHash();
		TimeStamp sts = _skillReuses.get(reuseHash);
		if(sts == null)
			return false;
		if(sts.hasNotPassed())
			return true;
		_skillReuses.remove(reuseHash);
		return false;
	}

	public final boolean isTeleporting()
	{
		return isTeleporting.get();
	}

	public void updateZones()
	{
		Zone[] zones = isVisible() ? getCurrentRegion().getZones() : Zone.EMPTY_L2ZONE_ARRAY;
		zonesWrite.lock();
		List<Zone> leaving = null;
		List<Zone> entering = null;
		try
		{
			if(!_zones.isEmpty())
			{
				leaving = new ArrayList<>();
				for(int i = 0; i < _zones.size(); ++i)
				{
					Zone zone = _zones.get(i);
					if(!ArrayUtils.contains(zones, zone) || !zone.checkIfInZone(getX(), getY(), getZ(), getReflection()))
						leaving.add(zone);
				}
				if(!leaving.isEmpty())
					for(int i = 0; i < leaving.size(); ++i)
					{
						Zone zone = leaving.get(i);
						_zones.remove(zone);
					}
			}
			if(zones.length > 0)
			{
				entering = new ArrayList<>();
				for(int i = 0; i < zones.length; ++i)
				{
					Zone zone = zones[i];
					if(!_zones.contains(zone) && zone.checkIfInZone(getX(), getY(), getZ(), getReflection()))
						entering.add(zone);
				}
				if(!entering.isEmpty())
					_zones.addAll(entering);
			}
		}
		finally
		{
			zonesWrite.unlock();
		}

		onUpdateZones(leaving, entering);
	}

	protected void onUpdateZones(List<Zone> leaving, List<Zone> entering)
	{
		if(leaving != null && !leaving.isEmpty())
			for(int i = 0; i < leaving.size(); ++i)
			{
				Zone zone = leaving.get(i);
				zone.doLeave(this);
			}
		if(entering != null && !entering.isEmpty())
			for(int i = 0; i < entering.size(); ++i)
			{
				Zone zone = entering.get(i);
				zone.doEnter(this);
			}
	}

	public boolean isInPeaceZone()
	{
      return isInZone(ZoneType.peace_zone) || isInZone(ZoneType.STEAD);
	}
	
	public boolean isInPeaceZoneOld()
	{
      return isInZone(ZoneType.peace_zone) || isInZone(ZoneType.STEAD) || isInZone(ZoneType.peace_zone_old);
	}
	
	public boolean isInZoneBattle()
	{
		return isInZone(Zone.ZoneType.battle_zone);
	}

	public boolean isInWater()
	{
		return isInZone(Zone.ZoneType.water) && !isInBoat() && !isBoat() && !isFlying();
	}

	public boolean isInSiegeZone()
	{
		return isInZone(Zone.ZoneType.SIEGE);
	}

	public boolean isInSSQZone()
	{
		return isInZone(Zone.ZoneType.ssq_zone);
	}

	public boolean isInDangerArea()
	{
		return isInZone(Zone.ZoneType.damage) || isInZone(Zone.ZoneType.swamp) || isInZone(Zone.ZoneType.poison) /*|| isInZone(Zone.ZoneType.instant_skill)*/;
	}

	public boolean isInZone(Zone.ZoneType type)
	{
		zonesRead.lock();
		try
		{
			for(int i = 0; i < _zones.size(); ++i)
			{
				Zone zone = _zones.get(i);
				if(zone.getType() == type)
					return true;
			}
		}
		finally
		{
			zonesRead.unlock();
		}
		return false;
	}
	public List<Event> getZoneEvents()
	{
		zonesRead.lock();
		List<Event> e = Collections.emptyList();
		try
		{
			for(int i = 0; i < _zones.size(); ++i)
			{
				Zone zone = _zones.get(i);
				if(!zone.getEvents().isEmpty())
				{
					if(e.isEmpty())
						e = new ArrayList<>(2);
					e.addAll(zone.getEvents());
				}
			}
		}
		finally
		{
			zonesRead.unlock();
		}
		return e;
	}

	public boolean isInZoneContainsName(String name)
	{
		zonesRead.lock();
		try
		{
			for(int i = 0; i < _zones.size(); ++i)
			{
				Zone zone = _zones.get(i);
				if(zone.getName().contains(name))
					return true;
			}
		}
		finally
		{
			zonesRead.unlock();
		}
		return false;
	}
	
	public boolean isInZone(String name)
	{
		zonesRead.lock();
		try
		{
			for(int i = 0; i < _zones.size(); ++i)
			{
				Zone zone = _zones.get(i);
				if(zone.getName().equals(name))
					return true;
			}
		}
		finally
		{
			zonesRead.unlock();
		}
		return false;
	}

	public boolean isInZone(Zone zone)
	{
		zonesRead.lock();
		try
		{
			return _zones.contains(zone);
		}
		finally
		{
			zonesRead.unlock();
		}
	}

	public Zone getZone(Zone.ZoneType type)
	{
		zonesRead.lock();
		try
		{
			for(int i = 0; i < _zones.size(); ++i)
			{
				Zone zone = _zones.get(i);
				if(zone.getType() == type)
					return zone;
			}
		}
		finally
		{
			zonesRead.unlock();
		}
		return null;
	}

	public List<GveZone> getGveZones()
	{
		zonesRead.lock();

		try
		{
			return _zones.stream()
					.map(Zone::getGveZone)
					.filter(Objects::nonNull)
					.collect(Collectors.toUnmodifiableList());
		}
		finally
		{
			zonesRead.unlock();
		}
	}

	public Location getRestartPoint()
	{
		zonesRead.lock();
		try
		{
			for(int i = 0; i < _zones.size(); ++i)
			{
				Zone zone = _zones.get(i);
				if(zone.getRestartPoints() != null)
				{
					Zone.ZoneType type = zone.getType();
					if(type == Zone.ZoneType.battle_zone || type == Zone.ZoneType.peace_zone || type == Zone.ZoneType.offshore || type == Zone.ZoneType.dummy)
						return zone.getSpawn();
				}
			}
		}
		finally
		{
			zonesRead.unlock();
		}
		return null;
	}

	public Location getPKRestartPoint()
	{
		zonesRead.lock();
		try
		{
			for(int i = 0; i < _zones.size(); ++i)
			{
				Zone zone = _zones.get(i);
				if(zone.getRestartPoints() != null)
				{
					Zone.ZoneType type = zone.getType();
					if(type == Zone.ZoneType.battle_zone || type == Zone.ZoneType.peace_zone || type == Zone.ZoneType.offshore || type == Zone.ZoneType.dummy)
						return zone.getPKSpawn();
				}
			}
		}
		finally
		{
			zonesRead.unlock();
		}
		return null;
	}

	@Override
	public int getGeoZ(Location loc)
	{
		if(isFlying() || isInWater() || isInBoat() || isBoat() || isDoor())
			return loc.z;
		return super.getGeoZ(loc);
	}

	protected boolean needStatusUpdate()
	{
		if(!isVisible())
			return false;
		boolean result = false;
		int bar = (int) (getCurrentHp() * 352.0 / getMaxHp());
		if(bar == 0 || bar != _lastHpBarUpdate)
		{
			_lastHpBarUpdate = bar;
			result = true;
		}
		bar = (int) (getCurrentMp() * 352.0 / getMaxMp());
		if(bar == 0 || bar != _lastMpBarUpdate)
		{
			_lastMpBarUpdate = bar;
			result = true;
		}
		if(isPlayer())
		{
			bar = (int) (getCurrentCp() * 352.0 / getMaxCp());
			if(bar == 0 || bar != _lastCpBarUpdate)
			{
				_lastCpBarUpdate = bar;
				result = true;
			}
		}
		return result;
	}

	public void onHitTimer(Creature target, int damage, boolean crit, boolean miss, boolean soulshot, boolean shld, boolean unchargeSS)
	{
		if(isAlikeDead())
		{
			sendActionFailed();
			return;
		}
		if(target.isDead() || !isInRange(target, 2000L))
		{
			sendActionFailed();
			return;
		}
		if(isPlayable() && target.isPlayable() && isInZoneBattle() != target.isInZoneBattle())
		{
			Player player = getPlayer();
			if(player != null)
			{
				player.sendPacket(SystemMsg.INVALID_TARGET);
				player.sendActionFailed();
			}
			return;
		}
		target.getListeners().onAttackHit(this);
		ThreadPoolManager.getInstance().execute(new GameObjectTasks.NotifyAITask(target, CtrlEvent.EVT_ATTACKED, this, null, damage));
		boolean checkPvP = checkPvP(target, null);
		target.reduceCurrentHp(damage, this, null, true, true, false, true, false, false, true, true, crit, miss, shld, false);
		if(!miss && damage > 0)
		{
			if(!target.isDead())
			{
				if(crit)
                    useTriggers(target, TriggerType.CRIT, null, null, damage);
                useTriggers(target, TriggerType.ATTACK, null, null, damage);
				if(Formulas.calcStunBreak(crit, false, false))
				{
					target.getAbnormalList().stopEffects(EffectType.Stun);
//					target.getAbnormalList().stopEffects(EffectType.Fear);
				}
				if(Formulas.calcCastBreak(target, crit))
					target.abortCast(false, true);
			}
			if(soulshot && unchargeSS)
				unChargeShots(false);
		}
		if(miss)
			target.useTriggers(this, TriggerType.UNDER_MISSED_ATTACK, null, null, damage);
		startAttackStanceTask();
		if(checkPvP)
			startPvPFlag(target);
		if(target.isPlayer())
			for(Servitor servitor : target.getPlayer().getServitors())
				servitor.onOwnerGotAttacked(this);
		else if(target.isServitor())
			((Servitor) target).onAttacked(this);
		else if(isPlayer())
			for(Servitor servitor : getPlayer().getServitors())
				servitor.onOwnerOfAttacks(target);
	}

	public void onMagicUseTimer(Creature aimingTarget, Skill skill, boolean forceUse)
	{
		if(skill == null)
		{
            broadcastPacket(new MagicSkillCanceled(getObjectId()));
			onCastEndTime(null, false);
			return;
		}
		switch(skill.getFlyType())
		{
			case CHARGE:
			case WARP_BACK:
			case WARP_FORWARD:
			{
				Location flyLoc = _flyLoc;
				if(flyLoc == null)
					break;
                setLoc(flyLoc);
				if(skill.getTickInterval() > 0)
				{
					_log.warn("Skill ID[" + skill.getId() + "] LEVEL[" + skill.getLevel() + "] have fly effect and tick effects. Rework please fly algoritm!");
					break;
				}
				break;
			}
		}
		_castInterruptTime = 0L;
		if(!skill.isOffensive() && getAggressionTarget() != null)
			forceUse = true;
		if(!skill.checkCondition(this, aimingTarget, forceUse, false, false))
		{
			if(skill.getSkillType() == Skill.SkillType.PET_SUMMON && isPlayer())
				getPlayer().setPetControlItem(null);
            broadcastPacket(new MagicSkillCanceled(getObjectId()));
			onCastEndTime(null, false);
			return;
		}
		if(!isDoCast() && skill.getCastRange() != -1 && skill.getSkillType() != Skill.SkillType.TAKECASTLE && skill.getSkillType() != Skill.SkillType.TAKEFORTRESS && !GeoEngine.canSeeTarget(this, aimingTarget, isFlying()))
		{
            sendPacket(SystemMsg.CANNOT_SEE_TARGET);
            broadcastPacket(new MagicSkillCanceled(getObjectId()));
			onCastEndTime(null, false);
			return;
		}
		List<Creature> targets = skill.getTargets(this, aimingTarget, forceUse);
		if(skill.getTickInterval() > 0)
		{
			_castInterval -= skill.getTickInterval();
			if(_castInterval >= 0)
			{
				double mpConsumeTick = skill.getMpConsumeTick();
				if(mpConsumeTick > 0.0)
				{
					if(skill.isMusic())
					{
						double inc = mpConsumeTick / 2.0;
						double add = 0.0;
						for(Abnormal e : getAbnormalList().getEffects())
							if(e.getSkill().getId() != skill.getId() && e.getSkill().isMusic() && e.getTimeLeft() > 30)
								add += inc;
						mpConsumeTick += add;
						mpConsumeTick = calcStat(Stats.MP_DANCE_SKILL_CONSUME, mpConsumeTick, aimingTarget, skill);
					}
					else if(skill.isMagic())
						mpConsumeTick = calcStat(Stats.MP_MAGIC_SKILL_CONSUME, mpConsumeTick, aimingTarget, skill);
					else
						mpConsumeTick = calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsumeTick, aimingTarget, skill);
					if(_currentMp < mpConsumeTick && isPlayable())
					{
                        sendPacket(SystemMsg.NOT_ENOUGH_MP);
                        broadcastPacket(new MagicSkillCanceled(getObjectId()));
						onCastEndTime(null, false);
						return;
					}
					reduceCurrentMp(mpConsumeTick, null);
				}
				skill.onTickCast(this, targets);
                useTriggers(aimingTarget, TriggerType.ON_TICK_CAST, null, skill, 0.0);
			}
			if(_castInterval > 0)
			{
				_doCast = true;
				int delay = Math.min(_castInterval, skill.getTickInterval());

				long doCast = delay * 50 / 100;
				_doCastTask = ThreadPoolManager.getInstance().schedule(new DoCastTask(this), doCast);

				_skillLaunchedTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.MagicLaunchedTask(this, forceUse), delay);
				_skillTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.MagicUseTask(this, forceUse), delay);
				return;
			}
		}
		int clanRepConsume = skill.getClanRepConsume();
		if(clanRepConsume > 0)
			getPlayer().getClan().incReputation(-clanRepConsume, false, "clan skills");
		int fameConsume = skill.getFameConsume();
		if(fameConsume > 0)
			getPlayer().setFame(getPlayer().getFame() - fameConsume, "clan skills", true);
		int hpConsume = skill.getHpConsume();
		if(hpConsume > 0)
            setCurrentHp(Math.max(0.0, _currentHp - hpConsume), false);
		double mpConsume2 = skill.getMpConsume2();
		if(mpConsume2 > 0.0)
		{
			if(skill.isMusic())
			{
				double inc2 = mpConsume2 / 2.0;
				double add2 = 0.0;
				for(Abnormal e2 : getAbnormalList().getEffects())
					if(e2.getSkill().getId() != skill.getId() && e2.getSkill().isMusic() && e2.getTimeLeft() > 30)
						add2 += inc2;
				mpConsume2 += add2;
				mpConsume2 = calcStat(Stats.MP_DANCE_SKILL_CONSUME, mpConsume2, aimingTarget, skill);
			}
			else if(skill.isMagic())
				mpConsume2 = calcStat(Stats.MP_MAGIC_SKILL_CONSUME, mpConsume2, aimingTarget, skill);
			else
				mpConsume2 = calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsume2, aimingTarget, skill);
			if(_currentMp < mpConsume2 && isPlayable())
			{
                sendPacket(SystemMsg.NOT_ENOUGH_MP);
                broadcastPacket(new MagicSkillCanceled(getObjectId()));
				onCastEndTime(null, false);
				return;
			}
			reduceCurrentMp(mpConsume2, null);
		}
		callSkill(skill, targets, true, false);
		if(skill.getNumCharges() > 0)
			setIncreasedForce(getIncreasedForce() - skill.getNumCharges());
		if(skill.getCondCharges() > 0 && getIncreasedForce() > 0)
		{
			int decreasedForce = skill.getCondCharges();
			if(decreasedForce > 15)
				decreasedForce = 5;
			setIncreasedForce(getIncreasedForce() - decreasedForce);
		}
		if(skill.isSoulBoost())
			setConsumedSouls(getConsumedSouls() - Math.min(getConsumedSouls(), 5), null);
		else if(skill.getSoulsConsume() > 0)
			setConsumedSouls(getConsumedSouls() - skill.getSoulsConsume(), null);

		switch(skill.getFlyType())
		{
			case THROW_UP:
			case THROW_HORIZONTAL:
			case PUSH_HORIZONTAL:
			case PUSH_DOWN_HORIZONTAL:
			{
				for(Creature target : targets)
				{
					Location flyLoc = target.getFlyLocation(this, skill);
					if(flyLoc == null)
						_log.warn(skill.getFlyType() + " have null flyLoc.");
					target.setLoc(flyLoc);
					target.broadcastPacket(new FlyToLocationPacket(target, flyLoc, skill.getFlyType(), skill.getFlySpeed(), skill.getFlyDelay(), skill.getFlyAnimationSpeed()));
				}
				break;
			}
			case DUMMY:
			{
				Creature dummyTarget = aimingTarget;
				if(skill.getTargetType() == Skill.SkillTargetType.TARGET_AURA)
					dummyTarget = this;
				Location flyLoc = getFlyLocation(dummyTarget, skill);
				if(flyLoc != null)
				{
					setLoc(flyLoc);
					broadcastPacket(new FlyToLocationPacket(this, flyLoc, skill.getFlyType(), skill.getFlySpeed(), skill.getFlyDelay(), skill.getFlyAnimationSpeed()));
				}
				break;
			}
		}

		int chargeAddition = 0;
		if(skill.getFlyType() == FlyToLocationPacket.FlyType.CHARGE && skill.getFlySpeed() > 0)
			chargeAddition = (int) (getDistance(aimingTarget) / skill.getFlySpeed() * 1000.0);
		int skillCoolTime = 0;
		if(!skill.isSkillTimePermanent())
			skillCoolTime = Formulas.calcSkillCastSpd(this, skill, skill.getCoolTime() + chargeAddition);
		else
			skillCoolTime = skill.getCoolTime() + chargeAddition;
		if(skillCoolTime > 0)
			ThreadPoolManager.getInstance().schedule(new GameObjectTasks.CastEndTimeTask(this, targets), skillCoolTime);
		else
			onCastEndTime(targets, true);
	}

	public void onCastEndTime(List<Creature> targets, boolean success)
	{
		Skill castingSkill = getCastingSkill();
		Creature castingTarget = getCastingTarget();
		clearCastVars();
		if(castingSkill != null)
		{
			getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING, castingSkill, castingTarget, success);

			if(success)
			{
				castingSkill.onFinishCast(this, castingTarget, targets);
                useTriggers(castingTarget, TriggerType.ON_FINISH_CAST, null, castingSkill, 0.0);
			}
		}
	}

	public boolean isDoCast() {
		return _doCast;
	}

	public void clearCastVars()
	{
		_castInterval = 0;
		_animationEndTime = 0L;
		_castInterruptTime = 0L;
		_castingSkill = null;
		_isCriticalBlowCastingSkill = false;
		_skillTask = null;
		_skillLaunchedTask = null;
		_flyLoc = null;
		_doCastTask = null;
		_doCast = false;
	}

	public final int getCastInterval()
	{
		return _castInterval;
	}

	public final void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflectAndAbsorb, boolean transferDamage, boolean isDot, boolean sendReceiveMessage)
	{
        reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflectAndAbsorb, transferDamage, isDot, sendReceiveMessage, false, false, false, false, false);
	}

	public void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflectAndAbsorb, boolean transferDamage, boolean isDot, boolean sendReceiveMessage, boolean sendGiveMessage, boolean crit, boolean miss, boolean shld, boolean magic)
	{
		reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflectAndAbsorb, transferDamage, isDot, sendReceiveMessage, sendGiveMessage, crit, miss, shld, magic, false);
	}

	public void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflectAndAbsorb, boolean transferDamage, boolean isDot, boolean sendReceiveMessage, boolean sendGiveMessage, boolean crit, boolean miss, boolean shld, boolean magic, boolean sharedDamage)
	{
		if(isImmortal())
			return;
		boolean damaged = true;
		if(miss || damage <= 0.0)
			damaged = false;
		if(attacker == null || isDead() || attacker.isDead() && !isDot)
			damaged = false;
		boolean damageBlocked = isDamageBlocked(attacker, null);
		if(damageBlocked && transferDamage)
			damaged = false;
		if(!damaged)
		{
			if(attacker != this && sendGiveMessage)
				attacker.displayGiveDamageMessage(this, 0, null, 0, crit, miss, shld, magic);
			return;
		}
		if(damageBlocked && attacker != this)
		{
			if(attacker.isPlayer() && sendGiveMessage)
			{
				ExMagicAttackInfo.packet(attacker, this, MagicAttackType.IMMUNE);
				attacker.sendPacket(SystemMsg.THE_ATTACK_HAS_BEEN_BLOCKED);
			}
			return;
		}
		double reflectedDamage = 0.0;
		double transferedDamage = 0.0;
		Servitor servitorForTransfereDamage = null;
		if(canReflectAndAbsorb)
		{
			boolean canAbsorb = canAbsorb(this, attacker);
			if(canAbsorb)
				damage = absorbToEffector(attacker, damage);
			damage = reduceDamageByMp(attacker, damage);
			transferedDamage = getDamageForTransferToServitor(damage);
			servitorForTransfereDamage = getServitorForTransfereDamage(transferedDamage);
			if(servitorForTransfereDamage != null)
				damage -= transferedDamage;
			else
				transferedDamage = 0.0;
			reflectedDamage = reflectDamage(attacker, skill, damage);
			if(canAbsorb)
				attacker.absorbDamage(this, skill, damage);
		}
		double damageLimit = -1.0;
		if(skill == null)
			damageLimit = calcStat(Stats.RECIEVE_DAMAGE_LIMIT, damage);
		else if(skill.isMagic())
			damageLimit = calcStat(Stats.RECIEVE_DAMAGE_LIMIT_M_SKILL, damage);
		else
			damageLimit = calcStat(Stats.RECIEVE_DAMAGE_LIMIT_P_SKILL, damage);
		if(damageLimit >= 0.0 && damage > damageLimit)
			damage = damageLimit;
		getListeners().onCurrentHpDamage(damage, attacker, skill, sharedDamage);
		if(attacker != this)
		{
			if(sendGiveMessage)
				attacker.displayGiveDamageMessage(this, (int) damage, servitorForTransfereDamage, (int) transferedDamage, crit, miss, shld, magic);
			if(sendReceiveMessage)
				displayReceiveDamageMessage(attacker, (int) damage);
			if(!isDot)
                useTriggers(attacker, TriggerType.RECEIVE_DAMAGE, null, null, damage);
		}
		if(servitorForTransfereDamage != null && transferedDamage > 0.0)
			servitorForTransfereDamage.reduceCurrentHp(transferedDamage, attacker, null, false, false, false, false, true, false, true);
		onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, isDot);
		if(reflectedDamage > 0.0)
		{
			displayGiveDamageMessage(attacker, (int) reflectedDamage, null, 0, false, false, false, false);
			attacker.reduceCurrentHp(reflectedDamage, this, null, true, true, false, false, false, false, true);
		}
	}

	protected void onReduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean isDot)
	{
		if(awake && isSleeping())
			getAbnormalList().stopEffects(EffectType.Sleep);
		if(attacker != this || skill != null && skill.isOffensive())
		{
			TIntSet effectsToRemove = new TIntHashSet();
			for(Abnormal effect : getAbnormalList().getEffects())
				if(effect.getSkill().isDispelOnDamage())
					effectsToRemove.add(effect.getSkill().getId());
			getAbnormalList().stopEffects(effectsToRemove);
			if(isMeditated())
				getAbnormalList().stopEffects(EffectType.Meditation);
			startAttackStanceTask();
			checkAndRemoveInvisible();
		}
		if(damage <= 0.0)
			return;
		if(getCurrentHp() - damage < 10.0 && calcStat(Stats.ShillienProtection) == 1.0)
		{
            setCurrentHp(getMaxHp(), false, true);
            setCurrentCp(getMaxCp());
			return;
		}
		boolean isUndying = isUndying();
        setCurrentHp(Math.max(getCurrentHp() - damage, isDot ? 1.0 : isUndying ? 0.5 : 0.0), false);
		if(isUndying)
		{
            if (getCurrentHp() == 0.5 && _undying != SpecialEffectState.GM)
                if (_undyingFlag.compareAndSet(false, true))
                    getListeners().onDeathFromUndying(attacker);
		}
		else if(getCurrentHp() < 0.5)
		{
			if(attacker != this || skill != null && skill.isOffensive())
                useTriggers(attacker, TriggerType.DIE, null, null, damage);
			doDie(attacker);
		}
	}

	public void reduceCurrentMp(double i, Creature attacker)
	{
		if(attacker != null && attacker != this)
		{
			if(isSleeping())
				getAbnormalList().stopEffects(EffectType.Sleep);
			if(isMeditated())
				getAbnormalList().stopEffects(EffectType.Meditation);
		}
		if(isDamageBlocked(attacker, null) && attacker != null && attacker != this)
		{
			attacker.sendPacket(SystemMsg.THE_ATTACK_HAS_BEEN_BLOCKED);
			return;
		}
		if(attacker != null && attacker.isPlayer() && Math.abs(attacker.getLevel() - getLevel()) > 10)
		{
			if(attacker.isPK() && getAbnormalList().containsEffects(5182) && !isInSiegeZone())
				return;
			if(isPK() && attacker.getAbnormalList().containsEffects(5182) && !attacker.isInSiegeZone())
				return;
		}
		i = _currentMp - i;
		if(i < 0.0)
			i = 0.0;
        setCurrentMp(i);
		if(attacker != null && attacker != this)
			startAttackStanceTask();
	}

	public void removeAllSkills()
	{
        getAllSkillsStream().forEach(this::removeSkill);
	}

	public SkillEntry removeSkill(SkillEntry skillEntry)
	{
		if(skillEntry == null)
			return null;
		return removeSkillById(skillEntry.getId());
	}

	public SkillEntry removeSkillById(int id)
	{
		SkillEntry oldSkillEntry = _skills.remove(id);
		if(oldSkillEntry != null)
		{
			Skill oldSkill = oldSkillEntry.getTemplate();
			if(oldSkill.isToggle())
				getAbnormalList().stopEffects(oldSkill);
			removeTriggers(oldSkill);
			removeStatsOwner(oldSkill);
			if(oldSkill.removeEffectOnDeleteSkill() || Config.ALT_DELETE_SA_BUFFS && (oldSkill.isItemSkill() || oldSkill.isHandler()))
			{
				getAbnormalList().stopEffects(oldSkill);
				for(Servitor servitor : getServitors())
					servitor.getAbnormalList().stopEffects(oldSkill);
			}
			//TODO java.lang.NullPointerException:
			if (!this.isPhantom())
			{
			PlayableAI.AINextAction nextAction = getAI().getNextAction();
			if(nextAction != null && nextAction == PlayableAI.AINextAction.CAST)
			{
				Object args1 = getAI().getNextActionArgs()[0];
				if(args1 == oldSkill)
					getAI().clearNextAction();
			}
			}
		}
		return oldSkillEntry;
	}

	public void addTriggers(StatTemplate f)
	{
		if(f.getTriggerList().isEmpty())
			return;
		for(TriggerInfo t : f.getTriggerList())
			addTrigger(t);
	}

	public void addTrigger(TriggerInfo t)
	{
		if(_triggers == null)
			_triggers = new ConcurrentHashMap<>();
		Set<TriggerInfo> hs = _triggers.get(t.getType());
		if(hs == null)
		{
			hs = new CopyOnWriteArraySet<>();
			_triggers.put(t.getType(), hs);
		}
		hs.add(t);
		if(t.getType() == TriggerType.ADD)
			useTriggerSkill(this, null, t, null, 0.0);
		else if(t.getType() == TriggerType.IDLE)
			new RunnableTrigger(this, t).schedule();
	}

	public Map<TriggerType, Set<TriggerInfo>> getTriggers()
	{
		return _triggers;
	}

	public void removeTriggers(StatTemplate f)
	{
		if(_triggers == null || f.getTriggerList().isEmpty())
			return;
		for(TriggerInfo t : f.getTriggerList())
			removeTrigger(t);
	}

	public void removeTrigger(TriggerInfo t)
	{
		if(_triggers == null)
			return;
		Set<TriggerInfo> hs = _triggers.get(t.getType());
		if(hs == null)
			return;
		hs.remove(t);
		if(t.cancelEffectsOnRemove())
			triggerCancelEffects(t);
	}

	public void sendActionFailed()
	{
        sendPacket(ActionFailPacket.STATIC);
	}

	public boolean hasAI()
	{
		return _ai != null;
	}

	public CharacterAI getAI()
	{
		if(_ai == null)
		{
			synchronized (this)
			{
				if(_ai == null)
				{
					_ai = new CharacterAI(this);
				}
			}
		}
		return _ai;
	}

	public void setAI(CharacterAI newAI)
	{
		if(newAI == null)
			return;
		CharacterAI oldAI = _ai;
		synchronized (this)
		{
			_ai = newAI;
		}
		if(oldAI != null && oldAI.isActive())
		{
			oldAI.stopAITask();
			newAI.startAITask();
			newAI.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
	}

	public final void setCurrentHp(double newHp, boolean canResurrect, boolean sendInfo)
	{
		int maxHp = getMaxHp();
		newHp = Math.min(maxHp, Math.max(0.0, newHp));
		if(isDeathImmune())
			newHp = Math.max(1.1, newHp);
		if(_currentHp == newHp)
			return;
		if(newHp >= 0.5 && isDead() && !canResurrect)
			return;
		double hpStart = _currentHp;
		_currentHp = newHp;
		if(isDead.compareAndSet(true, false))
			onRevive();
		checkHpMessages(hpStart, _currentHp);
		if(sendInfo)
		{
			broadcastStatusUpdate();
			sendChanges();
		}
		if(_currentHp < maxHp)
			startRegeneration();
		getListeners().onChangeCurrentHp(hpStart, newHp);
	}

	public final void setCurrentHp(double newHp, boolean canResurrect)
	{
        setCurrentHp(newHp, canResurrect, true);
	}

	public final void setCurrentMp(double newMp, boolean sendInfo)
	{
		int maxMp = getMaxMp();
		newMp = Math.min(maxMp, Math.max(0.0, newMp));
		if(_currentMp == newMp)
			return;
		if(newMp >= 0.5 && isDead())
			return;
		double mpStart = _currentMp;
		_currentMp = newMp;
		if(sendInfo)
		{
			broadcastStatusUpdate();
			sendChanges();
		}
		if(_currentMp < maxMp)
			startRegeneration();
		getListeners().onChangeCurrentMp(mpStart, newMp);
	}

	public final void setCurrentMp(double newMp)
	{
        setCurrentMp(newMp, true);
	}

	public final void setCurrentCp(double newCp, boolean sendInfo)
	{
		if(!isPlayer())
			return;
		int maxCp = getMaxCp();
		newCp = Math.min(maxCp, Math.max(0.0, newCp));
		if(_currentCp == newCp)
			return;
		if(newCp >= 0.5 && isDead())
			return;
		double cpStart = _currentCp;
		_currentCp = newCp;
		if(sendInfo)
		{
			broadcastStatusUpdate();
			sendChanges();
		}
		if(_currentCp < maxCp)
			startRegeneration();
		getListeners().onChangeCurrentCp(cpStart, newCp);
	}

	public final void setCurrentCp(double newCp)
	{
        setCurrentCp(newCp, true);
	}

	public void setCurrentHpMp(double newHp, double newMp, boolean canResurrect)
	{
		int maxHp = getMaxHp();
		int maxMp = getMaxMp();
		newHp = Math.min(maxHp, Math.max(0.0, newHp));
		newMp = Math.min(maxMp, Math.max(0.0, newMp));
		if(isDeathImmune())
			newHp = Math.max(1.1, newHp);
		if(_currentHp == newHp && _currentMp == newMp)
			return;
		if(newHp >= 0.5 && isDead() && !canResurrect)
			return;
		double hpStart = _currentHp;
		double mpStart = _currentMp;
		_currentHp = newHp;
		_currentMp = newMp;
		if(isDead.compareAndSet(true, false))
			onRevive();
		checkHpMessages(hpStart, _currentHp);
		broadcastStatusUpdate();
		sendChanges();
		if(_currentHp < maxHp || _currentMp < maxMp)
			startRegeneration();
		getListeners().onChangeCurrentHp(hpStart, newHp);
		getListeners().onChangeCurrentMp(mpStart, newMp);
	}

	public void setCurrentHpMp(double newHp, double newMp)
	{
        setCurrentHpMp(newHp, newMp, false);
	}

	public final void setFlying(boolean mode)
	{
		_flying = mode;
	}

	@Override
	public final int getHeading()
	{
		return _heading;
	}

	public final void setHeading(int heading)
	{
        setHeading(heading, false);
	}

	public final void setHeading(int heading, boolean broadcast)
	{
		_heading = heading;
		if(broadcast)
            broadcastPacket(new ExRotation(getObjectId(), heading));
	}

	public final void setIsTeleporting(boolean value)
	{
		isTeleporting.compareAndSet(!value, value);
	}

	public final void setName(String name)
	{
		_name = name;
	}

	public Creature getCastingTarget()
	{
		return _castingTarget.get();
	}

	public void setCastingTarget(Creature target)
	{
		if(target == null)
			_castingTarget = HardReferences.emptyRef();
		else
			_castingTarget = target.getRef();
	}

	public final void setRunning()
	{
		if(!_running)
		{
			_running = true;
            broadcastPacket(changeMovePacket());
		}
	}

	public void setAggressionTarget(Creature target)
	{

	}

	public Creature getAggressionTarget()
	{
		return null;
	}

	public void setTarget(GameObject object)
	{
		if(object != null && !object.isVisible())
			object = null;
		if(object == null)
			target = HardReferences.emptyRef();
		else
			target = object.getRef();
	}

	public void setTitle(String title)
	{
		_title = title;
	}

	public void setWalking()
	{
		if(_running)
		{
			_running = false;
            broadcastPacket(changeMovePacket());
		}
	}

	protected L2GameServerPacket changeMovePacket()
	{
		return new ChangeMoveTypePacket(this);
	}

	public final void startAbnormalEffect(AbnormalEffect ae)
	{
		if(ae == AbnormalEffect.NONE)
			_abnormalEffects.clear();
		else
			_abnormalEffects.add(ae);
		sendChanges();
	}

	public void startAttackStanceTask()
	{
		startAttackStanceTask0();
	}

	protected void startAttackStanceTask0()
	{
		if(isInCombat())
		{
			_stanceEndTime = System.currentTimeMillis() + 15000L;
			return;
		}
		_stanceEndTime = System.currentTimeMillis() + 15000L;
        broadcastPacket(new AutoAttackStartPacket(getObjectId()));
		Future<?> task = _stanceTask;
		if(task != null)
			task.cancel(false);
		_stanceTask = LazyPrecisionTaskManager.getInstance().scheduleAtFixedRate(_stanceTaskRunnable == null ? (_stanceTaskRunnable = new AttackStanceTask()) : _stanceTaskRunnable, 1000L, 1000L);
	}

	public void stopAttackStanceTask()
	{
		_stanceEndTime = 0L;
		Future<?> task = _stanceTask;
		if(task != null)
		{
			task.cancel(false);
			_stanceTask = null;
            broadcastPacket(new AutoAttackStopPacket(getObjectId()));
		}
	}

	protected void stopRegeneration()
	{
		regenLock.lock();
		try
		{
			if(_isRegenerating)
			{
				_isRegenerating = false;
				if(_regenTask != null)
				{
					_regenTask.cancel(false);
					_regenTask = null;
				}
			}
		}
		finally
		{
			regenLock.unlock();
		}
	}

	protected void startRegeneration()
	{
		if(!isVisible() || isDead() || getRegenTick() == 0L)
			return;
		if(_isRegenerating)
			return;
		regenLock.lock();
		try
		{
			if(!_isRegenerating)
			{
				_isRegenerating = true;
				_regenTask = RegenTaskManager.getInstance().scheduleAtFixedRate(_regenTaskRunnable == null ? (_regenTaskRunnable = new RegenTask()) : _regenTaskRunnable, getRegenTick(), getRegenTick());
			}
		}
		finally
		{
			regenLock.unlock();
		}
	}

	public long getRegenTick()
	{
		return 3000L;
	}

	public final void stopAbnormalEffect(AbnormalEffect ae)
	{
		_abnormalEffects.remove(ae);
		sendChanges();
	}

	public void addDeathImmunityEffect(Abnormal effect)
	{
		_deathImmunityEffects.add(effect);
	}

	public boolean removeDeathImmunityEffect(Abnormal effect)
	{
		return _deathImmunityEffects.remove(effect);
	}

	public void block()
	{
		_blocked = true;
	}

	public void unblock()
	{
		_blocked = false;
	}

	public boolean startConfused()
	{
		return _confused.getAndSet(true);
	}

	public boolean stopConfused()
	{
		return _confused.setAndGet(false);
	}

	public boolean startFear()
	{
		return _afraid.getAndSet(true);
	}

	public boolean stopFear()
	{
		return _afraid.setAndGet(false);
	}

	public boolean startMuted()
	{
		return _muted.getAndSet(true);
	}

	public boolean stopMuted()
	{
		return _muted.setAndGet(false);
	}

	public boolean startPMuted()
	{
		return _pmuted.getAndSet(true);
	}

	public boolean stopPMuted()
	{
		return _pmuted.setAndGet(false);
	}

	public boolean startAMuted()
	{
		return _amuted.getAndSet(true);
	}

	public boolean stopAMuted()
	{
		return _amuted.setAndGet(false);
	}

	public boolean startMoveBlock()
	{
		return _moveBlocked.getAndSet(true);
	}

	public boolean stopMoveBlock()
	{
		return _moveBlocked.setAndGet(false);
	}

	public boolean startSleeping()
	{
		return _sleeping.getAndSet(true);
	}

	public boolean stopSleeping()
	{
		return _sleeping.setAndGet(false);
	}

	public boolean startStunning()
	{
		return _stunned.getAndSet(true);
	}

	public boolean stopStunning()
	{
		return _stunned.setAndGet(false);
	}

	public boolean startParalyzed()
	{
		return _paralyzed.getAndSet(true);
	}

	public boolean stopParalyzed()
	{
		return _paralyzed.setAndGet(false);
	}

	public boolean startImmobilized()
	{
		return _immobilized.getAndSet(true);
	}

	public boolean stopImmobilized()
	{
		return _immobilized.setAndGet(false);
	}

	public boolean startHealBlocked()
	{
		return _healBlocked.getAndSet(true);
	}

	public boolean stopHealBlocked()
	{
		return _healBlocked.setAndGet(false);
	}

	public boolean startDamageBlocked()
	{
		return _damageBlocked.getAndSet(true);
	}

	public boolean stopDamageBlocked()
	{
		return _damageBlocked.setAndGet(false);
	}

	public void setDamageBlockedException(Creature exception)
	{
		if(exception == null)
			_damageBlockedException = HardReferences.emptyRef();
		else
			_damageBlockedException = exception.getRef();
	}

	public boolean startBuffImmunity()
	{
		return _buffImmunity.getAndSet(true);
	}

	public boolean stopBuffImmunity()
	{
		return _buffImmunity.setAndGet(false);
	}

	public boolean startDebuffImmunity()
	{
		return _debuffImmunity.getAndSet(true);
	}

	public boolean stopDebuffImmunity()
	{
		return _debuffImmunity.setAndGet(false);
	}

	public boolean startEffectImmunity()
	{
		return _effectImmunity.getAndSet(true);
	}

	public boolean stopEffectImmunity()
	{
		return _effectImmunity.setAndGet(false);
	}

	public void setEffectImmunityException(Creature exception)
	{
		if(exception == null)
			_effectImmunityException = HardReferences.emptyRef();
		else
			_effectImmunityException = exception.getRef();
	}

	public boolean startWeaponEquipBlocked()
	{
		return _weaponEquipBlocked.getAndSet(true);
	}

	public boolean stopWeaponEquipBlocked()
	{
		return _weaponEquipBlocked.getAndSet(false);
	}

	public void startFrozen()
	{
		_frozen = true;
	}

	public void stopFrozen()
	{
		_frozen = false;
	}

	public boolean startDistortedSpace()
	{
		return _distortedSpace.getAndSet(true);
	}

	public boolean stopDistortedSpace()
	{
		return _distortedSpace.setAndGet(false);
	}

	@Override
	public boolean isInvisible(GameObject observer)
	{
		if (observer != null) {
			if (getObjectId() == observer.getObjectId()) {
				return false;
			}
		}
		if (_invisible.get() || !_invisibleEffects.isEmpty()) {
			return true;
		}
		return false;
	}

	public boolean isInvisible() {
		return _invisible.get();
	}

	public boolean setInvisible(boolean value)
	{
		if(value)
			return _invisible.getAndSet(true);
		return _invisible.setAndGet(false);
	}

	public void addInvisibleEffect(Abnormal effect)
	{
		_invisibleEffects.add(effect);
	}

	public boolean removeInvisibleEffect(Abnormal effect)
	{
		return _invisibleEffects.remove(effect);
	}

	public boolean isUndying()
	{
        return _undying != SpecialEffectState.FALSE;
	}

    public void setUndying(SpecialEffectState val)
    {
        _undying = val;
        _undyingFlag.set(false);
    }

	public boolean isInvul()
	{
		return _invul.get() || !_invulEffects.isEmpty();
	}

	public boolean setInvul(boolean value)
	{
		if(value)
			return _invul.getAndSet(true);
		return _invul.setAndGet(false);
	}

	public void addInvulnerableEffect(Abnormal effect)
	{
		_invulEffects.add(effect);
	}

	public boolean removeInvulnerableEffect(Abnormal effect)
	{
		return _invulEffects.remove(effect);
	}

	public void setFakeDeath(boolean value)
	{
		_fakeDeath = value;
	}

	public void breakFakeDeath()
	{
		getAbnormalList().stopEffects(EffectType.FakeDeath);
	}

	public void setMeditated(boolean value)
	{
		_meditated = value;
	}

	public final void setIsBlessedByNoblesse(boolean value)
	{
		_isBlessedByNoblesse = value;
	}

	public final void setIsSalvation(boolean value)
	{
		_isSalvation = value;
	}

	public void setLockedTarget(boolean value)
	{
		_lockedTarget = value;
	}

	public boolean isConfused()
	{
		return _confused.get();
	}

	public boolean isFakeDeath()
	{
		return _fakeDeath;
	}

	public boolean isAfraid()
	{
		return _afraid.get();
	}

	public boolean isBlocked()
	{
		return _blocked;
	}

	public boolean isMuted(Skill skill)
	{
		return skill != null && !skill.isNotAffectedByMute() && (isMMuted() && skill.isMagic() || isPMuted() && !skill.isMagic());
	}

	public boolean isPMuted()
	{
		return _pmuted.get();
	}

	public boolean isMMuted()
	{
		return _muted.get();
	}

	public boolean isAMuted()
	{
		return _amuted.get() || isTransformed() && !getTransform().getType().isCanAttack();
	}

	public boolean isMoveBlocked()
	{
		return _moveBlocked.get();
	}

	public boolean isSleeping()
	{
		return _sleeping.get();
	}

	public boolean isStunned()
	{
		return _stunned.get();
	}

	public boolean isMeditated()
	{
		return _meditated;
	}

	public boolean isWeaponEquipBlocked()
	{
		return _weaponEquipBlocked.get();
	}

	public boolean isParalyzed()
	{
		return _paralyzed.get();
	}

	public boolean isFrozen()
	{
		return _frozen;
	}

	public boolean isImmobilized()
	{
		return _immobilized.get() || getRunSpeed() < 1;
	}

	public boolean isHealBlocked()
	{
		return !isInvul() && (isAlikeDead() || _healBlocked.get());
	}

	public boolean isDamageBlocked(Creature attacker, Skill skill)
	{
		if(isInvul())
			return true;

		if(attacker != null && skill != null && !skill.isAoE() && !isAutoAttackable(attacker))
			return true;

		Creature exception = _damageBlockedException.get();
		if(exception != null && exception == attacker)
			return false;
		if(_damageBlocked.get())
		{
			double blockRadius = calcStat(Stats.DAMAGE_BLOCK_RADIUS);
			if(blockRadius == -1.0)
				return true;
			if(attacker == null)
				return false;
			if(attacker.getDistance(this) <= blockRadius)
				return true;
		}
		return false;
	}

	public boolean isDistortedSpace()
	{
		return _distortedSpace.get();
	}

	public boolean isCastingNow()
	{
		return _skillTask != null;
	}

	public boolean isLockedTarget()
	{
		return _lockedTarget;
	}

	public boolean isMovementDisabled()
	{
		return isBlocked() || isMoveBlocked() || isImmobilized() || isAlikeDead() || isStunned() || isSleeping() || isDecontrolled() || isAttackingNow() || isCastingNow() || isFrozen();
	}

	public final boolean isActionsDisabled()
	{
		return isActionsDisabled(true);
	}

	public boolean isActionsDisabled(boolean withCast)
	{
		return isBlocked() || isAlikeDead() || isStunned() || isSleeping() || isDecontrolled() || isAttackingNow() || withCast && isCastingNow() || isFrozen();
	}

	public final boolean isDecontrolled()
	{
		return isParalyzed() || isKnockDowned() || isKnockBacked() || isFlyUp();
	}

	public final boolean isAttackingDisabled()
	{
		return _attackReuseEndTime > System.currentTimeMillis();
	}

	public boolean isOutOfControl()
	{
		return isBlocked() || isConfused() || isAfraid();
	}

	public void checkAndRemoveInvisible()
	{
		getAbnormalList().stopEffects(EffectType.Invisible);
	}

	public boolean teleToLocation(Location loc)
	{
        return teleToLocation(loc.x, loc.y, loc.z, getReflection());
	}

	public boolean teleToLocation(Location loc, Reflection r)
	{
        return teleToLocation(loc.x, loc.y, loc.z, r);
	}

	public boolean teleToLocation(int x, int y, int z)
	{
        return teleToLocation(x, y, z, getReflection());
	}

	public boolean teleToLocation(Location location, int min, int max)
	{
        return teleToLocation(Location.findAroundPosition(location, min, max, 0), getReflection());
	}

	public boolean teleToLocation(int x, int y, int z, Reflection r)
	{
		if(!isTeleporting.compareAndSet(false, true))
			return false;
		if(isFakeDeath())
			breakFakeDeath();
		abortCast(true, false);
		if(!isLockedTarget())
			setTarget(null);
        stopMove();
		if(!isBoat() && !isFlying() && !World.isWater(new Location(x, y, z), r))
			z = GeoEngine.getHeight(x, y, z, r.getGeoIndex());
		Location loc = Location.findPointToStay(x, y, z, 0, 50, r.getGeoIndex());
		if(isPlayer())
		{
			Player player = (Player) this;
            sendPacket(new TeleportToLocationPacket(this, loc.x, loc.y, loc.z));
			player.getListeners().onTeleport(loc.x, loc.y, loc.z, r);
			decayMe();
            setLoc(loc);
			for(Servitor s : getServitors())
			{
				if(!s.isInRange())
				{
					s.setSpawnAnimation(2);
					s.teleToLocation(loc);
				}
			}
            setReflection(r);
			player.setLastClientPosition(null);
			player.setLastServerPosition(null);

			if (this.isPhantom())
			{
				player.phantom_params.setLockedTarget(null); // убираем свои цели
				player.phantom_params.clearIgnoreList(); // очистим игнорлист
				player.onTeleported();
			}

            sendPacket(new ExTeleportToLocationActivate(this, loc.x, loc.y, loc.z));
		}
		else
		{
            broadcastPacket(new TeleportToLocationPacket(this, loc.x, loc.y, loc.z));
            setLoc(loc);
            setReflection(r);
            sendPacket(new ExTeleportToLocationActivate(this, loc.x, loc.y, loc.z));
			onTeleported();
		}
		return true;
	}

	public void fastTeleport(int x, int y, int z)
	{
		if(!isTeleporting.compareAndSet(false, true))
			return;
		if(isFakeDeath())
			breakFakeDeath();
		abortCast(true, false);
		if(!isLockedTarget())
			setTarget(null);
		stopMove();
		//if(!isBoat() && !isFlying() && !World.isWater(new Location(x, y, z), getReflection()))
		//	z = GeoEngine.getHeight(x, y, z, getReflection().getGeoIndex());
		//Location loc = Location.findPointToStay(x, y, z, 0, 50, getReflection().getGeoIndex());
		var loc = new Location(x, y, z);
		if(isPlayer())
		{
			Player player = (Player) this;
			sendPacket(new TeleportToLocationPacket(this, loc, true));

			decayMe();
			setLoc(loc);

			player.setLastClientPosition(null);
			player.setLastServerPosition(null);
			if (this.isPhantom())
			{
				player.phantom_params.setLockedTarget(null); // убираем свои цели
				player.phantom_params.clearIgnoreList(); // очистим игнорлист
				player.onTeleported();
			}

			sendPacket(new ExTeleportToLocationActivate(this, loc.x, loc.y, loc.z));
		}
		else
		{
			broadcastPacket(new TeleportToLocationPacket(this, loc, true));
			setLoc(loc);

			sendPacket(new ExTeleportToLocationActivate(this, loc.x, loc.y, loc.z));
		}
	}

	public boolean onTeleported()
	{
		return isTeleporting.compareAndSet(true, false);
	}

	public void sendMessage(CustomMessage message)
	{}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + getObjectId() + "]";
	}

	@Override
	public double getColRadius()
	{
		return getBaseStats().getCollisionRadius();
	}

	@Override
	public double getColHeight()
	{
		return getBaseStats().getCollisionHeight();
	}

	public AbnormalList getAbnormalList()
	{
		if(_effectList == null)
			synchronized (this)
			{
				if(_effectList == null)
					_effectList = new AbnormalList(this);
			}
		return _effectList;
	}

	public boolean paralizeOnAttack(Creature attacker)
	{
		int max_attacker_level = 65535;
		NpcInstance leader;
		if(isRaid() || isNpc() && (leader = ((NpcInstance) this).getLeader()) != null && leader.isRaid())
			max_attacker_level = getLevel() + Config.RAID_MAX_LEVEL_DIFF;
		else if(isNpc())
		{
			int max_level_diff = ((NpcInstance) this).getParameter("ParalizeOnAttack", -1000);
			if(max_level_diff != -1000)
				max_attacker_level = getLevel() + max_level_diff;
		}
		return attacker.getLevel() > max_attacker_level;
	}

	@Override
	protected void onDelete()
	{
		stopDeleteTask();
		if(!isObservePoint())
			GameObjectsStorage.remove(this);
		getAbnormalList().stopAllEffects();
		super.onDelete();
	}

	public void addExpAndSp(long exp, long sp)
	{}

	public void broadcastCharInfo()
	{}

	public void broadcastCharInfoImpl(IUpdateTypeComponent... components)
	{}

	public void checkHpMessages(double currentHp, double newHp)
	{}

	public boolean checkPvP(Creature target, Skill skill)
	{
		return false;
	}

	public boolean consumeItem(int itemConsumeId, long itemCount, boolean sendMessage)
	{
		return true;
	}

	public boolean consumeItemMp(int itemId, int mp)
	{
		return true;
	}

	public boolean isFearImmune()
	{
		return isPeaceNpc();
	}

	public boolean isThrowAndKnockImmune()
	{
		return isPeaceNpc();
	}

	public boolean isTransformImmune()
	{
		return isPeaceNpc();
	}

	public boolean isLethalImmune()
	{
		return isBoss() || isRaid();
	}

	public double getChargedSoulshotPower()
	{
		return 0.0;
	}

	public void setChargedSoulshotPower(double val)
	{}

	public double getChargedSpiritshotPower()
	{
		return 0.0;
	}

	public void setChargedSpiritshotPower(double val)
	{}

	public int getIncreasedForce()
	{
		return 0;
	}

	public int getConsumedSouls()
	{
		return 0;
	}

	public int getKarma()
	{
		return 0;
	}

	public boolean isPK()
	{
		return getKarma() < 0;
	}

	public double getLevelBonus()
	{
		return LevelBonusHolder.getInstance().getLevelBonus(getLevel());
	}

	public int getNpcId()
	{
		return 0;
	}

	public boolean isMyServitor(int objId)
	{
		return false;
	}

	public List<Servitor> getServitors()
	{
		return Collections.emptyList();
	}

	public int getPvpFlag()
	{
		return 0;
	}

	public void setTeam(TeamType t)
	{
		_team = t;
		sendChanges();
	}

	public TeamType getTeam()
	{
		return _team;
	}

	public boolean isUndead()
	{
		return false;
	}

	public boolean isParalyzeImmune()
	{
		return false;
	}

	public void reduceArrowCount()
	{}

	public void sendChanges()
	{
		getStatsRecorder().sendChanges();
	}

	public void sendMessage(String message)
	{}

	public void sendPacket(IBroadcastPacket mov)
	{}

	public void sendPacket(IBroadcastPacket... mov)
	{}

	public void sendPacket(List<? extends IBroadcastPacket> mov)
	{}

	public void setIncreasedForce(int i)
	{}

	public void setConsumedSouls(int i, NpcInstance monster)
	{}

	public void startPvPFlag(Creature target)
	{}

	public boolean unChargeShots(boolean spirit)
	{
		return false;
	}

	public void updateEffectIcons()
	{
		if(Config.USER_INFO_INTERVAL == 0L)
		{
			if(_updateEffectIconsTask != null)
			{
				_updateEffectIconsTask.cancel(false);
				_updateEffectIconsTask = null;
			}
			updateEffectIconsImpl();
			return;
		}
		if(_updateEffectIconsTask != null)
			return;
		_updateEffectIconsTask = ThreadPoolManager.getInstance().schedule(new UpdateEffectIcons(), Config.USER_INFO_INTERVAL);
	}

	public void updateEffectIconsImpl()
	{
		broadcastAbnormalStatus(getAbnormalStatusUpdate());
	}

	public ExAbnormalStatusUpdateFromTargetPacket getAbnormalStatusUpdate()
	{
		if(!Config.SHOW_TARGET_EFFECTS)
			return null;
		Abnormal[] effects = getAbnormalList().getFirstEffects();
		Arrays.sort(effects, EffectsComparator.getInstance());
		ExAbnormalStatusUpdateFromTargetPacket abnormalStatus = new ExAbnormalStatusUpdateFromTargetPacket(getObjectId());
		for(Abnormal effect : effects)
			if(!effect.checkAbnormalType(AbnormalType.hp_recover))
				effect.addIcon(abnormalStatus);
		return abnormalStatus;
	}

	public void broadcastAbnormalStatus(ExAbnormalStatusUpdateFromTargetPacket packet)
	{
		if(getTarget() == this)
            sendPacket(packet);
		if(!isVisible())
			return;
		List<Player> players = World.getAroundObservers(this);
		for(int i = 0; i < players.size(); ++i)
		{
			Player target = players.get(i);
			if(target.getTarget() == this)
				target.sendPacket(packet);
		}
	}

	protected void refreshHpMpCp()
	{
		int maxHp = getMaxHp();
		int maxMp = getMaxMp();
		int maxCp = isPlayer() ? getMaxCp() : 0;
		if(_currentHp > maxHp)
            setCurrentHp(maxHp, false);
		if(_currentMp > maxMp)
            setCurrentMp(maxMp, false);
		if(_currentCp > maxCp)
            setCurrentCp(maxCp, false);
		if(_currentHp < maxHp || _currentMp < maxMp || _currentCp < maxCp)
			startRegeneration();
	}

	public void updateStats()
	{
		refreshHpMpCp();
		sendChanges();
	}

	public void setOverhitAttacker(Creature attacker)
	{}

	public void setOverhitDamage(double damage)
	{}

	public int getAccessLevel()
	{
		return 0;
	}

	public Clan getClan()
	{
		return null;
	}

	public int getFormId()
	{
		return 0;
	}

	public boolean isNameAbove()
	{
		return true;
	}

	@Override
	public void setLoc(Location loc)
	{
        setXYZ(loc.x, loc.y, loc.z);
	}

	public void setLoc(Location loc, boolean MoveTask)
	{
        setXYZ(loc.x, loc.y, loc.z, MoveTask);
	}

	@Override
	public void setXYZ(int x, int y, int z)
	{
        setXYZ(x, y, z, false);
	}

	public void setXYZ(int x, int y, int z, boolean MoveTask)
	{
		if(!MoveTask)
            stopMove();
		moveLock.lock();
		try
		{
			super.setXYZ(x, y, z);
			getListeners().onChangeLocation(getLoc(), getReflectionId());
		}
		finally
		{
			moveLock.unlock();
		}
		updateZones();
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();
		updateStats();
		updateZones();
	}

	@Override
	public void spawnMe(Location loc)
	{
		if(loc.h > 0)
            setHeading(loc.h);
		super.spawnMe(loc);
	}

	@Override
	protected void onDespawn()
	{
		if(!isLockedTarget())
			setTarget(null);
        stopMove();
		stopAttackStanceTask();
		stopRegeneration();
		updateZones();
		super.onDespawn();
	}

	public final void doDecay()
	{
		if(!isDead())
			return;
		onDecay();
	}

	protected void onDecay()
	{
		decayMe();
	}

	public void validateLocation(int broadcast)
	{
		L2GameServerPacket sp = new ValidateLocationPacket(this);
		if(broadcast == 0)
            sendPacket(sp);
		else if(broadcast == 1)
            broadcastPacket(sp);
		else
            broadcastPacketToOthers(sp);
	}

	public void addUnActiveSkill(Skill skill)
	{
		if(skill == null || isUnActiveSkill(skill.getId()))
			return;
		if(skill.isToggle())
			getAbnormalList().stopEffects(skill);
		removeStatsOwner(skill);
		removeTriggers(skill);
		_unActiveSkills.add(skill.getId());
	}

	public void removeUnActiveSkill(Skill skill)
	{
		if(skill == null || !isUnActiveSkill(skill.getId()))
			return;
		addStatFuncs(skill.getStatFuncs());
		addTriggers(skill);
		_unActiveSkills.remove(skill.getId());
	}

	public boolean isUnActiveSkill(int id)
	{
		return _unActiveSkills.contains(id);
	}

	public abstract int getLevel();

	public abstract ItemInstance getActiveWeaponInstance();

	public abstract WeaponTemplate getActiveWeaponTemplate();

	public abstract ItemInstance getSecondaryWeaponInstance();

	public abstract WeaponTemplate getSecondaryWeaponTemplate();

	public CharListenerList getListeners()
	{
		if(listeners == null)
			synchronized (this)
			{
				if(listeners == null)
					listeners = new CharListenerList(this);
			}
		return listeners;
	}

	public <T extends Listener<Creature>> boolean addListener(@Nonnull T listener)
	{
		return getListeners().add(listener);
	}

	public <T extends Listener<Creature>> boolean removeListener(@Nonnull T listener)
	{
		return getListeners().remove(listener);
	}

	public CharStatsChangeRecorder<? extends Creature> getStatsRecorder()
	{
		if(_statsRecorder == null)
			synchronized (this)
			{
				if(_statsRecorder == null)
					_statsRecorder = new CharStatsChangeRecorder<>(this);
			}
		return _statsRecorder;
	}

	@Override
	public boolean isCreature()
	{
		return true;
	}

	public void displayGiveDamageMessage(Creature target, int damage, Servitor servitorTransferedDamage, int transferedDamage, boolean crit, boolean miss, boolean shld, boolean magic)
	{
		if(miss && target.isPlayer() && !target.isDamageBlocked(this, null))
			target.sendPacket(new SystemMessage(2264).addName(target).addName(this));
	}

	public void displayReceiveDamageMessage(Creature attacker, int damage)
	{}

	public Collection<TimeStamp> getSkillReuses()
	{
		return _skillReuses.values();
	}

	public TimeStamp getSkillReuse(Skill skill)
	{
		return _skillReuses.get(skill.getReuseHash());
	}

	public Sex getSex()
	{
		return Sex.MALE;
	}

	public final boolean isInFlyingTransform()
	{
		return isTransformed() && getTransform().getType() == TransformType.FLYING;
	}

	public final boolean isVisualTransformed()
	{
		return getVisualTransform() != null;
	}

	public final int getVisualTransformId()
	{
		if(getVisualTransform() != null)
			return getVisualTransform().getId();
		return 0;
	}

	public final TransformTemplate getVisualTransform()
	{
		if(_isInTransformUpdate)
			return null;
		if(_visualTransform != null)
			return _visualTransform;
		return getTransform();
	}

	public final void setVisualTransform(int id)
	{
		TransformTemplate template = id > 0 ? TransformTemplateHolder.getInstance().getTemplate(getSex(), id) : null;
        setVisualTransform(template);
	}

	public void setVisualTransform(TransformTemplate template)
	{
		if(_visualTransform == template)
			return;
		if(template != null && isVisualTransformed() || template == null && isTransformed())
		{
			_isInTransformUpdate = true;
			_visualTransform = null;
			sendChanges();
			_isInTransformUpdate = false;
		}
		_visualTransform = template;
		sendChanges();
	}

	public boolean isTransformed()
	{
		return false;
	}

	public final int getTransformId()
	{
		if(isTransformed())
			return getTransform().getId();
		return 0;
	}

	public TransformTemplate getTransform()
	{
		return null;
	}

	public void setTransform(int id)
	{}

	public void setTransform(TransformTemplate template)
	{}

	public boolean isDeathImmune()
	{
		return _deathImmunity.get() || isPeaceNpc() || !_deathImmunityEffects.isEmpty();
	}

	public boolean startDeathImmunity() {
		return _deathImmunity.getAndSet(true);
	}

	public boolean stopDeathImmunity() {
		return _deathImmunity.setAndGet(false);
	}

	public final double getMovementSpeedMultiplier() {
		if (isRunning())
			return getRunSpeed() * 1. / getBaseStats().getRunSpd();
		return getWalkSpeed() * 1. / getBaseStats().getWalkSpd();
	}

	@Override
	public int getMoveSpeed() {
		if (isRunning())
			return getRunSpeed();
		return getWalkSpeed();
	}

	public int getRunSpeed()
	{
		if(isInWater())
			return getSwimRunSpeed();
		return getSpeed(getBaseStats().getRunSpd());
	}

	public int getWalkSpeed()
	{
		if(isInWater())
			return getSwimWalkSpeed();
		return getSpeed(getBaseStats().getWalkSpd());
	}

	public final int getSwimRunSpeed()
	{
		return getSpeed(getBaseStats().getWaterRunSpd());
	}

	public final int getSwimWalkSpeed()
	{
		return getSpeed(getBaseStats().getWaterWalkSpd());
	}

	public final int getSpeed(double baseSpeed)
	{
		return (int) calcStat(Stats.RUN_SPEED, baseSpeed, null, null);
	}

	public double getHpRegen()
	{
		return calcStat(Stats.REGENERATE_HP_RATE, getBaseStats().getHpReg());
	}

	public double getMpRegen()
	{
		return calcStat(Stats.REGENERATE_MP_RATE, getBaseStats().getMpReg());
	}

	public double getCpRegen()
	{
		return calcStat(Stats.REGENERATE_CP_RATE, getBaseStats().getCpReg());
	}

	public int getEnchantEffect()
	{
		return 0;
	}

	public final boolean isKnockDowned()
	{
		return _knockDowned.get();
	}

	public final boolean stopKnockDown()
	{
		return _knockDowned.setAndGet(false);
	}

	public final boolean startKnockDown()
	{
		return _knockDowned.getAndSet(true);
	}

	public final boolean isKnockBacked()
	{
		return _knockBacked.get();
	}

	public final boolean startKnockBack()
	{
		return _knockBacked.getAndSet(true);
	}

	public final boolean stopKnockBack()
	{
		return _knockBacked.setAndGet(false);
	}

	public final boolean isFlyUp()
	{
		return _flyUp.get();
	}

	public final boolean startFlyUp()
	{
		return _flyUp.getAndSet(true);
	}

	public final boolean stopFlyUp()
	{
		return _flyUp.setAndGet(false);
	}

	public void setRndCharges(int value)
	{
		_rndCharges = value;
	}

	public int getRndCharges()
	{
		return _rndCharges;
	}

	public boolean isPeaceNpc()
	{
		return false;
	}

	public boolean checkInteractionDistance(GameObject target)
	{
		return getRealDistance3D(target) < 200.0;
	}

	@Override
	public boolean isTargetable(Creature creature)
	{
		if(creature != null)
		{
			if(creature == this)
				return true;
			if(creature.isPlayer() && creature.getPlayer().isGM())
				return true;
		}
		return _isTargetable;
	}

	public boolean isTargetable()
	{
		return isTargetable(this);
	}

	public void setTargetable(boolean value)
	{
		_isTargetable = value;
	}

	private boolean checkRange(Creature caster, Creature target)
	{
		return caster.isInRange(target, Config.REFLECT_MIN_RANGE);
	}

	private boolean canAbsorb(Creature attacked, Creature attacker)
	{
		return attacked.isPlayable() || !Config.DISABLE_VAMPIRIC_VS_MOB_ON_PVP || attacker.getPvpFlag() == 0;
	}

	public CreatureBaseStats getBaseStats()
	{
		if(_baseStats == null)
			_baseStats = new CreatureBaseStats(this);
		return _baseStats;
	}

	public boolean isSpecialEffect(Skill skill)
	{
		return false;
	}

	public boolean isImmortal()
	{
		return false;
	}

	public boolean isChargeBlocked()
	{
		return true;
	}

	public int getAdditionalVisualSSEffect()
	{
		return 0;
	}

	public boolean isSymbolInstance()
	{
		return false;
	}

	public boolean isTargetUnderDebuff()
	{
		for(Abnormal effect : getAbnormalList().getEffects())
			if(effect.isOffensive())
				return true;
		return false;
	}

	public boolean isSitting()
	{
		return false;
	}

	public void sendChannelingEffect(Creature target, int state)
	{
        broadcastPacket(new ExShowChannelingEffectPacket(this, target, state));
	}

	public void startDeleteTask(long delay)
	{
		stopDeleteTask();
		_deleteTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.DeleteTask(this), delay);
	}

	public void stopDeleteTask()
	{
		if(_deleteTask != null)
		{
			_deleteTask.cancel(false);
			_deleteTask = null;
		}
	}

	static
	{
		_log = LoggerFactory.getLogger(Creature.class);
		POLE_VAMPIRIC_MOD = new double[] { 1.0, 0.9, 0.0, 7.0, 0.2, 0.01 };
	}

	public class AbortCastDelayed implements Runnable
	{
		private final Creature _cha;

		public AbortCastDelayed(Creature cha)
		{
			_cha = cha;
		}

		@Override
		public void run()
		{
			if(_cha == null)
				return;
			_cha.abortCast(true, true);
		}
	}

	public class AttackStanceTask implements Runnable
	{
		@Override
		public void run()
		{
			if(!isInCombat())
				stopAttackStanceTask();
		}
	}

	private class RegenTask implements Runnable
	{
		@Override
		public void run()
		{
			if(isAlikeDead() || getRegenTick() == 0L)
				return;
			double hpStart = _currentHp;
			double mpStart = _currentMp;
			double cpStart = _currentCp;
			int maxHp = getMaxHp();
			int maxMp = getMaxMp();
			int maxCp = isPlayer() ? getMaxCp() : 0;
			regenLock.lock();
			try
			{
				double addHp = 0.0;
				if(_currentHp < maxHp)
					addHp += getHpRegen();
				double addMp = 0.0;
				if(_currentMp < maxMp)
					addMp += getMpRegen();
				double addCp = 0.0;
				if(_currentCp < maxCp)
					addCp += getCpRegen();
				if(isSitting())
				{
					if(isPlayer() && Config.REGEN_SIT_WAIT)
					{
						Player pl = getPlayer();
						pl.updateWaitSitTime();
						if(pl.getWaitSitTime() > 5)
						{
							addHp += pl.getWaitSitTime();
							addMp += pl.getWaitSitTime();
							addCp += pl.getWaitSitTime();
						}
					}
					else
					{
						addHp += getHpRegen() * 0.5;
						addMp += getMpRegen() * 0.5;
						addCp += getCpRegen() * 0.5;
					}
				}
				else if(!isMoving())
				{
					addHp += getHpRegen() * 0.1;
					addMp += getMpRegen() * 0.1;
					addCp += getCpRegen() * 0.1;
				}
				else if(isRunning())
				{
					addHp -= getHpRegen() * 0.3;
					addMp -= getMpRegen() * 0.3;
					addCp -= getCpRegen() * 0.3;
				}
				if(isRaid())
				{
					addHp *= Config.RATE_RAID_REGEN;
					addMp *= Config.RATE_RAID_REGEN;
				}
				_currentHp += Math.max(0.0, Math.min(addHp, calcStat(Stats.HP_LIMIT, null, null) * maxHp / 100.0 - _currentHp));
				_currentHp = Math.min(maxHp, _currentHp);
				Creature this$0 = Creature.this;
				this$0._currentMp += Math.max(0.0, Math.min(addMp, calcStat(Stats.MP_LIMIT, null, null) * maxMp / 100.0 - _currentMp));
				_currentMp = Math.min(maxMp, _currentMp);
				getListeners().onChangeCurrentHp(hpStart, _currentHp);
				getListeners().onChangeCurrentMp(mpStart, _currentMp);
				if(isPlayer())
				{
					Creature this$2 = Creature.this;
					this$2._currentCp += Math.max(0.0, Math.min(addCp, calcStat(Stats.CP_LIMIT, null, null) * maxCp / 100.0 - _currentCp));
					_currentCp = Math.min(maxCp, _currentCp);
					getListeners().onChangeCurrentCp(cpStart, _currentCp);
				}
				if(_currentHp == maxHp && _currentMp == maxMp && _currentCp == maxCp)
					stopRegeneration();
			} finally {
				regenLock.unlock();
			}
			broadcastStatusUpdate();
			sendChanges();
			checkHpMessages(hpStart, _currentHp);
		}
	}

	public boolean isPlayable() {
		return false;
	}

	public Playable getPlayable() {
		return null;
	}

	public int getWaterZ() {
		if (!isInWater())
			return Integer.MIN_VALUE;

		zonesRead.lock();
		int waterZ = Integer.MIN_VALUE;
		try {
			for (int i = 0; i < _zones.size(); ++i) {
				Zone zone = _zones.get(i);
				if (zone.getType() == Zone.ZoneType.water && (waterZ == Integer.MIN_VALUE || waterZ < zone.getTerritory().getZmax()))
					waterZ = zone.getTerritory().getZmax();
			}
		} finally {
			zonesRead.unlock();
		}

		return waterZ;
	}

	public int getWaterBottomZ() {
		if (!isInWater())
			return Integer.MAX_VALUE;

		int waterBottomZ = Integer.MAX_VALUE;
		zonesRead.lock();
		try {
			Zone zone;
			for (int i = 0; i < _zones.size(); i++) {
				zone = _zones.get(i);
				if (zone.getType() == ZoneType.water)
					if (waterBottomZ == Integer.MAX_VALUE || waterBottomZ > zone.getTerritory().getZmin())
						waterBottomZ = zone.getTerritory().getZmin();
			}
		} finally {
			zonesRead.unlock();
		}

		return waterBottomZ;
	}

	public int getMoveTickInterval() {
		return isPlayer() ? MOVE_TASK_QUANTUM_PC : MOVE_TASK_QUANTUM_NPC;
	}

	@Override
	public int getActingRange() {
		return 150;
	}

	public boolean isHero() {
		return false;
	}

	private boolean setSimplePath(Location dest) {
		List<Location> moveList = GeoMove.constructMoveList(getLoc(), dest);
		if (moveList.isEmpty()) {
			return false;
		}
		_targetRecorder.clear();
		_targetRecorder.add(moveList);
		return true;
	}

	@Override
	public Fraction getFraction() {
		return fraction;
	}

	public void setFraction(Fraction fraction) {
		this.fraction = fraction;
	}

	public Future<?> getDeleteTask()
	{
		return _deleteTask;
	}

	public boolean isGuard()
	{
		return false;
	}

	public boolean isOutpost()
	{
		return false;
	}

	public ActionPermissionComponent getActionPermissionComponent() {
		return actionPermissionComponent;
	}

	public boolean isUndyingFlag(){
		return _undyingFlag.get();
	}

	private static class DoCastTask implements Runnable {
		private final HardReference<? extends Creature> creatureRef;

		DoCastTask(Creature creature) {
			this.creatureRef = creature.getRef();
		}

		public void run() {
			if(creatureRef == null)
				return;
			Creature creature = creatureRef.get();
			if(creature == null)
				return;
			Skill castingSkill = creature.getCastingSkill();
			Creature castingTarget = creature.getCastingTarget();
			if(castingSkill == null || castingTarget == null) {
				creature.setDoCast(false);
				return;
			}
			creature.setDoCast(GeoEngine.canSeeTarget(creature, castingTarget, false));
		}
	}

	private void setDoCast(boolean doCast) {
		this._doCast = doCast;
	}

	private class UpdateEffectIcons implements Runnable {
		@Override
		public void run() {
			updateEffectIconsImpl();
			_updateEffectIconsTask = null;
		}
	}


	public boolean hasClubCard() {
		return Arrays.stream(Config.GVE_CLUB_CARD_ITEMS).anyMatch(id -> ItemFunctions.haveItem(getPlayable(), id, 1));
	}

	public void setGreed(double value) {
		this.greed = value;
	}

	public double getGreed() {
		return this.greed;
	}

	public void addReApplyTask(ScheduledFuture<?> task) {
		reApplyTasks.add(task);
	}

	public void cancelReApplyTasks() {
		for (ScheduledFuture<?> task : reApplyTasks) {
			if (task != null && !task.isDone()) {
				task.cancel(false);
			}
		}
		reApplyTasks.clear();
	}

	//TODO move
	private volatile Location _lastClientPosition;
	private volatile Location _lastServerPosition;
	
	public void setLastClientPosition(final Location position)
	{
		_lastClientPosition = position;
	}
	
	public Location getLastClientPosition()
	{
		return _lastClientPosition;
	}
	
	public void setLastServerPosition(final Location position)
	{
		_lastServerPosition = position;
	}
	
	public Location getLastServerPosition()
	{
		return _lastServerPosition;
	}
	
	private boolean _running;
	protected MoveActionBase moveAction;
	private Future <?> _moveTask;
	private Runnable _moveTaskRunnable;
	private final Lock moveLock = new ReentrantLock();



	public int getInteractDistance(final GameObject target)
	{
		return getActingRange()+(int) getMinDistance(target);
	}

	protected static class CreatureMoveActionTask extends RunnableImpl
	{
		private final HardReference <? extends Creature> _creatureRef;

		public CreatureMoveActionTask(final Creature creature)
		{
			_creatureRef = creature.getRef();
		}

		@Override
		public void runImpl()
		{
			final Creature actor = _creatureRef.get();
			if (actor == null)
			{
				return;
			}
			actor.moveLock.lock();
			try
			{
				final MoveActionBase moveActionBase = actor.moveAction;
				if (actor._moveTaskRunnable == this && moveActionBase != null && !moveActionBase.isFinished() && moveActionBase.tickImpl(actor) && actor._moveTaskRunnable == this)
				{
					moveActionBase.scheduleNextTick();
				}
			}finally
			{
				actor.moveLock.unlock();
			}
		}
	}

	protected abstract static class MoveActionBase
	{
		private final HardReference <? extends Creature> actorRef;
		protected volatile boolean isFinished;
		private long prevTick;
		private int prevSpeed;
		private double passDist;

		public MoveActionBase(final Creature actor)
		{
			isFinished = false;
			actorRef = actor.getRef();
			prevTick = 0L;
			prevSpeed = 0;
			passDist = 0.0;
			isFinished = false;
		}


		protected Creature getActor()
		{
			return actorRef.get();
		}

		protected void setIsFinished(final boolean isFinished)
		{
			this.isFinished = isFinished;
		}

		public boolean isFinished()
		{
			return isFinished;
		}

		protected long getPrevTick()
		{
			return prevTick;
		}

		protected void setPrevTick(final long prevTick)
		{
			this.prevTick = prevTick;
		}

		protected int getPrevSpeed()
		{
			return prevSpeed;
		}

		protected void setPrevSpeed(final int prevSpeed)
		{
			this.prevSpeed = prevSpeed;
		}

		protected double getPassDist()
		{
			return passDist;
		}

		protected void setPassDist(final double passDist)
		{
			this.passDist = passDist;
		}

		public boolean start()
		{
			final Creature actor = getActor();
			if (actor == null)
			{
				return false;
			}
			setPrevTick(System.currentTimeMillis());
			setPrevSpeed(actor.getMoveSpeed());
			setPassDist(0.0);
			setIsFinished(false);
			return weightCheck(actor);
		}

		public abstract Location moveFrom();

		public abstract Location moveTo();

		protected double getMoveLen()
		{
			return PositionUtils.calculateDistance(moveFrom(), moveTo(), includeMoveZ());
		}

		protected boolean includeMoveZ()
		{
			final Creature actor = getActor();
			return actor == null || actor.isInWater() || actor.isFlying() || actor.isBoat() || actor.isInBoat();
		}

		public int getNextTickInterval()
		{
			if (!getActor().isPlayable())
			{
				return Math.min(Config.MOVE_TASK_QUANTUM_NPC, (int) (1000.0*(getMoveLen()-getPassDist())/Math.max(getPrevSpeed(), 1)));
			}
			return Math.min(Config.MOVE_TASK_QUANTUM_PC, (int) (1000.0*(getMoveLen()-getPassDist())/Math.max(getPrevSpeed(), 1)));
		}

		protected boolean onEnd()
		{
			return true;
		}

		protected void onFinish(final boolean finishedWell, final boolean isInterrupted)
		{
			setIsFinished(true);
		}

		public void interrupt()
		{
			tick();
			onFinish(false, true);
		}

		protected boolean onTick(final double done)
		{
			final Creature actor = getActor();
			if (actor == null)
			{
				onFinish(false, true);
				return false;
			}
			return true;
		}

		public boolean scheduleNextTick()
		{
			final Creature actor = getActor();
			if (actor == null)
			{
				return false;
			}
			if (actor.isPhantom())
			{
				actor.setLastServerPosition(actor.getLoc());
				actor.setLastClientPosition(actor.getLoc());
			}

			Runnable r;
			actor._moveTaskRunnable = (r = new CreatureMoveActionTask(actor));
			actor._moveTask = ThreadPoolManager.getInstance().schedule(r, getNextTickInterval());
			return true;
		}

		public boolean tick()
		{
			final Creature actor = getActor();
			if (actor == null)
			{
				return false;
			}
			actor.moveLock.lock();
			try
			{
				return tickImpl(actor);
			}finally
			{
				actor.moveLock.unlock();
			}
		}

		private boolean tickImpl(final Creature actor)
		{
			if (isFinished())
			{
				return false;
			}
			if (actor.moveAction != this)
			{
				setIsFinished(true);
				return false;
			}
			if (actor.isMovementDisabled())
			{
				onFinish(false, false);
				return false;
			}
			final int currSpeed = actor.getMoveSpeed();
			if (currSpeed <= 0)
			{
				onFinish(false, false);
				return false;
			}
			final long now = System.currentTimeMillis();
			final float delta = (now-getPrevTick())/1000.0f;
			final boolean includeMoveZ = includeMoveZ();
			double passLen = getPassDist();
			passLen += delta*(Math.max(getPrevSpeed()+currSpeed, 2)/2.0);
			setPrevTick(now);
			setPrevSpeed(currSpeed);
			setPassDist(passLen);
			final double len = getMoveLen();
			final double done = Math.max(0.0, Math.min(passLen/Math.max(len, 1.0), 1.0));
			final Location currLoc = actor.getLoc();
			final Location newLoc = currLoc.clone();
			if (!calcMidDest(actor, newLoc, includeMoveZ, done, passLen, len))
			{
				onFinish(false, false);
				return false;
			}
			if (!includeMoveZ)
			{}
			actor.setLoc(newLoc, true);
			if (done == 1.0)
			{
				return !onEnd();
			}
			if (!onTick(done))
			{
				setIsFinished(true);
				return false;
			}
			return true;
		}

		protected boolean weightCheck(final Creature creature)
		{
			if (!creature.isPlayer())
			{
				return true;
			}
			if (creature.getPlayer().getCurrentLoad() >= 2*creature.getPlayer().getMaxLoad())
			{
				creature.sendPacket(new SystemMessage(555));
				return false;
			}
			return true;
		}

		protected boolean calcMidDest(final Creature creature, final Location result, final boolean includeZ, final double done, final double pass, final double len)
		{
			result.set(moveTo().clone().indent(moveFrom(), (int) Math.round(len-pass), creature.isFlying() || creature.isInWater())).correctGeoZ();
			return true;
		}

		public abstract L2GameServerPacket movePacket();
	}

	public abstract static class MoveToAction extends MoveActionBase
	{
		protected final int indent;
		protected final boolean pathFind;
		protected final boolean ignoreGeo;
		protected final Queue <List <Location>> geoPathLines;
		protected List <Location> currentGeoPathLine;
		protected Location moveFrom;
		protected Location moveTo;
		protected double prevMoveLen;
		protected boolean prevIncZ;

		protected MoveToAction(final Creature actor,final boolean ignoreGeo,final int indent,final boolean pathFind)
		{
			super(actor);
			this.indent = indent;
			this.pathFind = pathFind;
			this.ignoreGeo = ignoreGeo;
			geoPathLines = new LinkedList <>();
			currentGeoPathLine = Collections.emptyList();
			moveFrom = actor.getLoc();
			moveTo = actor.getLoc();
			prevMoveLen = 0.0;
			prevIncZ = false;
		}

		protected boolean buildPathLines(final Location pathFrom, final Location pathTo)
		{
			final Creature actor = getActor();
			if (actor == null)
			{
				return false;
			}
			final LinkedList <List <Location>> geoPathLines = new LinkedList <>();
			if (!GeoMove.buildGeoPath(geoPathLines, pathFrom.clone().world2geo(), pathTo.clone().world2geo(), actor.getGeoIndex(), (int) actor.getColRadius(), (int) actor.getColHeight(), indent, pathFind && !ignoreGeo && !isRelativeMove(), true, actor.isFlying(), actor.isInWater(), actor.getWaterZ(), ignoreGeo))
			{
				return false;
			}
			this.geoPathLines.clear();
			this.geoPathLines.addAll(geoPathLines);
			return true;
		}

		protected boolean pollPathLine()
		{
			final List <Location> currentGeoPathLine = geoPathLines.poll();
			this.currentGeoPathLine = currentGeoPathLine;
			if (currentGeoPathLine != null)
			{
				final Creature actor = getActor();
				moveFrom = this.currentGeoPathLine.get(0).clone().geo2world();
				moveTo = this.currentGeoPathLine.get(this.currentGeoPathLine.size()-1).clone().geo2world();
				prevIncZ = includeMoveZ();
				prevMoveLen = PositionUtils.calculateDistance(moveFrom, moveTo, prevIncZ);
				setPassDist(0.0);
				setPrevTick(System.currentTimeMillis());
				if (prevMoveLen > 16.0)
				{
					actor.setHeading(PositionUtils.calculateHeadingFrom(moveFrom.getX(), moveFrom.getY(), moveTo.getX(), moveTo.getY()));
				}
				return true;
			}
			return false;
		}

		protected int remainingLinesCount()
		{
			return geoPathLines.size();
		}

		protected abstract boolean isRelativeMove();

		@Override
		protected boolean calcMidDest(final Creature creature, final Location result, final boolean includeZ, final double done, final double pass, final double len)
		{
			if (currentGeoPathLine == null)
			{
				return false;
			}
			final Location currLoc = creature.getLoc();
			if (len < 16.0 || done == 0.0 || pass == 0.0 || currentGeoPathLine.isEmpty())
			{
				result.set(currLoc);
				return true;
			}
			final int lastIdx = currentGeoPathLine.size()-1;
			result.set(moveFrom).indent(moveTo, (int) (pass+0.5), includeZ).setZ(currentGeoPathLine.get(Math.min(lastIdx, (int) (lastIdx*done+0.5))).getZ());
			return result.equalsGeo(currLoc) || ignoreGeo || !Config.ALLOW_GEODATA || includeZ || GeoEngine.canMoveToCoord(currLoc.getX(), currLoc.getY(), currLoc.getZ(), result.getX(), result.getY(), result.getZ(), creature.getGeoIndex());
		}

		@Override
		public Location moveFrom()
		{
			return moveFrom;
		}

		@Override
		public Location moveTo()
		{
			return moveTo;
		}

		@Override
		protected double getMoveLen()
		{
			final boolean incZ = includeMoveZ();
			if (incZ != prevIncZ)
			{
				prevMoveLen = PositionUtils.calculateDistance(moveFrom, moveTo, incZ);
				prevIncZ = incZ;
			}
			return prevMoveLen;
		}
	}

	public static class MoveToLocationAction extends MoveToAction
	{
		private final Location dst;
		private final Location src;

		public MoveToLocationAction(final Creature actor,final Location moveFrom,final Location moveTo,final boolean ignoreGeo,final int indent,final boolean pathFind)
		{
			super(actor, ignoreGeo, indent, pathFind);
			src = moveFrom.clone();
			dst = moveTo.clone();
		}

		public MoveToLocationAction(final Creature actor,final Location dest,final int indent,final boolean pathFind)
		{
			this(actor, actor.getLoc(), dest, actor.isBoat() || actor.isInBoat(), indent, pathFind);
		}

		public boolean isSameDest(final Location to)
		{
			return dst.equalsGeo(to);
		}

		@Override
		public boolean start()
		{
			return super.start() && buildPathLines(src, dst) && !onEnd();
		}

		@Override
		protected boolean onEnd()
		{
			final Creature actor = getActor();
			if (actor == null)
			{
				return true;
			}
			if (!pollPathLine())
			{
				onFinish(true, false);
				return true;
			}
			actor.broadcastMove();
			return false;
		}

		@Override
		protected void onFinish(final boolean finishedWell, final boolean isInterrupted)
		{
			final Creature actor = getActor();
			if (isFinished() || actor == null)
			{
				return;
			}

			if (actor.isPlayer() && !actor.isPhantom()&& actor.getPlayer().tScheme_record.isLogging())
				actor.getPlayer().tScheme_record.stopMove(finishedWell);

			if (isInterrupted)
			{
				setIsFinished(true);
				return;
			}
			if (finishedWell)
			{
				ThreadPoolManager.getInstance().execute(new NotifyAITask(actor, CtrlEvent.EVT_ARRIVED));
			}
			else
			{
				actor.stopMove(true, true, false);
				ThreadPoolManager.getInstance().execute(new NotifyAITask(actor, CtrlEvent.EVT_ARRIVED_BLOCKED, actor.getLoc()));
			}
			super.onFinish(finishedWell, isInterrupted);
		}

		@Override
		public L2GameServerPacket movePacket()
		{
			final Creature actor = getActor();
			
			//return (actor != null) ? new CharMoveToLocation(actor, actor.getLoc(), moveTo.clone()) : null;
			return (actor != null) ? new MTLPacket(actor, actor.getLoc(), moveTo.clone()) : null;
		}

		@Override
		protected boolean isRelativeMove()
		{
			return false;
		}
	}

	public static class MoveToRelativeAction extends MoveToAction
	{
		private final HardReference <? extends GameObject> targetRef;
		private final int range;
		private Location prevTargetLoc;
		private boolean isRelativeMoveEnabled;

		protected MoveToRelativeAction(final Creature actor,final GameObject target,final boolean ignoreGeo,final int indent,final int range,final boolean pathFind)
		{
			super(actor, ignoreGeo, indent, pathFind);
			targetRef = target.getRef();
			prevTargetLoc = target.getLoc().clone();
			this.range = Math.max(range, indent+16);
			isRelativeMoveEnabled = false;
		}

		private GameObject getTarget()
		{
			return targetRef.get();
		}

		public boolean isSameTarget(final GameObject target)
		{
			return getTarget() == target;
		}

		@Override
		public boolean start()
		{
			if (!super.start())
			{
				return false;
			}
			final Creature actor = getActor();
			final GameObject target = getTarget();
			if (actor == null || target == null)
			{
				return false;
			}
			final Location actorLoc = actor.getLoc();
			final Location pawnLoc = target.getLoc().clone();
			if (!buildPathLines(actorLoc, pawnLoc))
			{
				return false;
			}
			prevTargetLoc = pawnLoc.clone();
			return !onEnd();
		}

		protected boolean isPathRebuildRequired()
		{
			final Creature actor = getActor();
			final GameObject target = getTarget();
			if (actor == null || target == null)
			{
				return true;
			}
			final Location targetLoc = target.getLoc();
			return isRelativeMoveEnabled && !prevTargetLoc.equalsGeo(targetLoc);
		}

		@Override
		protected boolean onEnd()
		{
			final Creature actor = getActor();
			final GameObject target = getTarget();
			if (actor == null || target == null)
			{
				return true;
			}
			final int remainingLinesCount = remainingLinesCount();
			if (remainingLinesCount > 1)
			{
				if (!pollPathLine())
				{
					onFinish(false, false);
					return true;
				}
			}
			else
			{
				if (remainingLinesCount != 1)
				{
					onFinish(true, false);
					return true;
				}
				if (!(actor instanceof Servitor))
				{
					isRelativeMoveEnabled = true;
				}
				if (isPathRebuildRequired())
				{
					if (isArrived())
					{
						onFinish(true, false);
						return true;
					}
					final Location actorLoc = actor.getLoc();
					final Location targetLoc = getImpliedTargetLoc();
					if (!buildPathLines(actorLoc, targetLoc))
					{
						onFinish(false, false);
						return true;
					}
					if (!pollPathLine())
					{
						onFinish(false, false);
						return true;
					}
					prevTargetLoc = targetLoc.clone();
				}
				else if (!pollPathLine())
				{
					onFinish(false, false);
					return true;
				}
			}
			actor.broadcastMove();
			return false;
		}

		protected boolean isArrived()
		{
			final Creature actor = getActor();
			final GameObject target = getTarget();
			if (actor == null || target == null)
			{
				return false;
			}
			if (target.isCreature() && ((Creature) target).isMoving())
			{
				final int threshold = indent+16;
				if (includeMoveZ())
				{
					return target.isInRangeZ(actor, threshold);
				}
				return target.isInRange(actor, threshold);
			}
			else
			{
				if (includeMoveZ())
				{
					return target.isInRangeZ(actor, indent+16);
				}
				return target.isInRange(actor, indent+16);
			}
		}

		private Location getImpliedTargetLoc()
		{
			final Creature actor = getActor();
			final GameObject targetObj = getTarget();
			if (actor == null || targetObj == null)
			{
				return null;
			}
			if (!targetObj.isCreature())
			{
				return targetObj.getLoc();
			}
			final Creature target = (Creature) targetObj;
			final Location loc = targetObj.getLoc();
			if (!target.isMoving())
			{
				return loc;
			}
			return GeoMove.getIntersectPoint(actor.getLoc(), loc, target.getMoveSpeed(), Math.max(128, Config.MOVE_TASK_QUANTUM_PC/2));
		}

		@Override
		protected boolean onTick(final double done)
		{
			if (!super.onTick(done))
			{
				return false;
			}
			final Creature actor = getActor();
			final GameObject target = getTarget();
			if (actor == null || target == null)
			{
				return false;
			}
			if (done < 1.0)
			{
				if (isPathRebuildRequired())
				{
					final Location actorLoc = actor.getLoc();
					final Location pawnLoc = getImpliedTargetLoc();
					if (actor.isPlayer() && actor.getPlayer().getNetConnection() != null)
					{
						final int pawnClippingRange = actor.getPlayer().getNetConnection().getPawnClippingRange();
						if (actorLoc.distance3D(pawnLoc) > pawnClippingRange)
						{
							onFinish(false, false);
							return false;
						}
					}
					if (!buildPathLines(actorLoc, pawnLoc))
					{
						onFinish(false, false);
						return false;
					}
					if (!pollPathLine())
					{
						onFinish(false, false);
						return false;
					}
					prevTargetLoc = pawnLoc.clone();
				}
				else if (isRelativeMoveEnabled && isArrived())
				{
					onFinish(true, false);
					return false;
				}
			}
			return true;
		}

		@Override
		protected void onFinish(final boolean finishedWell, final boolean isInterrupted)
		{
			final Creature actor = getActor();
			final GameObject target = getTarget();
			if (isFinished() || actor == null || target == null)
			{
				return;
			}
			if (isInterrupted)
			{
				setIsFinished(true);
				return;
			}
			actor.stopMove(!(target instanceof StaticObjectInstance) && !target.isDoor(), false, false);
			boolean succeed = false;
			if (finishedWell)
			{
				succeed = ((includeMoveZ() ? actor.getRealDistance3D(target) : actor.getRealDistance(target)) <= range+16);
			}
			setIsFinished(true);
			if (succeed)
			{
				ThreadPoolManager.getInstance().execute(new NotifyAITask(actor, CtrlEvent.EVT_ARRIVED_TARGET));
			}
			else
			{
				ThreadPoolManager.getInstance().execute(new NotifyAITask(actor, CtrlEvent.EVT_ARRIVED_BLOCKED, actor.getLoc()));
			}
		}

		@Override
		protected boolean isRelativeMove()
		{
			return isRelativeMoveEnabled;
		}

		@Override
		public L2GameServerPacket movePacket()
		{
			final Creature actor = getActor();
			if (actor == null)
			{
				return null;
			}
			final GameObject target = getTarget();
			if (!isRelativeMove())
			{
				return new MTLPacket(actor, actor.getLoc(), moveTo.clone());
			}
			if (target == null)
			{
				return null;
			}
			return new MTPPacket(actor, target, indent);
		}
	}

	public Location getDestination()
	{
		if (moveAction instanceof MoveToLocationAction)
		{
			return moveAction.moveTo().clone();
		}
		return null;
	}

	public boolean isMoving()
	{
		final MoveActionBase theMoveActionBase = moveAction;
		return theMoveActionBase != null && !theMoveActionBase.isFinished();
	}

	public boolean isFollowing()
	{
		final MoveActionBase theMoveActionBase = moveAction;
		return theMoveActionBase != null && theMoveActionBase instanceof MoveToRelativeAction && !theMoveActionBase.isFinished();
	}

	public boolean isFollow()
	{
		return isFollowing();
	}

	public int maxZDiff()
	{
		final MoveActionBase theMoveActionBase = moveAction;
		if (theMoveActionBase != null)
		{
			final Location moveFrom = theMoveActionBase.moveFrom();
			final Location moveTo = theMoveActionBase.moveTo();
			if (moveFrom.getZ() > moveTo.getZ())
			{
				return moveFrom.getZ()-moveTo.getZ();
			}
		}
		return Config.MAX_Z_DIFF;
	}

	public Creature getFollowTarget()
	{
		final MoveActionBase moveAction = this.moveAction;
		if (moveAction instanceof MoveToRelativeAction && !moveAction.isFinished())
		{
			final MoveToRelativeAction mtra = (MoveToRelativeAction) moveAction;
			final GameObject target = mtra.getTarget();
			if (target instanceof Creature)
			{
				return (Creature) target;
			}
		}
		return null;
	}

	protected MoveActionBase createMoveToRelative(final GameObject pawn, final int indent, final int range, final boolean pathfinding)
	{
		return new MoveToRelativeAction(this, pawn, !Config.ALLOW_GEODATA, indent, range, pathfinding);
	}

	protected MoveActionBase createMoveToLocation(final Location dest, final int indent, final boolean pathFind)
	{
		return new MoveToLocationAction(this, getLoc(), dest, isInBoat() || isBoat() || !Config.ALLOW_GEODATA, indent, pathFind);
	}

	public boolean moveToLocation(final Location loc, final int offset, final boolean pathfinding)
	{
		return moveToLocation(loc.x, loc.y, loc.z, offset, pathfinding);
	}

	public boolean moveToLocation(final int toX, final int toY, final int toZ, int indent, final boolean pathfinding)
	{
		if (this.isPhantom()) // задержка на движение фантомам
		{
			if (System.currentTimeMillis()-this.getPlayer().getLastMovePacket() < 100)
				return false;
			this.getPlayer().setLastMovePacket();
		}

		moveLock.lock();
		try
		{
			if (this.isPlayer() && !this.isPhantom()&& this.getPlayer().tScheme_record.isLogging())
				this.getPlayer().tScheme_record.StartMove();

			indent = Math.max(indent, 0);
			final Location worldTo = new Location(toX, toY, toZ);
			final MoveActionBase prevMoveAction = moveAction;
			if (prevMoveAction instanceof MoveToLocationAction && ((MoveToLocationAction) prevMoveAction).isSameDest(worldTo))
			{
				sendActionFailed();
				return false;
			}
			if (isMovementDisabled())
			{
				getAI().setNextAction(PlayableAI.AINextAction.MOVE, new Location(toX, toY, toZ), indent, pathfinding, false);
				sendActionFailed();
				return false;
			}
			getAI().clearNextAction();
			if (isPlayer())
			{
				final Player player = getPlayer();
				getAI().changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
				/*if (Config.ALT_TELEPORT_PROTECTION && isPlayer() && player.getAfterTeleportPortectionTime() > System.currentTimeMillis())
				{
					player.setAfterTeleportPortectionTime(0L);
					player.sendMessage(new CustomMessage("alt.teleport_protect_gonna", player));
				}*/
			}
			stopMove(false, false);
			final MoveActionBase mtla = createMoveToLocation(worldTo, indent, pathfinding);
			moveAction = mtla;
			if (!mtla.start())
			{
				moveAction = null;
				sendActionFailed();
				return false;
			}
			mtla.scheduleNextTick();
			return true;
		}finally
		{
			moveLock.unlock();
		}
	}

	public boolean followToCharacter(Location loc, Creature target, int offset, boolean forestalling)
	{
		return moveToRelative(target,  100, offset, forestalling);
	}

	public boolean moveToRelative(final GameObject pawn, final int indent, final int range)
	{
		return moveToRelative(pawn, indent, range, Config.ALLOW_PAWN_PATHFIND);
	}

	public boolean moveToRelative(final GameObject pawn, int indent, int range, final boolean pathfinding)
	{
		if (this.isPhantom()) // задержка на движение фантомам
		{
			if (System.currentTimeMillis()-this.getPlayer().getLastMovePacket() < 10)
				return false;
			this.getPlayer().setLastMovePacket();
		}

		moveLock.lock();
		try
		{
			if (isMovementDisabled() || pawn == null || isInBoat())
			{
				return false;
			}
			final MoveActionBase prevMoveAction = moveAction;
			if (prevMoveAction instanceof MoveToRelativeAction && !prevMoveAction.isFinished() && ((MoveToRelativeAction) prevMoveAction).isSameTarget(pawn))
			{
				sendActionFailed();
				return false;
			}
			range = Math.max(range, 10);
			indent = Math.min(indent, range);
			getAI().clearNextAction();
			if (isPlayer())
			{
				final Player player = getPlayer();
				/*if (Config.ALT_TELEPORT_PROTECTION && isPlayer() && player.getAfterTeleportPortectionTime() > System.currentTimeMillis())
				{
					player.setAfterTeleportPortectionTime(0L);
					player.sendMessage(new CustomMessage("alt.teleport_protect_gonna", player));
				}*/
			}
			stopMove(false, false);
			final MoveActionBase mtra = createMoveToRelative(pawn, indent, range, pathfinding);
			moveAction = mtra;
			if (!mtra.start())
			{
				moveAction = null;
				sendActionFailed();
				return false;
			}
			mtra.scheduleNextTick();
			return true;
		}finally
		{
			moveLock.unlock();
		}
	}

	protected void broadcastMove()
	{
		validateLocation(isPlayer() ? 2 : 1);
		broadcastPacket(movePacket());
	}

	public void stopMove()
	{
		stopMove(true, true);
	}

	public void stopMove(final boolean validate)
	{
		stopMove(true, validate);
	}

	public void stopMove(final boolean stop, final boolean validate)
	{
		stopMove(stop, validate, true);
	}

	public void stopMove(final boolean stop, final boolean validate, final boolean action)
	{
		if (!isMoving())
		{
			return;
		}
		moveLock.lock();
		try
		{
			if (!isMoving())
			{
				return;
			}
			if (action && moveAction != null && !moveAction.isFinished())
			{
				moveAction.interrupt();
				moveAction = null;
			}
			if (_moveTask != null)
			{
				_moveTask.cancel(false);
				_moveTask = null;
			}

			if (validate)
			{
				validateLocation(isPlayer() ? 2 : 1);
			}
			if (stop)
			{
				broadcastStopMove();
			}
		}finally
		{
			moveLock.unlock();
		}
	}

	public void broadcastStopMove()
	{
		broadcastPacket(stopMovePacket());
	}

	/*protected L2GameServerPacket stopMovePacket()
	{
		return new StopMove(this);
	}*/

	protected L2GameServerPacket stopMovePacket() {
		return new StopMovePacket(this);
	}
	
	public L2GameServerPacket movePacket()
	{
		final MoveActionBase moveAction = this.moveAction;
		if (moveAction != null)
		{
			return moveAction.movePacket();
		}
		//TODO 
		//return new CharMoveToLocation(this);
		return new MTLPacket(this);
	}

	public Location getImpliedTargetLoc(Creature target)
	{
		if (target == null)
			return null;

		final Location loc = target.getLoc();
		if (!target.isMoving())
		{
			return loc;
		}
		return GeoMove.getIntersectPoint(this.getLoc(), loc, target.getMoveSpeed(), Math.max(128, Config.MOVE_TASK_QUANTUM_PC/2)).correctGeoZ();
	}

	//TODO move end
	private PhantomType phantom_type = PhantomType.NONE;

	public PhantomType getPhantomType()
	{
		return phantom_type;
	}

	public void setPhantomType(PhantomType type)
	{
		phantom_type = type;
	}

	public List <Player> getAroundPhantom(int radius, int height)
	{
		if (!isVisible())
			return Collections.emptyList();
		return World.getAroundPhantom(this, radius, height);
	}

	public List <Player> getAroundPlayers(int radius, int height)
	{
		if (!isVisible())
			return Collections.emptyList();
		return World.getAroundPlayers(this, radius, height);
	}

	public List <MonsterInstance>  getAroundMonsters(int radius, int height)
	{
		if (!isVisible())
			return Collections.emptyList();
		return World.getAroundMonsters(this, radius, height);
	}
}
