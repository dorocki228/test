package l2s.gameserver.model;

import com.google.common.flogger.FluentLogger;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import l2s.commons.geometry.Circle;
import l2s.commons.geometry.Shape;
import l2s.commons.lang.reference.HardReference;
import l2s.commons.lang.reference.HardReferences;
import l2s.commons.listener.Listener;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CharacterAI;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.PlayableAI.AINextAction;
import l2s.gameserver.data.xml.holder.LevelBonusHolder;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.data.xml.holder.TransformTemplateHolder;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.geometry.ILocation;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.listener.hooks.ListenerHook;
import l2s.gameserver.listener.hooks.ListenerHookType;
import l2s.gameserver.model.GameObjectTasks.HitTask;
import l2s.gameserver.model.GameObjectTasks.NotifyAITask;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.model.actor.CreatureMovement;
import l2s.gameserver.model.actor.CreatureSkillCast;
import l2s.gameserver.model.actor.MoveType;
import l2s.gameserver.model.actor.basestats.CreatureBaseStats;
import l2s.gameserver.model.actor.flags.CreatureFlags;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.actor.instances.creature.AbnormalList;
import l2s.gameserver.model.actor.instances.player.Cubic;
import l2s.gameserver.model.actor.listener.CharListenerList;
import l2s.gameserver.model.actor.recorder.CharStatsChangeRecorder;
import l2s.gameserver.model.actor.stat.CreatureStat;
import l2s.gameserver.model.base.*;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.model.entity.events.impl.SingleMatchEvent;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.quest.QuestEventType;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.model.reference.L2Reference;
import l2s.gameserver.model.skill.SkillConditionScope;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.components.StatusUpdate;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.*;
import l2s.gameserver.network.l2.s2c.FlyToLocation.FlyType;
import l2s.gameserver.network.l2.s2c.updatetype.IUpdateTypeComponent;
import l2s.gameserver.skills.*;
import l2s.gameserver.stats.BooleanStat;
import l2s.gameserver.stats.DoubleStat;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.stats.StatTemplate;
import l2s.gameserver.stats.triggers.RunnableTrigger;
import l2s.gameserver.stats.triggers.TriggerInfo;
import l2s.gameserver.stats.triggers.TriggerType;
import l2s.gameserver.taskmanager.LazyPrecisionTaskManager;
import l2s.gameserver.taskmanager.RegenTaskManager;
import l2s.gameserver.templates.CreatureTemplate;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.templates.player.transform.TransformTemplate;
import l2s.gameserver.templates.skill.EffectTemplate;
import l2s.gameserver.utils.AbnormalsComparator;
import l2s.gameserver.utils.PositionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.napile.primitive.maps.impl.CTreeIntObjectMap;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static l2s.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;

public abstract class Creature extends GameObject
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	public static final Duration ATTACK_STANCE_DURATION = Duration.ofSeconds(15);

	public static class AbortCastDelayed implements Runnable
	{
		private Creature _cha;
		
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

	public static final double HEADINGS_IN_PI = 10430.378350470452724949566316381;
	public static final int INTERACTION_DISTANCE = 200;

	private Future<?> _stanceTask;
	private Runnable _stanceTaskRunnable;
	private long _stanceEndTime;

	private Future<?> _deleteTask;

	public final static int CLIENT_BAR_SIZE = 352; // 352 - размер полоски CP/HP/MP в клиенте, в пикселях

	private int _lastCpBarUpdate = -1;
	private int _lastHpBarUpdate = -1;
	private int _lastMpBarUpdate = -1;

	protected double _currentCp = 0;
	private double _currentHp = 1;
	protected double _currentMp = 1;
	private final AtomicInteger _previousHpPercent = new AtomicInteger();

	private volatile boolean _isAttackAborted;
	private volatile long _attackEndTime;
	private volatile long _attackReuseEndTime;
	private volatile long _lastAttackTime = -1;

	private int _poleAttackCount = 0;
	private static final double[] POLE_VAMPIRIC_MOD = { 1, 0.9, 0, 7, 0.2, 0.01 };

	/** HashMap(Integer, L2Skill) containing all skills of the L2Character */
	protected final IntObjectMap<SkillEntry> _skills = new CTreeIntObjectMap<SkillEntry>();
	protected Map<TriggerType, Set<TriggerInfo>> _triggers;

	protected IntObjectMap<TimeStamp> _skillReuses = new CHashIntObjectMap<TimeStamp>();

	protected volatile AbnormalList _effectList;

	protected volatile CharStatsChangeRecorder<? extends Creature> _statsRecorder;

	/** Map 32 bits (0x00000000) containing all abnormal effect in progress */
	private Set<AbnormalVisualEffect> abnormalVisualEffects = ConcurrentHashMap.newKeySet();

	private AtomicBoolean isDead = new AtomicBoolean();
	protected AtomicBoolean isTeleporting = new AtomicBoolean();

    private final AtomicBoolean usingFlyingSkill = new AtomicBoolean(false);

	private boolean _isPreserveAbnormal; // Восстанавливает все бафы после смерти
	private boolean _isSalvation; // Восстанавливает все бафы после смерти и полностью CP, MP, HP

	private boolean _meditated;
	private boolean _lockedTarget;

	private boolean _blocked;

	private final Map<EffectHandler, TIntSet> _ignoreSkillsEffects = new HashMap<>();

	private volatile HardReference<? extends Creature> _effectImmunityException = HardReferences.emptyRef();
	private volatile HardReference<? extends Creature> _damageBlockedException = HardReferences.emptyRef();

	private boolean _flying;

	private boolean _running;

	private volatile HardReference<? extends GameObject> _target = HardReferences.emptyRef();
	private volatile HardReference<? extends Creature> _aggressionTarget = HardReferences.emptyRef();

	private int _rndCharges = 0;

	private int _heading;

	private CreatureTemplate _template;

	protected volatile CharacterAI _ai;

	protected String _name;
	protected String _title;
	protected TeamType _team = TeamType.NONE;

	private boolean _isRegenerating;
	private final Lock regenLock = new ReentrantLock();
	private Future<?> _regenTask;
	private Runnable _regenTaskRunnable;

	private List<Zone> _zones = new ArrayList<>();
	/** Блокировка для чтения/записи объектов из региона */
	private final ReadWriteLock zonesLock = new ReentrantReadWriteLock();
	private final Lock zonesRead = zonesLock.readLock();
	private final Lock zonesWrite = zonesLock.writeLock();

	protected volatile CharListenerList listeners;

	private final Lock statusListenersLock = new ReentrantLock();

	protected HardReference<? extends Creature> reference;

	private boolean _isInTransformUpdate = false;
	private TransformTemplate _visualTransform = null;

	private boolean _isDualCastEnable = false;

	private boolean _isTargetable = true;

	protected CreatureBaseStats _baseStats = null;
	protected CreatureStat _stat = null;
	protected CreatureFlags _statuses = null;

	private volatile Map<BasicProperty, BasicPropertyResist> _basicPropertyResists;

	private final CreatureMovement _movement = new CreatureMovement(this);
	private final CreatureSkillCast[] _skillCasts = new CreatureSkillCast[SkillCastingType.VALUES.length];

	public Creature(int objectId, CreatureTemplate template)
	{
		super(objectId);

		_template = template;

		reference = new L2Reference<Creature>(this);

		if(!isPlayer())	// Игрока начинаем хранить после полного рестора.
			GameObjectsStorage.put(this);
	}

	@Override
	public HardReference<? extends Creature> getRef()
	{
		return reference;
	}

	/**
	 * Stops any attack, move or casting actions. Notifies AI about action stopped.
	 */
	public final void stopActions()
	{
		abortAttack(true, false);
		abortCast(true, false);
		getMovement().stopMove();
		// TODO need ?
		// getAI().notifyEvent(CtrlEvent.EVT_ACTION_STOPPED);
	}

	public boolean isAttackAborted()
	{
		return _isAttackAborted;
	}

	public final void abortAttack(boolean force, boolean message)
	{
		if(isAttackingNow())
		{
			if(force)
				_isAttackAborted = true;

			getAI().setIntention(AI_INTENTION_ACTIVE);

			if(isPlayer() && message)
			{
				sendActionFailed();
				sendPacket(new SystemMessage(SystemMessage.C1S_ATTACK_FAILED).addName(this));
			}
		}
	}

	public final void abortCast(boolean force, boolean message, boolean normalCast, boolean dualCast)
	{
		boolean cancelled = false;

		if(normalCast)
		{
			if(getSkillCast(SkillCastingType.NORMAL).abortCast(force))
				cancelled = true;
		}

		if(dualCast)
		{
			if(getSkillCast(SkillCastingType.NORMAL_SECOND).abortCast(force))
				cancelled = true;
		}

		if(cancelled)
		{
			broadcastPacket(new MagicSkillCanceled(getObjectId())); // broadcast packet to stop animations client-side
			// TODO sendPacket(ActionFailPacket.get(skillCast.getCastingType())); // send an "action failed" packet to the caster

			getAI().setIntention(AI_INTENTION_ACTIVE);

			if(isPlayer() && message)
				sendPacket(SystemMsg.YOUR_CASTING_HAS_BEEN_INTERRUPTED);
		}
	}

	public final void abortCast(boolean force, boolean message)
	{
		abortCast(force, message, true, true);
	}

	/**
	 * Break a cast and send Server->Client ActionFailed packet and a System Message to the L2Character.
	 */
	public void breakCast()	{
		// Break only one skill at a time while casting.
		CreatureSkillCast skillCast = getSkillCast(CreatureSkillCast::canAbortCast, CreatureSkillCast::isAnyNormalType);
		if (skillCast == null) {
			return;
		}
		SkillEntry skillEntry = skillCast.getSkillEntry();
		if (skillEntry == null || !skillEntry.getTemplate().isMagic()) {
			return;
		}

		skillCast.abortCast();

		broadcastPacket(new MagicSkillCanceled(getObjectId())); // broadcast packet to stop animations client-side
		sendPacket(ActionFailPacket.get(skillCast.getCastingType())); // send an "action failed" packet to the caster

		getAI().setIntention(AI_INTENTION_ACTIVE);

		if (isPlayer()) {
			// Send a system message
			sendPacket(SystemMsg.YOUR_CASTING_HAS_BEEN_INTERRUPTED);
		}
	}

	// Reworked by Rivelia.
	/*private double reflectDamage(Creature attacker, Skill skill, double damage)
	{
		if(isDead() || damage <= 0 || !attacker.checkRange(attacker, this) || getCurrentHp() + getCurrentCp() <= damage)
			return 0.;

		final boolean bow = attacker.getBaseStats().getAttackType() == WeaponType.BOW || attacker.getBaseStats().getAttackType() == WeaponType.CROSSBOW || attacker.getBaseStats().getAttackType() == WeaponType.TWOHANDCROSSBOW;
		final double resistReflect = 1 - (attacker.getStat().getValue(DoubleStat.RESIST_REFLECT_DAM, 0, null, null) * 0.01);

		double value = 0.;
		double chanceValue = 0.;
		if(skill != null)
		{
			if(skill.isMagic())
			{
				chanceValue = getStat().getValue(DoubleStat.REFLECT_AND_BLOCK_MSKILL_DAMAGE_CHANCE, 0, attacker, skill);
				value = getStat().getValue(DoubleStat.REFLECT_MSKILL_DAMAGE_PERCENT, 0, attacker, skill);
			}
			else if(skill.isPhysic())
			{
				chanceValue = getStat().getValue(DoubleStat.REFLECT_AND_BLOCK_PSKILL_DAMAGE_CHANCE, 0, attacker, skill);
				value = getStat().getValue(DoubleStat.REFLECT_PSKILL_DAMAGE_PERCENT, 0, attacker, skill);
			}
		}
		else
		{
			chanceValue = getStat().getValue(DoubleStat.REFLECT_AND_BLOCK_DAMAGE_CHANCE, 0, attacker, null);
			if(bow)
				value = getStat().getValue(DoubleStat.REFLECT_BOW_DAMAGE_PERCENT, 0, attacker, null);
			else
				value = getStat().getValue(DoubleStat.REFLECT_DAMAGE_PERCENT, 0, attacker, null);
		}

		// If we are not lucky, set back value to 0, otherwise set it equal to damage.
		if(chanceValue > 0 && Rnd.chance(chanceValue))
			chanceValue = damage;
		else
			chanceValue = 0.;

		if(value > 0 || chanceValue > 0)
		{
			value = ((value / 100. * damage) + chanceValue) * resistReflect;
			if(Config.REFLECT_DAMAGE_CAPPED_BY_PDEF)	// @Rivelia. If config is on: reflected damage cannot exceed enemy's P. Def.
			{
				int xPDef = attacker.getPDef(this);
				if(xPDef > 0)
					value = Math.min(value, xPDef);
			}
			return value;
		}
		return 0.;
	}*/

	/*private void absorbDamage(Creature target, Skill skill, double damage)
	{
		if(target.isDead())
			return;

		if(damage <= 0)
			return;

		final boolean bow = getBaseStats().getAttackType() == WeaponType.BOW || getBaseStats().getAttackType() == WeaponType.CROSSBOW || getBaseStats().getAttackType() == WeaponType.TWOHANDCROSSBOW;

		// вампирик
		//damage = (int) (damage - target.getCurrentCp() - target.getCurrentHp()); WTF?

		double absorb = 0;
		if(skill != null)
		{
			if(skill.isMagic())
				absorb = getStat().getValue(DoubleStat.ABSORB_MSKILL_DAMAGE_PERCENT, 0, this, skill);
			else
				absorb = getStat().getValue(DoubleStat.ABSORB_PSKILL_DAMAGE_PERCENT, 0, this, skill);
		}
		else if(skill == null && !bow)
			absorb = getStat().getValue(DoubleStat.ABSORB_DAMAGE_PERCENT, 0, this, null);
		else if(skill == null && bow)
			absorb = getStat().getValue(DoubleStat.ABSORB_BOW_DAMAGE_PERCENT, 0, this, null);

		final double poleMod = POLE_VAMPIRIC_MOD[Math.max(0, Math.min(_poleAttackCount, POLE_VAMPIRIC_MOD.length - 1))];

		absorb = poleMod * absorb;

		final boolean damageBlocked = target.isDamageBlocked(this);
		double limit;
		if(absorb > 0 && !damageBlocked && Rnd.chance(Config.ALT_VAMPIRIC_CHANCE) && !target.isServitor() && !target.isInvulnerable())
		{
			limit = getStat().getValue(DoubleStat.HP_LIMIT, null, null) * getMaxHp() / 100.;
			if(getCurrentHp() < limit)
				setCurrentHp(Math.min(_currentHp + (damage * absorb / 100.), limit), false);
		}

		absorb = poleMod * getStat().getValue(DoubleStat.ABSORB_DAMAGEMP_PERCENT, 0, target, null);
		if(absorb > 0 && !damageBlocked && !target.isServitor() && !target.isInvulnerable())
		{
			limit = getStat().getValue(DoubleStat.MP_LIMIT, null, null) * getMaxMp() / 100.;
			if(getCurrentMp() < limit)
				setCurrentMp(Math.min(_currentMp + damage * absorb / 100., limit));
		}
	}

	public double absorbToEffector(Creature attacker, double damage)
	{
		if(damage == 0)
			return 0;

		double transferToEffectorDam = getStat().getValue(DoubleStat.TRANSFER_TO_EFFECTOR_DAMAGE_PERCENT, 0.);
		if(transferToEffectorDam > 0)
		{
			Collection<Abnormal> abnormals = getAbnormalList().values();
			if(abnormals.isEmpty())
				return damage;

			// TODO: Переписать.
			for(Abnormal abnormal : abnormals)
			{
				for(EffectHandler effect : abnormal.getEffects())
				{
					if(!effect.getName().equalsIgnoreCase("AbsorbDamageToEffector"))
						continue;

					Creature effector = abnormal.getEffector();
					// на мертвого чара, не онлайн игрока - не даем абсорб, и не на самого себя
					if(effector == this || effector.isDead() || !isInRange(effector, 1200))
						return damage;

					Player thisPlayer = getPlayer();
					Player effectorPlayer = effector.getPlayer();
					if(thisPlayer != null && effectorPlayer != null)
					{
						if(thisPlayer != effectorPlayer && (!thisPlayer.isOnline() || !thisPlayer.isInParty() || thisPlayer.getParty() != effectorPlayer.getParty()))
							return damage;
					}
					else
						return damage;

					double transferDamage = (damage * transferToEffectorDam) * .01;
					damage -= transferDamage;

					effector.reduceCurrentHp(transferDamage, effector, null, false, false, !attacker.isPlayable(), false, true, false, true);
				}
			}
		}
		return damage;
	}*/

	public Servitor getServitorForTransfereDamage(double damage)
	{
		return null;
	}

	public double getDamageForTransferToServitor(double damage)
	{
		return 0.;
	}

	public double reduceDamageByMp(Creature attacker, double damage)
	{
		return damage;
	}

    public void setTransferDamageTo(Player val)
    {
    }

    public Player getTransferingDamageTo()
    {
        return null;
    }

    public double transferDamage(Creature attacker, double damage) {
        return damage;
    }

	public SkillEntry addSkill(SkillEntry newSkillEntry)
	{
		if(newSkillEntry == null)
			return null;

		SkillEntry oldSkillEntry = _skills.get(newSkillEntry.getId());
		if(newSkillEntry.equals(oldSkillEntry))
			return oldSkillEntry;

		// Replace oldSkill by newSkill or Add the newSkill
		_skills.put(newSkillEntry.getId(), newSkillEntry);

		Skill newSkill = newSkillEntry.getTemplate();

		if(oldSkillEntry != null)
		{
			Skill oldSkill = oldSkillEntry.getTemplate();
			if(oldSkill.isToggle())
			{
				if(oldSkill.getLevel() > newSkill.getLevel())
					getAbnormalList().stop(oldSkill, false);
			}

			removeTriggers(oldSkill);

			if(oldSkill.isPassive())
			{
				if (oldSkill.checkConditions(SkillConditionScope.PASSIVE, this, this)) {
					for(EffectTemplate et : oldSkill.getEffectTemplates(EffectUseType.NORMAL)) {
						final EffectHandler handler = et.getHandler();
						if (handler.checkPumpConditionImpl(null, this, this)) {
							handler.pumpEnd(null, this, this);
						}
					}
				}
			}

			onRemoveSkill(oldSkillEntry);
		}

		addTriggers(newSkill);

		if(newSkill.isPassive())
		{
			if (newSkill.checkConditions(SkillConditionScope.PASSIVE, this, this)) {
				for (EffectTemplate et : newSkill.getEffectTemplates(EffectUseType.NORMAL)) {
					final EffectHandler handler = et.getHandler();
					if (handler.checkPumpConditionImpl(null, this, this)) {
						handler.pumpStart(null, this, this);
					}
				}
			}
		}

		// If an old skill has been replaced, remove all its Func objects
        getStat().recalculateStats(false);

		onAddSkill(newSkillEntry);

		return oldSkillEntry;
	}

	protected void onAddSkill(SkillEntry skill)
	{
		//
	}

	protected void onRemoveSkill(SkillEntry skillEntry)
	{
		//
	}

	public void altOnMagicUse(Creature aimingTarget, SkillEntry skillEntry)
	{
		if(isAlikeDead() || skillEntry == null)
			return;

		Skill skill = skillEntry.getTemplate();
		int magicId = skill.getDisplayId();
		int level = skill.getDisplayLevel();
		List<Creature> targets = skill.getTargets(this, aimingTarget, skillEntry, true, true, false);
		if(!skill.isNotBroadcastable())
			broadcastPacket(new MagicSkillLaunchedPacket(getObjectId(), magicId, level, targets, SkillCastingType.NORMAL));
		double mpConsume2 = skill.getMpConsume2();
		if(mpConsume2 > 0)
		{
			double mpConsume2WithStats = getStat().getMpConsume(skill);

			if(_currentMp < mpConsume2WithStats)
			{
				sendPacket(SystemMsg.NOT_ENOUGH_MP);
				return;
			}
			reduceCurrentMp(mpConsume2WithStats, null);
		}
		callSkill(aimingTarget, skillEntry, targets, false, false);
	}

	public final void forceUseSkill(SkillEntry skillEntry, Creature target)
	{
		if(skillEntry == null)
			return;

		Skill skill = skillEntry.getTemplate();
		if(target == null)
		{
			target = skill.getAimingTarget(this, getTarget(), skill, true, false, false);
			if(target == null)
				return;
		}

		final List<Creature> targets = skill.getTargets(this, target, skillEntry, true, true, false);

		if(!skill.isNotBroadcastable())
		{
			broadcastPacket(new MagicSkillUse(this, target, skill.getDisplayId(), skill.getDisplayLevel(), 0, 0));
			broadcastPacket(new MagicSkillLaunchedPacket(getObjectId(), skill.getDisplayId(), skill.getDisplayLevel(), targets, SkillCastingType.NORMAL));
		}

		callSkill(target, skillEntry, targets, false, false);
	}

	public void altUseSkill(SkillEntry skillEntry, Creature target)
	{
		if(skillEntry == null)
			return;

		if(isUnActiveSkill(skillEntry.getId()))
			return;

		Skill skill = skillEntry.getTemplate();
		if(isSkillDisabled(skill))
			return;

		if(target == null)
		{
			target = skill.getAimingTarget(this, getTarget(), skill, true, false, false);
			if(target == null)
				return;
		}

		getListeners().onMagicUse(skillEntry, target, true);

		if(!skill.isHandler() && isPlayable())
		{
			if(skill.getItemConsumeId() > 0 && skill.getItemConsume() > 0)
			{
				if(skill.isItemConsumeFromMaster())
				{
					Player master = getPlayer();
					if(master == null || !master.consumeItem(skill.getItemConsumeId(), skill.getItemConsume(), false))
						return;
				}
				else if(!consumeItem(skill.getItemConsumeId(), skill.getItemConsume(), false))
					return;
			}
		}

		if(skill.getReferenceItemId() > 0)
		{
			if(!consumeItemMp(skill.getReferenceItemId(), skill.getReferenceItemMpConsume()))
				return;
		}

		if(skill.getEnergyConsume() > getAgathionEnergy())
			return;

		if(skill.getEnergyConsume() > 0)
			setAgathionEnergy(getAgathionEnergy() - skill.getEnergyConsume());

		long reuseDelay = getStat().getReuseTime(skill);

		if(!skill.isToggle() && !skill.isNotBroadcastable())
		{
			MagicSkillUse msu = new MagicSkillUse(this, target, skill.getDisplayId(), skill.getDisplayLevel(), skill.getHitTime(), reuseDelay);
			msu.setReuseSkillId(skill.getReuseSkillId());
			broadcastPacket(msu);
		}

		disableSkill(skill, reuseDelay);

		altOnMagicUse(target, skillEntry);
	}

	public void sendReuseMessage(Skill skill)
	{}

	public void broadcastPacket(IBroadcastPacket... packets)
	{
		sendPacket(packets);
		broadcastPacketToOthers(packets);
	}

	public void broadcastPacket(List<IBroadcastPacket> packets)
	{
		sendPacket(packets);
		broadcastPacketToOthers(packets);
	}

	public void broadcastPacketToOthers(Function<Player, IBroadcastPacket> packetFunc)
	{
		if(!isVisible() || packetFunc == null)
			return;

		for(Player target : World.getAroundObservers(this))
			target.sendPacket(packetFunc.apply(target));
	}

	public void broadcastPacketToOthers(IBroadcastPacket... packets)
	{
		if(!isVisible() || packets.length == 0)
			return;

		for(Player target : World.getAroundObservers(this))
			target.sendPacket(packets);
	}

	public void broadcastPacketToOthers(List<IBroadcastPacket> packets)
	{
		broadcastPacketToOthers(packets.toArray(new IBroadcastPacket[packets.size()]));
	}

	public void broadcastStatusUpdate()
	{
		if(!needStatusUpdate())
			return;

		broadcastPacket(new StatusUpdate(this, StatusUpdatePacket.UpdateType.DEFAULT, StatusUpdatePacket.CUR_HP, StatusUpdatePacket.MAX_HP, StatusUpdatePacket.CUR_MP, StatusUpdatePacket.MAX_MP));
	}

	public int calcHeading(int x_dest, int y_dest)
	{
		return (int) (Math.atan2(getY() - y_dest, getX() - x_dest) * HEADINGS_IN_PI) + 32768;
	}

	/**
	 * Return the Attack Speed of the L2Character (delay (in milliseconds) before next attack).
	 */
	public int calculateAttackDelay()
	{
		return Formulas.INSTANCE.calculateTimeBetweenAttacks(getPAtkSpd());
	}

	public void callSkill(Creature aimingTarget, SkillEntry skillEntry, List<Creature> targets, boolean useActionSkills, boolean trigger) {
		callSkill(aimingTarget, skillEntry, targets, useActionSkills, trigger, null);
	}

	public void callSkill(Creature aimingTarget, SkillEntry skillEntry, List<Creature> targets, boolean useActionSkills, boolean trigger, Cubic cubic)
	{
		try
		{
			Skill skill = skillEntry.getTemplate();
			if(useActionSkills)
			{
				if(skill.isBad() || skill.isDebuff())
				{
					useTriggers(aimingTarget, TriggerType.OFFENSIVE_SKILL_USE, null, skill, 0);

					if(skill.isMagic())
						useTriggers(aimingTarget, TriggerType.OFFENSIVE_MAGICAL_SKILL_USE, null, skill, 0);
					else if(skill.isPhysic())
						useTriggers(aimingTarget, TriggerType.OFFENSIVE_PHYSICAL_SKILL_USE, null, skill, 0);
				}
				else
				{
					useTriggers(aimingTarget, TriggerType.SUPPORT_SKILL_USE, null, skill, 0);

					if(skill.isMagic())
						useTriggers(aimingTarget, TriggerType.SUPPORT_MAGICAL_SKILL_USE, null, skill, 0);
					else if(skill.isPhysic())
						useTriggers(aimingTarget, TriggerType.SUPPORT_PHYSICAL_SKILL_USE, null, skill, 0);
				}

				useTriggers(this, TriggerType.ON_CAST_SKILL, null, skill, 0);
			}

			final Player player = getPlayer();
			for(Creature target : targets)
			{
				if(target == null)
					continue;

				target.getListeners().onMagicHit(skill, this);

				if(player != null && target.isNpc())
				{
					NpcInstance npc = (NpcInstance) target;
					List<QuestState> ql = player.getQuestsForEvent(npc, QuestEventType.MOB_TARGETED_BY_SKILL);
					if(ql != null)
					{
						for(QuestState qs : ql)
							qs.getQuest().notifySkillUse(npc, skill, qs);
					}
				}
			}

			useTriggers(aimingTarget, TriggerType.ON_END_CAST, null, skill, 0);

			skill.onEndCast(skillEntry, this, targets, cubic);
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "" );
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
				if(triggersOwner != null)
				{
					triggers = ConcurrentHashMap.newKeySet();
					for(TriggerInfo t : triggersOwner.getTriggerList())
					{
						if(t.getType() == type)
							triggers.add(t);
					}
				}
				break;
			case ON_CAST_SKILL:
				if(_triggers != null && _triggers.get(type) != null)
				{
					triggers = ConcurrentHashMap.newKeySet();
					for(TriggerInfo t : _triggers.get(type))
					{
						int skillID = t.getArgs() == null || t.getArgs().isEmpty() ? -1 : Integer.parseInt(t.getArgs());
						if(skillID == - 1 || skillID == owner.getId())
							triggers.add(t);
					}
				}
				break;
			default:
				if(_triggers != null)
					triggers = _triggers.get(type);
				break;
		}

		if(triggers != null && !triggers.isEmpty())
		{
			for(TriggerInfo t : triggers)
			{
				SkillEntry skillEntry = t.getSkill();
				if(skillEntry != null)
				{
					if(!skillEntry.getTemplate().equals(ex))
						useTriggerSkill(target == null ? getTarget() : target, targets, t, owner, damage);
				}
			}
		}
	}

	public void useTriggerSkill(GameObject target, List<Creature> targets, TriggerInfo trigger, Skill owner, double damage)
	{
		SkillEntry skillEntry = trigger.getSkill();
		if(skillEntry == null)
			return;

		/*if(skill.getTargetType() == SkillTargetType.TARGET_SELF && !skill.isTrigger())
			_log.warn("Self trigger skill dont have trigger flag. SKILL ID[" + skill.getId() + "]");*/

		Skill skill = skillEntry.getTemplate();
		Creature aimTarget = skill.getAimingTarget(this, target, skill, true, true, false);
		if(aimTarget != null && trigger.isIncreasing())
		{
			int increasedTriggerLvl = 0;
			for(Abnormal effect : aimTarget.getAbnormalList())
			{
				if(effect.getSkill().getId() != skillEntry.getId())
					continue;

				increasedTriggerLvl = effect.getSkill().getLevel(); //taking the first one only.
				break;
			}

			if(increasedTriggerLvl == 0)
			{
				loop: for(Servitor servitor : aimTarget.getServitors())
				{
					for(Abnormal effect : servitor.getAbnormalList())
					{
						if(effect.getSkill().getId() != skillEntry.getId())
							continue;

						increasedTriggerLvl = effect.getSkill().getLevel(); //taking the first one only.
						break loop;
					}
				}
			}

			if(increasedTriggerLvl > 0)
			{
				Skill newSkill = SkillHolder.getInstance().getSkill(skillEntry.getId(), increasedTriggerLvl + 1);
				if(newSkill != null)
					skillEntry = SkillEntry.makeSkillEntry(skillEntry.getEntryType(), newSkill);
				else
					skillEntry = SkillEntry.makeSkillEntry(skillEntry.getEntryType(), skillEntry.getId(), increasedTriggerLvl);
				skill = skillEntry.getTemplate();
			}
		}

		if(skill.getReuseDelay() > 0 && isSkillDisabled(skill))
			return;

		if(!Rnd.chance(trigger.getChance()))
			return;

		// DS: Для шансовых скиллов с TARGET_SELF и условием "пвп" сам кастер будет являться aimTarget,
		// поэтому в условиях для триггера проверяем реальную цель.
		Creature realTarget = target != null && target.isCreature() ? (Creature) target : null;
		if(trigger.checkCondition(this, realTarget, aimTarget, owner, damage) && skillEntry.checkCondition(this, aimTarget, true, true, true, false, true))
		{
			if(targets == null)
				targets = skill.getTargets(this, aimTarget, skillEntry, false, true, false);

			if(!skill.isNotBroadcastable() && !isCastingNow())
			{
				if(trigger.getType() != TriggerType.IDLE)
				{
					for(Creature cha : targets)
						broadcastPacket(new MagicSkillUse(this, cha, skillEntry.getDisplayId(), skillEntry.getDisplayLevel(), 0, 0));
				}
			}

			callSkill(aimTarget, skillEntry, targets, false, true);
			disableSkill(skill, skill.getReuseDelay());
		}
	}

	private void triggerCancelEffects(TriggerInfo trigger)
	{
		SkillEntry skillEntry = trigger.getSkill();
		if(skillEntry == null)
			return;

		getAbnormalList().stop(skillEntry.getTemplate(), false);
	}

	public boolean checkReflectSkill(Creature attacker, Skill skill)
	{
		if(this == attacker)
			return false;
		if(isDead() || attacker.isDead())
			return false;
		if(!skill.isReflectable())
			return false;
		// Не отражаем, если есть неуязвимость, иначе она может отмениться
		if(isInvulnerable() || attacker.isInvulnerable() || !skill.isDebuff())
			return false;
		if(Formulas.INSTANCE.calcBuffDebuffReflection(attacker, skill))
		{
			sendPacket(new SystemMessage(SystemMessage.YOU_COUNTERED_C1S_ATTACK).addName(attacker));
			attacker.sendPacket(new SystemMessage(SystemMessage.C1_DODGES_THE_ATTACK).addName(this));
			return true;
		}
		return false;
	}

	public boolean checkReflectDebuff(Creature effector, Skill skill)
	{
		if(this == effector)
			return false;
		if(isDead() || effector.isDead())
			return false;
		if(effector.isTrap())
			return false;
		if(effector.isRaid()) // Тестово. Сверить с оффом.
			return false;
		if(!skill.isReflectable())
			return false;
		// Не отражаем, если есть неуязвимость, иначе она может отмениться
		if(isInvulnerable() || effector.isInvulnerable() || !skill.isDebuff())
			return false;
		if(isDebuffImmune())
			return false;
		return Rnd.chance(getStat().getValue(skill.isMagic() ? DoubleStat.REFLECT_SKILL_MAGIC : DoubleStat.REFLECT_SKILL_PHYSIC));
	}

	/**
	 * Disable this skill id for the duration of the delay in milliseconds.
	 *
	 * @param skill
	 * @param delay (seconds * 1000)
	 */
	public void disableSkill(Skill skill, long delay)
	{
		_skillReuses.put(skill.getReuseHash(), new TimeStamp(skill, delay));
	}

	public abstract boolean isAutoAttackable(Creature attacker);

	public void doAttack(Creature target)
	{
		if (target == null || isAMuted() || isAttackingNow() || isAlikeDead() || target.isDead() || !isInRange(target, 2000)) //why alikeDead?
		{
			sendActionFailed();
			return;
		}

		if (isTransformed() && !getTransform().isNormalAttackable()) {
			sendActionFailed();
			return;
		}

		getListeners().onAttack(target);

		int ssGrade = 0;
		int attackReuseDelay = 0;
		boolean ssEnabled = false;

		WeaponTemplate weaponItem = getActiveWeaponTemplate();
		boolean isTwoHanded = (weaponItem != null) && (weaponItem.getBodyPart() == ItemTemplate.SLOT_LR_HAND);
		// Get the Attack Speed of the L2Character (delay (in milliseconds) before next attack)
		int timeAtk = calculateAttackDelay();
		int timeToHit = Formulas.INSTANCE.calculateTimeToHit(timeAtk, getAttackType(), isTwoHanded, false);
		_attackEndTime = TimeUnit.MILLISECONDS.toNanos(timeAtk) + System.nanoTime();

		if(isNpc())
		{
			attackReuseDelay = ((NpcTemplate) getTemplate()).getBaseReuseDelay();
			NpcTemplate.ShotsType shotType = ((NpcTemplate) getTemplate()).getShots();
			if(shotType != NpcTemplate.ShotsType.NONE && shotType != NpcTemplate.ShotsType.BSPIRIT && shotType != NpcTemplate.ShotsType.SPIRIT)
				ssEnabled = true;
		}
		else
		{
			if(weaponItem != null)
			{
				attackReuseDelay = weaponItem.getAttackReuseDelay();
				ssGrade = weaponItem.getGrade().extOrdinal();
			}
			ssEnabled = getChargedSoulshotPower() > 0;
		}

		if(attackReuseDelay > 0)
		{
			int reuse = (500000 + (333 * attackReuseDelay)) / getPAtkSpd();
			if(reuse > 0)
			{
				_attackReuseEndTime = TimeUnit.MILLISECONDS.toNanos(reuse) + System.nanoTime();
				sendPacket(new SetupGaugePacket(this, SetupGaugePacket.Colors.RED, reuse));
				if(reuse > timeAtk)
					ThreadPoolManager.getInstance().schedule(new NotifyAITask(this, CtrlEvent.EVT_READY_TO_ACT), reuse);
			}
		}

		_isAttackAborted = false;
		_lastAttackTime = System.currentTimeMillis();

		AttackPacket attack = new AttackPacket(this, target, ssEnabled, ssGrade);

		setHeading(PositionUtils.calculateHeadingFrom(this, target), true);

		switch(getAttackType())
		{
			case BOW:
			case CROSSBOW:
			case TWOHANDCROSSBOW:
				doAttackHitByBow(attack, target, timeToHit, timeAtk);
				break;
			case POLE:
				doAttackHitByPole(attack, target, timeToHit, timeAtk);
				break;
			case DUAL:
			case DUALFIST:
			case DUALDAGGER:
			case DUALBLUNT: {
				int timeToHit2 = Formulas.INSTANCE.calculateTimeToHit(timeAtk, getAttackType(), isTwoHanded, true) - timeToHit;
				doAttackHitByDual(attack, target, timeToHit, timeToHit2, timeAtk);
				break;
			}
			default:
				doAttackHitSimple(attack, target, timeToHit, timeAtk);
				break;
		}

		if(attack.hasHits())
			broadcastPacket(attack);
	}

	private void doAttackHitSimple(AttackPacket attack, Creature target, int hitTime, int attackTime)
	{
		// H5 Changes: without Polearm Mastery (skill 216) max simultaneous attacks is 3 (1 by default + 2 in skill 3599).
		int attackCountMax = (int) getStat().getValue(DoubleStat.ATTACK_COUNT_MAX, 1);
		if(attackCountMax > 1 && !isInPeaceZone())// Гварды с пикой, будут атаковать только одиночные цели в городе
		{
			int range = getPhysicalAttackRadius();

			int attackedCount = 0;

			for(Creature t : getAroundCharacters(range, 200))
			{
				if(attackedCount <= attackCountMax)
				{
					if(t == target || t.isDead())
						continue;

					// @Rivelia. Pole should not hit targets that are flagged if we are not flagged.
					if(t.isAutoAttackable(this) && ((this.getPvpFlag() == 0 && t.getPvpFlag() == 0) || this.getPvpFlag() != 0))
					{
						doAttackHitSimple0(attack, t, false, false, hitTime, attackTime, false);
						attackedCount++;
					}
				}
				else
					break;
			}
		}

		doAttackHitSimple0(attack, target, false, true, hitTime, attackTime, true);
	}

	private void doAttackHitSimple0(AttackPacket attack, Creature target, boolean halfDamage, boolean unchargeSS,
									int hitTime, int attackTime, boolean notify)
	{
		int damage = 0;
		byte shld = 0;
		boolean crit = false;
		boolean miss = Formulas.INSTANCE.calcHitMiss(this, target);
		int elementalDamage = 0;
		boolean elementalCrit = false;

		if(!miss)
		{
			shld = Formulas.INSTANCE.calcShldUse(this, target, true);
			crit = Formulas.INSTANCE.calcCrit(getStat().getCriticalHit(), this, target, null);
			damage = (int) Formulas.INSTANCE.calcAutoAttackDamage(this, target, shld, crit, attack._soulshot);
			elementalCrit = Formulas.INSTANCE.calcElementalCrit(this);
			elementalDamage = (int) Formulas.INSTANCE.calcElementalDamage(this, target, null, elementalCrit, attack._soulshot);
			damage += elementalDamage;
			if (halfDamage)
			{
				damage /= 2.0;
			}
		}

		ThreadPoolManager.getInstance().schedule(new HitTask(this, target, damage, crit, miss,
				attack._soulshot, shld, unchargeSS, notify, attackTime, hitTime, elementalDamage, elementalCrit), hitTime);

		attack.addHit(target, damage, miss, crit, shld);
	}

	private void doAttackHitByBow(AttackPacket attack, Creature target, int hitTime, int attackTime)
	{
		int damage = 0;
		byte shld = 0;
		boolean crit = false;
		int elementalDamage = 0;
		boolean elementalCrit = false;

		// Calculate if hit is missed or not
		boolean miss = Formulas.INSTANCE.calcHitMiss(this, target);

		reduceArrowCount();

		if(!miss)
		{
			shld = Formulas.INSTANCE.calcShldUse(this, target, true);
			crit = Formulas.INSTANCE.calcCrit(getStat().getCriticalHit(), this, target, null);
			damage = (int) Formulas.INSTANCE.calcAutoAttackDamage(this, target, shld, crit, attack._soulshot);
			elementalCrit = Formulas.INSTANCE.calcElementalCrit(this);
			elementalDamage = (int) Formulas.INSTANCE.calcElementalDamage(this, target, null, elementalCrit, attack._soulshot);
			damage += elementalDamage;

			/* В Lindvior атака теперь не зависит от расстояния.
			int range = getPhysicalAttackRange();
			damage *= Math.min(range, getDistance(target)) / range * .4 + 0.8; // разброс 20% в обе стороны
			*/
		}

		ThreadPoolManager.getInstance().schedule(new HitTask(this, target, damage, crit, miss,
				attack._soulshot, shld, true, true, attackTime, hitTime,
				elementalDamage, elementalCrit), hitTime);

		attack.addHit(target, damage, miss, crit, shld);
	}

	private void doAttackHitByDual(AttackPacket attack, Creature target, int hitTime, int hitTime2, int attackTime)
	{
		int damage1 = 0;
		int damage2 = 0;
		byte shld1 = 0;
		byte shld2 = 0;
		boolean crit1 = false;
		boolean crit2 = false;
		int elementalDamage1 = 0;
		int elementalDamage2 = 0;
		boolean elementalCrit1 = false;
		boolean elementalCrit2 = false;

		boolean miss1 = Formulas.INSTANCE.calcHitMiss(this, target);
		boolean miss2 = Formulas.INSTANCE.calcHitMiss(this, target);

		if(!miss1)
		{
			shld1 = Formulas.INSTANCE.calcShldUse(this, target, true);
			crit1 = Formulas.INSTANCE.calcCrit(getStat().getCriticalHit(), this, target, null);
			damage1 = (int) Formulas.INSTANCE.calcAutoAttackDamage(this, target, shld1, crit1, attack._soulshot);
			elementalCrit1 = Formulas.INSTANCE.calcElementalCrit(this);
			elementalDamage1 = (int) Formulas.INSTANCE.calcElementalDamage(this, target, null, elementalCrit1, attack._soulshot);
			damage1 += elementalDamage1;
			damage1 /= 2.0;
		}

		if(!miss2)
		{
			shld2 = Formulas.INSTANCE.calcShldUse(this, target, true);
			crit2 = Formulas.INSTANCE.calcCrit(getStat().getCriticalHit(), this, target, null);
			damage2 = (int) Formulas.INSTANCE.calcAutoAttackDamage(this, target, shld2, crit2, attack._soulshot);
			elementalCrit2 = Formulas.INSTANCE.calcElementalCrit(this);
			elementalDamage2 = (int) Formulas.INSTANCE.calcElementalDamage(this, target, null, elementalCrit2, attack._soulshot);
			damage2 += elementalDamage2;
			damage2 /= 2.0;
		}

		// Create a new hit task with Medium priority for hit 1 and for hit 2 with a higher delay
		ThreadPoolManager.getInstance().schedule(new HitTask(this, target, damage1, crit1, miss1,
				attack._soulshot, shld1, true, false, attackTime, hitTime,
				elementalDamage1, elementalCrit1), hitTime);
		ThreadPoolManager.getInstance().schedule(new HitTask(this, target, damage2, crit2, miss2,
				attack._soulshot, shld2, false, true, attackTime, hitTime,
				elementalDamage2, elementalCrit2), hitTime + hitTime2);

		attack.addHit(target, damage1, miss1, crit1, shld1);
		attack.addHit(target, damage2, miss2, crit2, shld2);
	}

	private void doAttackHitByPole(AttackPacket attack, Creature target, int hitTime, int attackTime)
	{
		// H5 Changes: without Polearm Mastery (skill 216) max simultaneous attacks is 3 (1 by default + 2 in skill 3599).
		int attackCountMax = (int) getStat().getValue(DoubleStat.ATTACK_COUNT_MAX, 1);

		if(isBoss())
			attackCountMax += 27;
		else if(isRaid())
			attackCountMax += 12;
		else if(isMonster())
			attackCountMax += getLevel() / 7.5;

		if (getStat().has(BooleanStat.PHYSICAL_POLEARM_TARGET_SINGLE)) {
			attackCountMax = 1;
		}

		if(attackCountMax > 1 && !isInPeaceZone())// Гварды с пикой, будут атаковать только одиночные цели в городе
		{
			int angle = getPhysicalAttackAngle(); // TODO: Вынести в датапак.
			int range = getPhysicalAttackRange() + getPhysicalAttackRadius();

			_poleAttackCount = 0;

			for(Creature t : getAroundCharacters(range, 200))
			{
				if(_poleAttackCount <= attackCountMax)
				{
					if(t == target || t.isDead())
						continue;

					// should dont work like this in classic ?
					if(!PositionUtils.isFacing(this, t, angle))
						continue;

					// @Rivelia. Pole should not hit targets that are flagged if we are not flagged.
					if(t.isAutoAttackable(this) && ((this.getPvpFlag() == 0 && t.getPvpFlag() == 0) || this.getPvpFlag() != 0))
					{
						doAttackHitSimple0(attack, t, false, false, hitTime, attackTime, false);
						_poleAttackCount++;
					}
				}
				else
					break;
			}

			_poleAttackCount = 0;
		}

		doAttackHitSimple0(attack, target, false, true, hitTime, attackTime, true);
	}

	public boolean doCast(SkillEntry skillEntry, Creature target, boolean forceUse)
	{
		if(getSkillCast(SkillCastingType.NORMAL).doCast(skillEntry, target, forceUse))	// Обычный каст
			return true;
		return getSkillCast(SkillCastingType.NORMAL_SECOND).doCast(skillEntry, target, forceUse);	// Дуал каст
	}

	public Location getFlyLocation(GameObject target,
								   FlyType flyType,
								   boolean isFlyDependsOnHeading,
								   int flyPositionDegree,
								   int flyRadius)
	{
		if(target != null && target != this)
		{
			Location loc;

			int heading = target.getHeading();
			if(!isFlyDependsOnHeading)
				heading = PositionUtils.calculateHeadingFrom(target, this);

			double radian = PositionUtils.convertHeadingToDegree(heading) + flyPositionDegree;
			if(radian > 360)
				radian -= 360;

			radian = (Math.PI * radian) / 180;

			loc = new Location(target.getX() + (int) (Math.cos(radian) * 40), target.getY() + (int) (Math.sin(radian) * 40), target.getZ());

			if(isFlying())
			{
				if(isInFlyingTransform() && ((loc.z <= 0) || (loc.z >= 6000)))
					return null;
				if(GeoEngine.moveCheckInAir(this, loc.x, loc.y, loc.z) == null)
					return null;
			}
			else
			{
				loc.correctGeoZ(getGeoIndex());

				if(!GeoEngine.canMoveToCoord(getX(), getY(), getZ(), loc.x, loc.y, loc.z, getGeoIndex()))
				{
					loc = target.getLoc(); // Если не получается встать рядом с объектом, пробуем встать прямо в него
					if(!GeoEngine.canMoveToCoord(getX(), getY(), getZ(), loc.x, loc.y, loc.z, getGeoIndex()))
						return null;
				}
			}

			return loc;
		}

		int x1 = 0;
		int y1 = 0;
		int z1 = 0;

		if(flyType == FlyType.THROW_UP)
		{
			x1 = 0;
			y1 = 0;
			z1 = getZ() + flyRadius;
		}
		else
		{
			double radian = PositionUtils.convertHeadingToRadian(getHeading());
			x1 = -(int) (Math.sin(radian) * flyRadius);
			y1 = (int) (Math.cos(radian) * flyRadius);
		}

		if(isFlying())
			return GeoEngine.moveCheckInAir(this, getX() + x1, getY() + y1, getZ() + z1);
		return GeoEngine.moveCheck(getX(), getY(), getZ(), getX() + x1, getY() + y1, getGeoIndex());
	}

	public final void doDie(Creature killer)
	{
		// killing is only possible one time
		if(!isDead.compareAndSet(false, true))
			return;

		onDeath(killer);
	}

	protected void onDeath(Creature killer)
	{
		LostItems dropItems = LostItems.Companion.getEMPTY();

        Player player = getPlayer();
		if(killer != null)
		{
			Player killerPlayer = killer.getPlayer();
			if(killerPlayer != null) {
				if (isPlayer()) {
					boolean checkPvp = getEvents(SingleMatchEvent.class).stream()
                            .allMatch(event -> event.canIncreasePvPPKCounter(killerPlayer, player));

					if (checkPvp) {
						dropItems = doPKPVPManage(killer);
						altDeathPenalty(killer);
					}
				}

				killerPlayer.getListeners().onKillIgnorePetOrSummon(this);
			}

			killer.getListeners().onKill(this);

			if(isPlayer() && killer.isPlayable())
				_currentCp = 0;
		}

		setTarget(null);

		abortCast(true, false);
		abortAttack(true, false);

		getMovement().stopMove();
		stopAttackStanceTask();
		stopRegeneration();

		_currentHp = 0;

        if (isPlayable() && player != null && !player.isInPvPEvent()) {
			final TIntSet effectsToRemove = new TIntHashSet();

			// Stop all active skills effects in progress on the L2Character
			if(isPreserveAbnormal() || isResurrectionSpecial())
			{
                if (isResurrectionSpecial() && isPlayer() && !player.isInOlympiadMode())
                    player.reviveRequest(player, 100, false);

				// TODO rework
				for(Abnormal abnormal : getAbnormalList())
				{
					int skillId = abnormal.getId();
					if(skillId == Skill.SKILL_RAID_BLESSING)
						effectsToRemove.add(skillId);
					else
					{
						for(EffectHandler effect : abnormal.getEffects())
						{
							// Noblesse Blessing Buff/debuff effects are retained after
							// death. However, Noblesse Blessing and Lucky Charm are lost as normal.
							if(effect.getName().equalsIgnoreCase("p_preserve_abnormal"))
								effectsToRemove.add(skillId);
							else if(effect.getName().equalsIgnoreCase("AgathionResurrect"))
							{
								if(isPlayer())
                                    player.setAgathionRes(true);
								effectsToRemove.add(skillId);
							}
						}
					}
				}
			}
			else
			{
				for(Abnormal abnormal : getAbnormalList())
				{
					// Некоторые эффекты сохраняются при смерти
					if(!abnormal.getSkill().isPreservedOnDeath())
						effectsToRemove.add(abnormal.getSkill().getId());
				}
				deleteCubics(); // TODO: Проверить, должно ли Благословение Дворянина влиять на кубики.
			}

			getAbnormalList().stop(effectsToRemove);
		}

		if(isPlayer())
            player.sendUserInfo(true); // Принудительно посылаем, исправляет баг, когда персонаж умирает в воздушных оковах.

		broadcastStatusUpdate();

		ThreadPoolManager.getInstance().execute(new NotifyAITask(this, CtrlEvent.EVT_DEAD, killer, dropItems, null));

		if(killer != null)
			killer.useTriggers(this, TriggerType.ON_KILL, null, null, 0);

		getListeners().onDeath(killer);
	}

	protected LostItems doPKPVPManage(Creature killer)
	{
		return LostItems.Companion.getEMPTY();
	}

	protected void altDeathPenalty(final Creature killer)
	{
	}

	protected void onRevive()
	{
		getListeners().onRevive();

		useTriggers(this, TriggerType.ON_REVIVE, null, null, 0);

		if (isResurrectionSpecial()) {
			getAbnormalList().stop(AbnormalType.RESURRECTION_SPECIAL);
		}
	}

	public void enableSkill(Skill skill)
	{
		_skillReuses.remove(skill.getReuseHash());
	}

	/**
	 * Return a map of 32 bits (0x00000000) containing all abnormal effects
	 */
	public Set<AbnormalVisualEffect> getAbnormalEffects()
	{
		return abnormalVisualEffects;
	}

	public AbnormalVisualEffect[] getAbnormalEffectsArray()
	{
		return abnormalVisualEffects.toArray(new AbnormalVisualEffect[abnormalVisualEffects.size()]);
	}

	public int getPAccuracy()
	{
		return (int) getStat().getValue(DoubleStat.ACCURACY_COMBAT);
	}

	public int getMAccuracy()
	{
		return (int) getStat().getValue(DoubleStat.ACCURACY_MAGIC);
	}

	/**
	 * Возвращает коллекцию скиллов для быстрого перебора
	 */
	public Collection<SkillEntry> getAllSkills()
	{
		return _skills.valueCollection();
	}

	/**
	 * Возвращает массив скиллов для безопасного перебора
	 */
	public final SkillEntry[] getAllSkillsArray()
	{
		return _skills.values(new SkillEntry[_skills.size()]);
	}

	public final double getAttackSpeedMultiplier()
	{
		return getStat().getAttackSpeedMultiplier();
	}

	/**
	 * Возвращает шанс физического крита (1000 == 100%)
	 */
	public int getPCriticalHit(Creature target)
	{
		return (int) getStat().getValue(DoubleStat.CRITICAL_RATE);
	}

	/**
	 * Возвращает шанс магического крита (1000 == 100%)
	 */
	public int getMCriticalHit(Creature target, Skill skill)
	{
		return (int) getStat().getValue(DoubleStat.MAGIC_CRITICAL_RATE);
	}

	/**
	 * Return the current CP of the L2Character.
	 *
	 */
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
		return getCurrentCpRatio() * 100.;
	}

	public final boolean isCurrentCpFull()
	{
		return getCurrentCp() >= getMaxCp();
	}

	public final boolean isCurrentCpZero()
	{
		return getCurrentCp() < 1;
	}

	public double getCurrentHp()
	{
		return _currentHp;
	}

	public final double getCurrentHpRatio()
	{
		return getCurrentHp() / getStat().getMaxHp();
	}

	public final double getCurrentHpPercents()
	{
		return getCurrentHpRatio() * 100.;
	}

	public final boolean isCurrentHpFull()
	{
		return getCurrentHp() >= getStat().getMaxHp();
	}

	public final boolean isCurrentHpZero()
	{
		return getCurrentHp() < 1;
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
		return getCurrentMpRatio() * 100.;
	}

	public final boolean isCurrentMpFull()
	{
		return getCurrentMp() >= getMaxMp();
	}

	public final boolean isCurrentMpZero()
	{
		return getCurrentMp() < 1;
	}

	public int getINT()
	{
		return (int) getStat().getValue(DoubleStat.STAT_INT);
	}

	public int getSTR()
	{
		return (int) getStat().getValue(DoubleStat.STAT_STR);
	}

	public int getCON()
	{
		return (int) getStat().getValue(DoubleStat.STAT_CON);
	}

	public int getMEN()
	{
		return (int) getStat().getValue(DoubleStat.STAT_MEN);
	}

	public int getDEX()
	{
		return (int) getStat().getValue(DoubleStat.STAT_DEX);
	}

	public int getWIT()
	{
		return (int) getStat().getValue(DoubleStat.STAT_WIT);
	}

	public int getPEvasionRate(Creature target)
	{
		return (int) getStat().getValue(DoubleStat.EVASION_RATE);
	}

	public int getMEvasionRate(Creature target)
	{
		return (int) getStat().getValue(DoubleStat.MAGIC_EVASION_RATE);
	}

	public List<Creature> getAroundCharacters(int radius, int height)
	{
		if(!isVisible())
			return Collections.emptyList();
		return World.getAroundCharacters(this, radius, height);
	}

	public Stream<Creature> getAroundCharacters(int radius, int height, Predicate<GameObject> predicate)
	{
		if(!isVisible())
			return Stream.empty();
		return World.getAroundCharacters(this, radius, height, predicate);
	}

	public List<Playable> getAroundPlayables(int radius, int height)
	{
		if(!isVisible())
			return Collections.emptyList();
		return World.getAroundPlayables(this, radius, height);
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

	public final int getMagicalAttackRange(Skill skill)
	{
		if(skill != null)
			return (int) getStat().getValue(DoubleStat.MAGIC_ATTACK_RANGE, skill.getCastRange());
		return getTemplate().getBaseAtkRange().orElse(0.0).intValue();
	}

	public int getMAtk(Creature target, Skill skill)
	{
		return (int) getStat().getValue(DoubleStat.MAGICAL_ATTACK);
	}

	public int getMAtkSpd()
	{
		return (int) getStat().getValue(DoubleStat.MAGICAL_ATTACK_SPEED);
	}

	public int getMaxHp()
	{
		return (int) getStat().getValue(DoubleStat.MAX_HP);
	}

	public int getMaxMp()
	{
		return (int) getStat().getValue(DoubleStat.MAX_MP);
	}

	public int getMaxCp()
	{
		return (int) getStat().getValue(DoubleStat.MAX_CP);
	}

	public int getMDef(Creature target, Skill skill)
	{
		return (int) getStat().getValue(DoubleStat.MAGICAL_DEFENCE);
	}

	public double getMinDistance(GameObject obj)
	{
		double distance = getCurrentCollisionRadius();

		if(obj != null && obj.isCreature())
			distance += obj.getCurrentCollisionRadius();

		return distance;
	}

	@Override
	public String getName()
	{
		return StringUtils.defaultString(_name);
	}

	public String getVisibleName(Player receiver)
	{
		return getName();
	}

	public int getPAtk(Creature target)
	{
		return (int) getStat().getValue(DoubleStat.PHYSICAL_ATTACK);
	}

	public int getPAtkSpd()
	{
		return (int) getStat().getValue(DoubleStat.PHYSICAL_ATTACK_SPEED);
	}

	public int getPDef(Creature target)
	{
		return (int) getStat().getValue(DoubleStat.PHYSICAL_DEFENCE);
	}

	public int getPhysicalAttackRange()
	{
		return (int) getStat().getValue(DoubleStat.PHYSICAL_ATTACK_RANGE);
	}

	public int getPhysicalAttackRadius()
	{
		return (int) getStat().getValue(DoubleStat.PHYSICAL_ATTACK_RADIUS);
	}

	public int getPhysicalAttackAngle()
	{
		return (int) getStat().getValue(DoubleStat.PHYSICAL_ATTACK_ANGLE);
	}

	/**
	 * @return a multiplier based on weapon random damage
	 */
	public final double getRandomDamageMultiplier()
	{
		final int random = (int) getStat().getValue(DoubleStat.RANDOM_DAMAGE);
		return (1 + ((double) Rnd.get(-random, random) / 100));
	}

	public double getWeaponReuseModifier(Creature target)
	{
		return getStat().getValue(DoubleStat.ATK_REUSE, 1.0);
	}

	public final int getShldDef()
	{
		return (int) getStat().getValue(DoubleStat.SHIELD_DEFENCE);
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
		return _target.get();
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

	public String getVisibleTitle(Player receiver)
	{
		return getTitle();
	}

	public double headingToRadians(int heading)
	{
		return (heading - 32768) / HEADINGS_IN_PI;
	}

	public final boolean isAlikeDead()
	{
		return isFakeDeath() || isDead();
	}

	public final boolean isAttackingNow()
	{
		return _attackEndTime > System.nanoTime();
	}

	public final long getLastAttackTime()
	{
		return _lastAttackTime;
	}

	public final void setLastAttackTime(long value)
	{
		_lastAttackTime = value;
	}

	public final boolean isPreserveAbnormal()
	{
		return _isPreserveAbnormal || getStat().has(BooleanStat.PRESERVE_ABNORMAL);
	}

	public final boolean isResurrectionSpecial()
	{
		return getStat().has(BooleanStat.RESURRECTION_SPECIAL);
	}

	public boolean isEffectImmune(Creature effector)
	{
		Creature exception = _effectImmunityException.get();
		if(exception != null && exception == effector)
			return false;

		return getFlags().getEffectImmunity().get();
	}

	public boolean isBuffImmune()
	{
		return getStat().has(BooleanStat.BLOCK_BUFF);
	}

	public boolean isDebuffImmune()
	{
		return getStat().has(BooleanStat.BLOCK_DEBUFF) || isPeaceNpc() || isInvulnerable();
	}

	public boolean cannotEscape()
	{
		return getStat().has(BooleanStat.BLOCK_ESCAPE);
	}

	public boolean isResurrectionBlocked()
	{
		return getStat().has(BooleanStat.BLOCK_RESURRECTION);
	}

	/**
	 * For Newbie Protection Blessing skill, keeps you safe from an attack by a chaotic character >= 10 levels apart from you.
	 * @return
	 */
	public final boolean isProtectionBlessingAffected()
	{
		return getStat().has(BooleanStat.PROTECTION_BLESSING);
	}

	public boolean isDead()
	{
		return _currentHp < 0.5 || isDead.get();
	}

	@Override
	public boolean isFlying()
	{
		return _flying;
	}

	/**
	 * Находится ли персонаж в боевой позе
	 * @return true, если персонаж в боевой позе, атакован или атакует
	 */
	public final boolean isInCombat()
	{
		return System.nanoTime() < _stanceEndTime;
	}

	public boolean isMageClass()
	{
		return getStat().getMAtk() > 3;
	}

	public final boolean isRunning()
	{
		return _running;
	}

	public boolean isSkillDisabled(Skill skill)
	{
		if (getFlags().hasBlockActions() || getFlags().isAllSkillsDisabled() || (getStat().has(BooleanStat.BLOCK_ACTIONS) && !getStat().isBlockedActionsAllowedSkill(skill)))
		{
			return true;
		}

		TimeStamp sts = _skillReuses.get(skill.getReuseHash());
		if(sts == null)
			return false;
		if(sts.hasNotPassed())
			return true;
		_skillReuses.remove(skill.getReuseHash());
		return false;
	}

	public final boolean isTeleporting()
	{
		return isTeleporting.get();
	}

    public final boolean isUsingFlyingSkill() {
        return usingFlyingSkill.get();
    }

	public void broadcastMove()
	{
		broadcastPacket(movePacket());
	}

	public void broadcastStopMove()
	{
		broadcastPacket(stopMovePacket());
	}

	/** Возвращает координаты поверхности воды, если мы находимся в ней, или над ней. */
	public int[] getWaterZ()
	{
		int[] waterZ = new int[]{ Integer.MIN_VALUE, Integer.MAX_VALUE };
		if(!isInWater())
			return waterZ;

		zonesRead.lock();
		try
		{
			Zone zone;
			for(int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
				if(zone.getType() == ZoneType.water)
				{
					if(waterZ[0] == Integer.MIN_VALUE || waterZ[0] > zone.getTerritory().getZmin())
						waterZ[0] = zone.getTerritory().getZmin();
					if(waterZ[1] == Integer.MAX_VALUE || waterZ[1] < zone.getTerritory().getZmax())
						waterZ[1] = zone.getTerritory().getZmax();
				}
			}
		}
		finally
		{
			zonesRead.unlock();
		}
		return waterZ;
	}

	protected IClientOutgoingPacket stopMovePacket()
	{
		return new StopMovePacket(this);
	}

	public IClientOutgoingPacket movePacket()
	{
		if (getMovement().isFollow() && !getMovement().isPathfindMoving())
		{
			Creature target = getMovement().getFollowTarget();
			if (target != null) {
				return new MoveToPawnPacket(this, target, getMovement().getMoveOffset());
			}
		}
		return new MoveToLocation(this);
	}

	public void updateZones()
	{
		if(isTeleporting())
			return;

		Zone[] zones = isVisible() ? getCurrentRegion().getZones() : Zone.EMPTY_L2ZONE_ARRAY;

		List<Zone> entering = null;
		List<Zone> leaving = null;

		Zone zone;

		zonesWrite.lock();
		try
		{
			if(!_zones.isEmpty())
			{
				leaving = new ArrayList<>();
				for(int i = 0; i < _zones.size(); i++)
				{
					zone = _zones.get(i);
					// зоны больше нет в регионе, либо вышли за территорию зоны
					if(!ArrayUtils.contains(zones, zone) || !zone.checkIfInZone(getX(), getY(), getZ(), getReflection()))
						leaving.add(zone);
				}

				//Покинули зоны, убираем из списка зон персонажа
				if(!leaving.isEmpty())
				{
					for(int i = 0; i < leaving.size(); i++)
					{
						zone = leaving.get(i);
						_zones.remove(zone);
					}
				}
			}

			if(zones.length > 0)
			{
				entering = new ArrayList<>();
				for(int i = 0; i < zones.length; i++)
				{
					zone = zones[i];
					// в зону еще не заходили и зашли на территорию зоны
					if(!_zones.contains(zone) && zone.checkIfInZone(getX(), getY(), getZ(), getReflection()))
						entering.add(zone);
				}

				//Вошли в зоны, добавим в список зон персонажа
				if(!entering.isEmpty())
				{
					for(int i = 0; i < entering.size(); i++)
					{
						zone = entering.get(i);
						_zones.add(zone);
					}
				}
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
		Zone zone;

		if(leaving != null && !leaving.isEmpty())
		{
			for(int i = 0; i < leaving.size(); i++)
			{
				zone = leaving.get(i);
				zone.doLeave(this);
			}
		}

		if(entering != null && !entering.isEmpty())
		{
			for(int i = 0; i < entering.size(); i++)
			{
				zone = entering.get(i);
				zone.doEnter(this);
			}
		}
	}

	public boolean isInPeaceZone()
	{
		return isInZone(ZoneType.peace_zone) && !isInZoneBattle();
	}

	public boolean isInZoneBattle()
	{
		for(Event event : getEvents())
		{
			Boolean result = event.isInZoneBattle(this);
			if(result != null)
				return result;
		}
		return isInZone(ZoneType.battle_zone);
	}

	@Override
	public boolean isInWater()
	{
		return isInZone(ZoneType.water) && !(isInBoat() || isBoat() || isFlying());
	}

	public boolean isInSiegeZone()
	{
		return isInZone(ZoneType.SIEGE);
	}

	public boolean isInSSQZone()
	{
		return isInZone(ZoneType.ssq_zone);
	}

	public boolean isInDangerArea()
	{
		zonesRead.lock();
		try
		{
			Zone zone;
			for(int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
				if(zone.getTemplate().isShowDangerzone())
					return true;
			}
		}
		finally
		{
			zonesRead.unlock();
		}
		return false;
	}

	public boolean isInZone(ZoneType type)
	{
		zonesRead.lock();
		try
		{
			Zone zone;
			for(int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
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
		List<Event> e = Collections.emptyList();
		zonesRead.lock();
		try
		{
			Zone zone;
			for(int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
				if(!zone.getEvents().isEmpty())
				{
					if(e.isEmpty())
						e = new ArrayList<Event>(2);

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

	public boolean isInZone(String name)
	{
		zonesRead.lock();
		try
		{
			Zone zone;
			for(int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
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

	public Zone getZone(ZoneType type)
	{
		zonesRead.lock();
		try
		{
			Zone zone;
			for(int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
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

	public List<Zone> getZones()
	{
		return _zones;
	}

	public Location getRestartPoint()
	{
		zonesRead.lock();
		try
		{
			Zone zone;
			for(int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
				if(zone.getRestartPoints() != null)
				{
					ZoneType type = zone.getType();
					if(type == ZoneType.battle_zone || type == ZoneType.peace_zone || type == ZoneType.offshore || type == ZoneType.dummy)
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
			Zone zone;
			for(int i = 0; i < _zones.size(); i++)
			{
				zone = _zones.get(i);
				if(zone.getRestartPoints() != null)
				{
					ZoneType type = zone.getType();
					if(type == ZoneType.battle_zone || type == ZoneType.peace_zone || type == ZoneType.offshore || type == ZoneType.dummy)
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
	public int getGeoZ(int x, int y, int z)
	{
		if(isFlying() || isInWater() || isInBoat() || isBoat() || isDoor())
			return z;

		return super.getGeoZ(x, y, z);
	}

	protected boolean needStatusUpdate()
	{
		if(!isVisible())
			return false;

		boolean result = false;

		int bar;
		bar = (int) (getCurrentHp() * CLIENT_BAR_SIZE / getMaxHp());
		if(bar == 0 || bar != _lastHpBarUpdate)
		{
			_lastHpBarUpdate = bar;
			result = true;
		}

		bar = (int) (getCurrentMp() * CLIENT_BAR_SIZE / getMaxMp());
		if(bar == 0 || bar != _lastMpBarUpdate)
		{
			_lastMpBarUpdate = bar;
			result = true;
		}

		if(isPlayer())
		{
			bar = (int) (getCurrentCp() * CLIENT_BAR_SIZE / getMaxCp());
			if(bar == 0 || bar != _lastCpBarUpdate)
			{
				_lastCpBarUpdate = bar;
				result = true;
			}
		}

		return result;
	}

	public void onHitTimer(Creature target, int damage, boolean crit, boolean miss, boolean soulshot, byte shld, boolean unchargeSS, int elementalDamage, boolean elementalCrit)
	{
		if(isAlikeDead())
		{
			sendActionFailed();
			return;
		}

		if(target.isDead() || !isInRange(target, 2000))
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

		target.getListeners().onAttackHit(this, damage, crit, miss);

		ThreadPoolManager.getInstance().execute(new NotifyAITask(this, CtrlEvent.EVT_ATTACK, target, null, damage));
		ThreadPoolManager.getInstance().execute(new NotifyAITask(target, CtrlEvent.EVT_ATTACKED, this, null, damage));

		boolean checkPvP = checkPvP(target, null);

		// reduce targets HP
		doAttack(damage, target, null, false, false, crit, false);

		if(!miss && damage > 0)
		{
			// Скиллы, кастуемые при физ атаке
			if(!target.isDead())
			{
				if(crit)
					useTriggers(target, TriggerType.CRIT, null, null, damage);

				useTriggers(target, TriggerType.ATTACK, null, null, damage);

				// in classic stun breaks only on crit
				if (crit && Formulas.INSTANCE.calcStunBreak(target)) {
					target.getAbnormalList().stop(AbnormalType.STUN);
					//target.getAbnormalList().stop(AbnormalType.TURN_FLEE); На классике вроде как не должно сбивать.
				}

				if (Formulas.INSTANCE.calcRealTargetBreak()) {
					target.getAbnormalList().stop(AbnormalType.REAL_TARGET);
				}

				for(Abnormal abnormal : target.getAbnormalList())
				{
					double chance = crit ? abnormal.getSkill().getOnCritCancelChance() : abnormal.getSkill().getOnAttackCancelChance();
					if(chance > 0 && Rnd.chance(chance))
						abnormal.exit();
				}
			}

			if(soulshot && unchargeSS)
				unChargeShots(false);
		}

		if(miss) {
			target.getListeners().onCreatureAttackAvoid(this, false);
			target.useTriggers(this, TriggerType.UNDER_MISSED_ATTACK, null, null, damage);
		}

		startAttackStanceTask();

		if(checkPvP)
			startPvPFlag(target);
	}

	public void doAttack(double damage, Creature target, Skill skill,
						 boolean isDOT, boolean directlyToHp, boolean critical, boolean reflect)
	{
		/* need ?
		ThreadPoolManager.getInstance().execute(new NotifyAITask(this, CtrlEvent.EVT_ATTACK, target, null, damage));
		ThreadPoolManager.getInstance().execute(new NotifyAITask(target, CtrlEvent.EVT_ATTACKED, this, null, damage));

		startAttackStanceTask();*/

		if (!reflect && !isDOT)
		{
			damage *= getStat().getPositionTypeValue(DoubleStat.ATTACK_DAMAGE, PositionUtils2.INSTANCE.getPosition(this, target));

			// Counterattacks happen before damage received.
			if (!target.isDead() && (skill != null))
			{
				Formulas.INSTANCE.calcCounterAttack(this, target, skill, critical);

				// Shield Deflect Magic: Reflect all damage on caster.
				if (skill.isMagic() && (target.getStat().getValue(DoubleStat.VENGEANCE_SKILL_MAGIC_DAMAGE, 0) > Rnd.get(100)))
				{
					reduceCurrentHp(damage, target, skill, true, true, directlyToHp, false, false, isDOT, true, true, critical, false, false, 0.0, false);
					return;
				}
			}
		}

		// Reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary
		target.reduceCurrentHp(damage, this, skill, true, true, directlyToHp, false, false, isDOT, true, true, critical, false, false, 0, false);

		// Check if damage should be reflected or absorbed. When killing blow is made, the target doesn't reflect (vamp too?).
		if (!reflect && !isDOT && !target.isDead())
		{
			int reflectedDamage = reflectDamage(target, skill, damage);

			absorbDamage(target, skill, damage);

			if (reflectedDamage > 0)
			{
				target.doAttack(reflectedDamage, this, skill, isDOT, directlyToHp, critical, true);
				target.displayGiveDamageMessage(this, skill, reflectedDamage, null, 0, false, false, false, false, 0, false);
			}
		}

		// Break casting of target during attack.
		if (!target.isRaid() && Formulas.INSTANCE.calcAtkBreak(target, damage)) {
			target.abortAttack(true, true);
			target.breakCast();
		}
	}

	private int reflectDamage(Creature target, Skill skill, double damage) {
		int reflectedDamage = 0;

		// Reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary
		double reflectPercent = target.getStat().getValue(DoubleStat.REFLECT_DAMAGE_PERCENT, 0) - getStat().getValue(DoubleStat.REFLECT_DAMAGE_PERCENT_DEFENSE, 0);
		if (reflectPercent > 0) {
			reflectedDamage = (int) ((reflectPercent / 100.) * damage);
			reflectedDamage = Math.min(reflectedDamage, target.getMaxHp());

			// Reflected damage is limited by P.Def/M.Def
			if (skill != null && skill.isMagic()) {
				reflectedDamage = (int) Math.min(reflectedDamage, target.getStat().getMDef() * 1.5);
			} else {
				reflectedDamage = Math.min(reflectedDamage, target.getStat().getPDef());
			}
		}

		return reflectedDamage;
	}

	private void absorbDamage(Creature target, Skill skill, double damage) {
		// Absorb HP from the damage inflicted
		double absorbPercent = getStat().getValue(DoubleStat.ABSORB_DAMAGE_PERCENT, 0) * target.getStat().getValue(DoubleStat.ABSORB_DAMAGE_DEFENCE, 1);
		if (absorbPercent > 0 && Rnd.nextDouble() < getStat().getValue(DoubleStat.ABSORB_DAMAGE_CHANCE)) {
			int absorbDamage = (int) Math.min(absorbPercent * damage, getStat().getMaxRecoverableHp() - getCurrentHp());
			absorbDamage = Math.min(absorbDamage, (int) target.getCurrentHp());
			if (absorbDamage > 0) {
				setCurrentHp(getCurrentHp() + absorbDamage, false);
			}
		}

		// Absorb MP from the damage inflicted.
		absorbPercent = getStat().getValue(DoubleStat.ABSORB_MANA_DAMAGE_PERCENT, 0);
		if (absorbPercent > 0) {
			int absorbDamage = (int) Math.min((absorbPercent / 100.) * damage, getStat().getMaxRecoverableMp() - getCurrentMp());
			absorbDamage = Math.min(absorbDamage, (int) target.getCurrentMp());
			if (absorbDamage > 0) {
				setCurrentMp(getCurrentMp() + absorbDamage);
			}
		}
	}

	public void onCastEndTime(SkillEntry skillEntry, Creature aimingTarget, List<Creature> targets, boolean success)
	{
        if (skillEntry == null) {
            return;
        }

        // FIXME why targets == null ?
        if (targets == null) {
			return;
        }

		Skill skill = skillEntry.getTemplate();

		getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING, skill, aimingTarget, success);

		if(success)
		{
			skill.onFinishCast(aimingTarget, this, targets);
			for (Creature target : targets) {
				skill.onAbnormalTimeEnd(this, target);
			}

			getListeners().onCreatureSkillFinishCast(aimingTarget, skill);
			useTriggers(aimingTarget, targets, TriggerType.ON_FINISH_CAST, null, skill, 0);

			if(isPlayer())
			{
				for(ListenerHook hook : getPlayer().getListenerHooks(ListenerHookType.PLAYER_FINISH_CAST_SKILL))
					hook.onPlayerFinishCastSkill(getPlayer(), skill.getId());

				for(ListenerHook hook : ListenerHook.getGlobalListenerHooks(ListenerHookType.PLAYER_FINISH_CAST_SKILL))
					hook.onPlayerFinishCastSkill(getPlayer(), skill.getId());
			}
		}
	}

	public final void reduceCurrentHp(double damage, Creature attacker, Skill skill)
	{
		reduceCurrentHp(damage, attacker, skill, true, true, false, false, false, false, true, false, false, false, false, 0, false);
	}

	public final void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflectAndAbsorb, boolean transferDamage, boolean isDot, boolean sendReceiveMessage)
	{
		reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflectAndAbsorb, transferDamage, isDot, sendReceiveMessage, false, false, false, false, 0, false);
	}

	public void reduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflectAndAbsorb, boolean transferDamage, boolean isDot, boolean sendReceiveMessage, boolean sendGiveMessage, boolean crit, boolean miss, boolean shld, double elementalDamage, boolean elementalCrit)
	{
		if(isImmortal())
			return;

		// Notify of this attack only if there is an attacking creature.
		if (attacker != null)
		{
			attacker.getListeners().onCreatureDamageDealt(this, damage, skill, crit, isDot, canReflectAndAbsorb);
		}

		if(canReflectAndAbsorb) // TODO: Сверить с оффом и переделать.
			damage = Math.max(0, damage - getStat().getValue(DoubleStat.DAMAGE_BLOCK_COUNT));

		boolean damaged = true;
		if(miss || damage <= 0)
			damaged = false;

		boolean damageBlocked = isDamageBlocked(attacker);
		if(attacker == null || isDead() || (attacker.isDead() && !isDot) || damageBlocked)
			damaged = false;

		if(!damaged)
		{
			if(attacker != this && sendGiveMessage)
				attacker.displayGiveDamageMessage(this, skill, 0, null, 0, crit, miss, shld, damageBlocked, 0, false);
			return;
		}

		//double reflectedDamage = 0.;
		double transferedDamage = 0.;
		Servitor servitorForTransfereDamage = null;

		/*if(canReflectAndAbsorb)
		{
			boolean canAbsorb = canAbsorb(this, attacker);
			if(canAbsorb)
				damage = absorbToEffector(attacker, damage);	// e.g. Noble Sacrifice.

			// TODO: Проверить на оффе, что должно быть первее, поглощение саммоном или МП?
			damage = reduceDamageByMp(attacker, damage);			// e.g. Arcane Barrier.

			// e.g. Transfer Pain
			transferedDamage = getDamageForTransferToServitor(damage);
			servitorForTransfereDamage = getServitorForTransfereDamage(transferedDamage);
			if(servitorForTransfereDamage != null)
				damage -= transferedDamage;
			else
				transferedDamage = 0.;

			reflectedDamage = reflectDamage(attacker, skill, damage);

			if(canAbsorb)
				attacker.absorbDamage(this, skill, damage);
		}*/

		if(!attacker.equals(this)) {
			int fullValue = (int) damage;

			// Check and calculate transfered damage
			transferedDamage = (int) getDamageForTransferToServitor(damage);
			servitorForTransfereDamage = getServitorForTransfereDamage(transferedDamage);
			if (servitorForTransfereDamage != null)
			{
				// Only transfer dmg up to current HP, it should not be killed
				transferedDamage = Math.min((int) servitorForTransfereDamage.getCurrentHp() - 1, transferedDamage);
				if (transferedDamage > 0)
				{
					servitorForTransfereDamage.reduceCurrentHp(transferedDamage, attacker, null);
					damage -= transferedDamage;
				}
			}

			damage = reduceDamageByMp(attacker, damage);

			damage = transferDamage(attacker, damage);
		}

		// Damage can be limited by ultimate effects
		final double damageCap = getStat().getValue(DoubleStat.DAMAGE_LIMIT);
		if (damageCap > 0) {
			damage = Math.min(damage, damageCap);
		}

		// Calculate PvP/PvE damage received. It is a post-attack stat.
		if (attacker.isPlayable()) {
			damage *= (100 + getStat().getValue(DoubleStat.PVP_DAMAGE_TAKEN)) / 100.0;
		} else {
			damage *= (100 + getStat().getValue(DoubleStat.PVE_DAMAGE_TAKEN)) / 100.0;
		}

		getListeners().onCurrentHpDamage(attacker, damage, skill, crit, isDot, false);

		if(attacker != this)
		{
			if(sendGiveMessage)
				attacker.displayGiveDamageMessage(this, skill, (int) damage, servitorForTransfereDamage, (int) transferedDamage, crit, miss, shld, damageBlocked, (int) elementalDamage, elementalCrit);

			if(sendReceiveMessage) {
				displayReceiveDamageMessage(attacker, (int) damage, servitorForTransfereDamage, (int) transferedDamage, (int) elementalDamage);
			}

			if(!isDot)
				useTriggers(attacker, TriggerType.RECEIVE_DAMAGE, null, null, damage);
		}

		/*if(servitorForTransfereDamage != null && transferedDamage > 0)
			servitorForTransfereDamage.reduceCurrentHp(transferedDamage, attacker, null, false, false, false, false, true, false, true);*/

		onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, isDot);

		/*if(reflectedDamage > 0.)
		{
			displayGiveDamageMessage(attacker, skill, (int) reflectedDamage, null, 0, false, false, false, false, 0, false);
			attacker.reduceCurrentHp(reflectedDamage, this, null, true, true, false, false, false, false, true);
		}*/
	}

	protected void onReduceCurrentHp(final double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean isDot)
	{
		if(awake && isSleeping())
			getAbnormalList().stop(AbnormalType.SLEEP);

		if(attacker != this || (skill != null && skill.isBad()))
		{
			getAbnormalList().stopEffectsOnDamage();

			startAttackStanceTask();
		}

		if(damage <= 0)
			return;

		/* don't exist in classic
		if(getCurrentHp() - damage < 10 && getStat().getValue(DoubleStat.ShillienProtection) == 1)
		{
			setCurrentHp(getMaxHp(), false, !isDot);
			setCurrentCp(getMaxCp(), !isDot);
			if(isDot)
			{
				StatusUpdate su = new StatusUpdate(this, attacker, StatusUpdatePacket.UpdateType.REGEN, StatusUpdatePacket.CUR_HP, StatusUpdatePacket.CUR_CP);
				attacker.sendPacket(su);
				sendPacket(su);
				broadcastStatusUpdate();
				sendChanges();
			}
			return;
		}*/

		boolean isUndying = isUndying();

		setCurrentHp(Math.max(getCurrentHp() - damage, isDot ? 1 : (isUndying ? 0.5 : 0)), false, !isDot);
		if(isDot)
		{
			StatusUpdate su = new StatusUpdate(this, attacker, StatusUpdatePacket.UpdateType.REGEN, StatusUpdatePacket.CUR_HP);
			attacker.sendPacket(su);
			sendPacket(su);
			broadcastStatusUpdate();
		}

		if(isUndying)
		{
			if(getCurrentHp() == 0.5 && (!isPlayer() || !getPlayer().isGMUndying()))
				if(getFlags().getUndying().getFlag().compareAndSet(false, true))
					getListeners().onDeathFromUndying(attacker);
		}
		else if(getCurrentHp() < 0.5)
		{
			if(attacker != this || (skill != null && skill.isBad()))
				useTriggers(attacker, TriggerType.DIE, null, null, damage);

			doDie(attacker);
		}
	}

	public void reduceCurrentMp(double i, Creature attacker)
	{
		if(attacker != null && attacker != this)
		{
			if(isSleeping())
				getAbnormalList().stop(AbnormalType.SLEEP);

			if(isMeditated())
				getAbnormalList().stop("Meditation");
		}

		if(isDamageBlocked(attacker) && attacker != null && attacker != this)
		{
			attacker.sendPacket(SystemMsg.THE_ATTACK_HAS_BEEN_BLOCKED);
			return;
		}

		// 5182 = Blessing of protection, работает если разница уровней больше 10 и не в зоне осады
		if(attacker != null && attacker.isPlayer() && Math.abs(attacker.getLevel() - getLevel()) > 10)
		{
			// ПК не может нанести урон чару с блессингом
			if(attacker.isPK() && isProtectionBlessingAffected() && !isInSiegeZone())
				return;
			// чар с блессингом не может нанести урон ПК
			if(isPK() && attacker.isProtectionBlessingAffected() && !attacker.isInSiegeZone())
				return;
		}

		i = _currentMp - i;

		if(i < 0)
			i = 0;

		setCurrentMp(i);

		if(attacker != null && attacker != this)
			startAttackStanceTask();
	}

	// place holder, only player has CP
	public void reduceCurrentCp(int value)
	{
	}

	public void removeAllSkills()
	{
		for(SkillEntry s : getAllSkillsArray())
			removeSkill(s);
	}

	public SkillEntry removeSkill(SkillInfo skillInfo)
	{
		return removeSkill(skillInfo, false);
	}

	public SkillEntry removeSkill(SkillInfo skillInfo, boolean stopEffects)
	{
		if(skillInfo == null)
			return null;
		return removeSkillById(skillInfo.getId(), stopEffects);
	}

	public SkillEntry removeSkillById(int id)
	{
		return removeSkillById(id, false);
	}

	public SkillEntry removeSkillById(int id, boolean stopEffects)
	{
		// Remove the skill from the L2Character _skills
		SkillEntry oldSkillEntry = _skills.remove(id);

		// Remove all its Func objects from the L2Character calculator set
		if(oldSkillEntry != null)
		{
			Skill oldSkill = oldSkillEntry.getTemplate();

			if(oldSkill.isToggle() || stopEffects)
				getAbnormalList().stop(oldSkill, false);

			removeTriggers(oldSkill);

			if(oldSkill.isPassive())
			{
				if (oldSkill.checkConditions(SkillConditionScope.PASSIVE, this, this)) {
					for (EffectTemplate et : oldSkill.getEffectTemplates(EffectUseType.NORMAL)) {
						final EffectHandler handler = et.getHandler();
						if (handler.checkPumpConditionImpl(null, this, this)) {
							handler.pumpEnd(null, this, this);
						}
					}
				}
			}

			if(Config.ALT_DELETE_SA_BUFFS && (oldSkill.isItemSkill() || oldSkill.isHandler()))
			{
				// Завершаем все эффекты, принадлежащие старому скиллу
				getAbnormalList().stop(oldSkill, false);

				// И с петов тоже
				for(Servitor servitor : getServitors())
					servitor.getAbnormalList().stop(oldSkill, false);
			}

			AINextAction nextAction = getAI().getNextAction();
			if(nextAction != null && nextAction == AINextAction.CAST)
			{
				Object args1 = getAI().getNextActionArgs()[0];
				if(oldSkillEntry.equals(args1))
					getAI().clearNextAction();
			}

            getStat().recalculateStats(false);

			onRemoveSkill(oldSkillEntry);
		}

		return oldSkillEntry;
	}

	public void addTriggers(StatTemplate f)
	{
		if(f.getTriggerList().isEmpty())
			return;

		for(TriggerInfo t : f.getTriggerList())
		{
			addTrigger(t);
		}
	}

	public void addTrigger(TriggerInfo t)
	{
		if(_triggers == null)
			_triggers = new ConcurrentHashMap<TriggerType, Set<TriggerInfo>>();

		Set<TriggerInfo> hs = _triggers.get(t.getType());
		if(hs == null)
		{
			hs = new CopyOnWriteArraySet<TriggerInfo>();
			_triggers.put(t.getType(), hs);
		}

		hs.add(t);

		if(t.getType() == TriggerType.ADD)
			useTriggerSkill(this, null, t, null, 0);
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
			synchronized (this)
			{
				if(_ai == null)
					_ai = new CharacterAI(this);
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

		if(oldAI != null)
		{
			if(oldAI.isActive())
			{
				oldAI.stopAITask();
				newAI.startAITask();
				newAI.setIntention(AI_INTENTION_ACTIVE);
			}
		}
	}

	public final void setCurrentHp(double newHp, boolean canResurrect, boolean sendInfo)
	{
		int maxHp = getMaxHp();

		newHp = Math.min(maxHp, Math.max(0, newHp));

		if(isDeathImmune())
			newHp = Math.max(1.1, newHp); // Ставим 1.1, потому что на олимпиаде 1 == Поражение, что вызовет зависание.

		if(_currentHp == newHp)
			return;

		if(newHp >= 0.5 && isDead() && !canResurrect)
			return;

		double hpStart = _currentHp;

		_currentHp = newHp;

		if(isDead.compareAndSet(true, false))
			onRevive();

		checkHpMessages(hpStart, _currentHp);

		final int lastHpPercent = _previousHpPercent.get();
		final int currentHpPercent = (int) ((_currentHp * 100) / maxHp);
		if (lastHpPercent >= 60 && currentHpPercent <= 60
				|| currentHpPercent >= 60 && lastHpPercent <= 60
				|| lastHpPercent >= 30 && currentHpPercent <= 30
				|| currentHpPercent >= 30 && lastHpPercent <= 30) {
			if (_previousHpPercent.compareAndSet(lastHpPercent, currentHpPercent)) {
				getStat().recalculateStats(sendInfo);
			}
		}

		if(sendInfo)
		{
			broadcastStatusUpdate();
		}

		if(_currentHp < maxHp)
			startRegeneration();

		onChangeCurrentHp(hpStart, newHp);

		getListeners().onChangeCurrentHp(hpStart, newHp);
	}

	public final void setCurrentHp(double newHp) {
		setCurrentHp(newHp, false);
	}

	public final void setCurrentHp(double newHp, boolean canResurrect)
	{
		setCurrentHp(newHp, canResurrect, true);
	}

	public void onChangeCurrentHp(double oldHp, double newHp)
	{
		//
	}

	public final void setCurrentMp(double newMp, boolean sendInfo)
	{
		int maxMp = getMaxMp();

		newMp = Math.min(maxMp, Math.max(0, newMp));

		if(_currentMp == newMp)
			return;

		if(newMp >= 0.5 && isDead())
			return;

		double mpStart = _currentMp;

		_currentMp = newMp;

		if(sendInfo)
		{
			broadcastStatusUpdate();
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
		newCp = Math.min(maxCp, Math.max(0, newCp));

		if(_currentCp == newCp)
			return;

		if(newCp >= 0.5 && isDead())
			return;

		double cpStart = _currentCp;

		_currentCp = newCp;

		if(sendInfo)
		{
			broadcastStatusUpdate();
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

		newHp = Math.min(maxHp, Math.max(0, newHp));
		newMp = Math.min(maxMp, Math.max(0, newMp));

		if(isDeathImmune())
			newHp = Math.max(1.1, newHp); // Ставим 1.1, потому что на олимпиаде 1 == Поражение, что вызовет зависание.

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

		final int lastHpPercent = _previousHpPercent.get();
		final int currentHpPercent = (int) ((_currentHp * 100) / maxHp);
		if (lastHpPercent >= 60 && currentHpPercent <= 60
				|| currentHpPercent >= 60 && lastHpPercent <= 60
				|| lastHpPercent >= 30 && currentHpPercent <= 30
				|| currentHpPercent >= 30 && lastHpPercent <= 30) {
			if (_previousHpPercent.compareAndSet(lastHpPercent, currentHpPercent)) {
				getStat().recalculateStats(true);
			}
		}

		broadcastStatusUpdate();

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

    public final void setUsingFlyingSkill(boolean value) {
        usingFlyingSkill.compareAndSet(!value, value);
    }

	public final void setName(String name)
	{
		_name = name;
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
		if(target == null)
			_aggressionTarget = HardReferences.emptyRef();
		else
			_aggressionTarget = target.getRef();
	}

	public Creature getAggressionTarget()
	{
		return _aggressionTarget.get();
	}

	public void setTarget(GameObject object)
	{
		if(object != null && !object.isVisible())
			object = null;

		/* DS: на оффе сброс текущей цели не отменяет атаку или каст.
		if(object == null)
		{
			if(isAttackingNow() && getAI().getAttackTarget() == getTarget())
				abortAttack(false, true);
			if(isCastingNow() && getAI().getCastTarget() == getTarget())
				breakCast();
		}
		*/

		if(object == null)
			_target = HardReferences.emptyRef();
		else
			_target = object.getRef();
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

	protected IClientOutgoingPacket changeMovePacket()
	{
		return new ChangeMoveTypePacket(this);
	}

	public final void startAbnormalEffect(AbnormalVisualEffect ae)
	{
		if(ae == AbnormalVisualEffect.NONE)
			return;

		abnormalVisualEffects.add(ae);
		sendChanges();
	}

	public void startAttackStanceTask()
	{
		startAttackStanceTask0();
	}

	/**
	 * Запускаем задачу анимации боевой позы. Если задача уже запущена, увеличиваем время, которое персонаж будет в боевой позе на 15с
	 */
	protected void startAttackStanceTask0()
	{
		// предыдущая задача еще не закончена, увеличиваем время
		if(isInCombat())
		{
			_stanceEndTime = System.nanoTime() + ATTACK_STANCE_DURATION.toNanos();
			return;
		}

		_stanceEndTime = System.nanoTime() + ATTACK_STANCE_DURATION.toNanos();

		broadcastPacket(new AutoAttackStartPacket(getObjectId()));

		// отменяем предыдущую
		final Future<?> task = _stanceTask;
		if(task != null)
			task.cancel(false);

		// Добавляем задачу, которая будет проверять, если истекло время нахождения персонажа в боевой позе,
		// отменяет задачу и останаливает анимацию.
		_stanceTask = LazyPrecisionTaskManager.getInstance().scheduleAtFixedRate(_stanceTaskRunnable == null ? _stanceTaskRunnable = new AttackStanceTask() : _stanceTaskRunnable, 1000L, 1000L);
	}

	/**
	 * Останавливаем задачу анимации боевой позы.
	 */
	public void stopAttackStanceTask()
	{
		_stanceEndTime = 0L;

		final Future<?> task = _stanceTask;
		if(task != null)
		{
			task.cancel(false);
			_stanceTask = null;

			broadcastPacket(new AutoAttackStopPacket(getObjectId()));
		}
	}

	private class AttackStanceTask implements Runnable
	{
		@Override
		public void run()
		{
			if(!isInCombat())
				stopAttackStanceTask();
		}
	}

	/**
	 * Остановить регенерацию
	 */
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

	/**
	 * Запустить регенерацию
	 */
	public void startRegeneration()
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
				_regenTask = RegenTaskManager.getInstance().scheduleAtFixedRate(_regenTaskRunnable == null ? _regenTaskRunnable = new RegenTask() : _regenTaskRunnable, getRegenTick(), getRegenTick());
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

			int maxHp = getStat().getMaxRecoverableHp();
			int maxMp = getStat().getMaxRecoverableMp();
			int maxCp = isPlayer() ? getStat().getMaxRecoverableCp() : 0;

			double addHp = 0.;
			double addMp = 0.;
			double addCp = 0.;

			regenLock.lock();
			try
			{
				if(_currentHp < maxHp)
					addHp += getHpRegen();

				if(_currentMp < maxMp)
					addMp += getMpRegen();

				if(_currentCp < maxCp)
					addCp += getCpRegen();

				if(isSitting())
				{
					// Added regen bonus when character is sitting
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
						// TODO: Вынести значения в датапак?
						addHp += getHpRegen() * 0.5;
						addMp += getMpRegen() * 0.5;
						addCp += getCpRegen() * 0.5;
					}
				}
				else if(!getMovement().isMoving())
				{
					// TODO: Вынести значения в датапак?
					addHp += getHpRegen() * 0.1;
					addMp += getMpRegen() * 0.1;
					addCp += getCpRegen() * 0.1;
				}
				else if(isRunning())
				{
					// TODO: Вынести значения в датапак?
					addHp -= getHpRegen() * 0.3;
					addMp -= getMpRegen() * 0.3;
					addCp -= getCpRegen() * 0.3;
				}

				if(isRaid())
				{
					addHp *= Config.RATE_RAID_REGEN;
					addMp *= Config.RATE_RAID_REGEN;
				}

				_currentHp += Math.max(0, Math.min(addHp, maxHp - _currentHp));
				_currentHp = Math.min(maxHp, _currentHp);

				_currentMp += Math.max(0, Math.min(addMp, maxMp - _currentMp));
				_currentMp = Math.min(maxMp, _currentMp);

				if(isPlayer())
				{
					_currentCp += Math.max(0, Math.min(addCp, maxCp - _currentCp));
					_currentCp = Math.min(maxCp, _currentCp);
				}

				//отрегенились, останавливаем задачу
				if(_currentHp == maxHp && _currentMp == maxMp && _currentCp == maxCp)
					stopRegeneration();
			}
			finally
			{
				regenLock.unlock();
			}

			final int lastHpPercent = _previousHpPercent.get();
			final int currentHpPercent = (int) ((_currentHp * 100) / maxHp);
			if (lastHpPercent >= 60 && currentHpPercent <= 60
					|| currentHpPercent >= 60 && lastHpPercent <= 60
					|| lastHpPercent >= 30 && currentHpPercent <= 30
					|| currentHpPercent >= 30 && lastHpPercent <= 30) {
				if (_previousHpPercent.compareAndSet(lastHpPercent, currentHpPercent)) {
					getStat().recalculateStats(true);
				}
			}

			getListeners().onChangeCurrentHp(hpStart, _currentHp);
			getListeners().onChangeCurrentMp(mpStart, _currentMp);

			if(isPlayer())
				getListeners().onChangeCurrentCp(cpStart, _currentCp);

			TIntSet updateAttributes = new TIntHashSet(3);
			if(addHp > 0 && _currentHp != hpStart)
				updateAttributes.add(StatusUpdatePacket.CUR_HP);
			if(addMp > 0 && _currentMp != mpStart)
				updateAttributes.add(StatusUpdatePacket.CUR_MP);
			if(addCp > 0 && _currentCp != cpStart)
				updateAttributes.add(StatusUpdatePacket.CUR_CP);
			if(!updateAttributes.isEmpty())
			{
				sendPacket(new StatusUpdate(Creature.this, StatusUpdatePacket.UpdateType.REGEN, updateAttributes.toArray()));
				broadcastStatusUpdate();
			}

			checkHpMessages(hpStart, _currentHp);
		}
	}

	public final void stopAbnormalEffect(AbnormalVisualEffect ae)
	{
		abnormalVisualEffects.remove(ae);
		sendChanges();
	}

	public final void stopAllAbnormalEffects()
	{
		abnormalVisualEffects.clear();
		sendChanges();
	}

	/**
	 * Блокируем персонажа
	 */
	public void block()
	{
		_blocked = true;
	}

	/**
	 * Разблокируем персонажа
	 */
	public void unblock()
	{
		_blocked = false;
	}

	public void setDamageBlockedException(Creature exception)
	{
		if(exception == null)
			_damageBlockedException = HardReferences.emptyRef();
		else
			_damageBlockedException = exception.getRef();
	}

	public void setEffectImmunityException(Creature exception)
	{
		if(exception == null)
			_effectImmunityException = HardReferences.emptyRef();
		else
			_effectImmunityException = exception.getRef();
	}

	@Override
	public boolean isInvisible(GameObject observer)
	{
		if(observer != null && getObjectId() == observer.getObjectId())
			return false;

		for(Event event : getEvents())
		{
			Boolean result = event.isInvisible(this, observer);
			if(result != null)
				return result;
		}
		return getFlags().getInvisible().get();
	}

	public boolean startInvisible(Object owner, boolean withServitors)
	{
		boolean result;
		if(owner == null)
			result = getFlags().getInvisible().start();
		else
			result = getFlags().getInvisible().start(owner);

		if(result)
		{
			for(Player p : World.getAroundObservers(this))
			{
				if(isInvisible(p))
					p.sendPacket(p.removeVisibleObject(this, null));
			}

			if(withServitors)
			{
				for(Servitor servitor : getServitors())
					servitor.startInvisible(owner, false);
			}
		}
		return result;
	}

	public final boolean startInvisible(boolean withServitors)
	{
		return startInvisible(null, withServitors);
	}

	public boolean stopInvisible(Object owner, boolean withServitors)
	{
		boolean result;
		if(owner == null)
			result = getFlags().getInvisible().stop();
		else
			result = getFlags().getInvisible().stop(owner);

		if(result)
		{
			List<Player> players = World.getAroundObservers(this);
			for(Player p : players)
			{
				if(isVisible() && !isInvisible(p))
					p.sendPacket(p.addVisibleObject(this, null));
			}

			if(withServitors)
			{
				for(Servitor servitor : getServitors())
					servitor.stopInvisible(owner, false);
			}
		}
		return result;
	}

	public final boolean stopInvisible(boolean withServitors)
	{
		return stopInvisible(null, withServitors);
	}

	public void addIgnoreSkillsEffect(EffectHandler effect, TIntSet skills)
	{
		_ignoreSkillsEffects.put(effect, skills);
	}

	public boolean removeIgnoreSkillsEffect(EffectHandler effect)
	{
		return _ignoreSkillsEffects.remove(effect) != null;
	}

	public boolean isIgnoredSkill(Skill skill)
	{
		for(TIntSet set : _ignoreSkillsEffects.values())
		{
			if(set.contains(skill.getId()))
				return true;
		}
		return false;
	}

	public boolean isUndying()
	{
		return getFlags().getUndying().get();
	}

	public boolean isInvulnerable()
	{
		return getFlags().getInvulnerable().get();
	}

	public boolean isHpBlocked()
	{
		return isInvulnerable() || getStat().has(BooleanStat.HP_BLOCKED);
	}

	public boolean isMpBlocked()
	{
		return isInvulnerable() || getStat().has(BooleanStat.MP_BLOCKED);
	}

	/**
	 * Active the abnormal effect Fake Death flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.
	 */
	public final void startFakeDeath()
	{
		if (!isPlayer()) {
			return;
		}

		// Aborts any attacks/casts if fake dead
		stopActions();
		getAI().notifyEvent(CtrlEvent.EVT_FAKE_DEATH);
		broadcastPacket(new ChangeWaitTypePacket(this, ChangeWaitTypePacket.WT_START_FAKEDEATH));
		// need ? broadcastCharInfo();
	}

	public void breakFakeDeath() {
		breakFakeDeath(true);
	}

	public void breakFakeDeath(boolean removeEffects)
	{
		if (removeEffects) {
			for (Abnormal abnormal : getAbnormalList()) {
				if (abnormal.getSkill().getNextAction() == NextActionType.FAKE_DEATH) {
					getAbnormalList().stop(abnormal.getId());
				}
			}
		}

		Player player = getPlayer();

		// if this is a player instance, start the grace period for this character (grace from mobs only)!
		player.setNonAggroTime(System.currentTimeMillis() + 5000L);

		broadcastPacket(new ChangeWaitTypePacket(this, ChangeWaitTypePacket.WT_STOP_FAKEDEATH));
		// TODO: Temp hack: players see FD on ppl that are moving: Teleport to someone who uses FD - if he gets up he will fall down again for that client -
		// even tho he is actually standing... Probably bad info in CharInfo packet?
		broadcastPacket(new RevivePacket(this));
		// need ? broadcastCharInfo();

		ThreadPoolManager.getInstance().schedule(new GameObjectTasks.EndBreakFakeDeathTask(player), 2500);
	}

	public void setMeditated(boolean value)
	{
		_meditated = value;
	}

	public final void setPreserveAbnormal(boolean value)
	{
		_isPreserveAbnormal = value;
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
		return getFlags().getConfused().get() || getStat().has(BooleanStat.CONFUSED);
	}

	public boolean isFakeDeath()
	{
		return getStat().has(BooleanStat.FAKE_DEATH);
	}

	public boolean isAfraid()
	{
		return getStat().has(BooleanStat.FEAR);
	}

	public boolean isBlocked()
	{
		return _blocked;
	}

	public boolean isMuted(Skill skill)
	{
		if(skill == null || skill.isNotAffectedByMute())
			return false;

		if (skill.isMagic()) {
			if (isMMuted()) {
				return true;
			}
		}
		if (skill.isPhysic()) {
			if (isPMuted()) {
				return true;
			}
		}
		if (skill.isSpecial()) {
			if (isSpecialMuted()) {
				return true;
			}
		}

		return false;
	}

	public boolean isPMuted()
	{
		return getStat().has(BooleanStat.PHYSICAL_SKILL_MUTED);
	}

	public boolean isMMuted()
	{
		return getStat().has(BooleanStat.BLOCK_SPELL);
	}

	public boolean isSpecialMuted()
	{
		return getStat().has(BooleanStat.SPECIAL_SKILL_MUTED);
	}

	public boolean isAMuted()
	{
		return getStat().has(BooleanStat.ATTACK_MUTED) || isTransformed() && !getTransform().getType().isCanAttack();
	}

	public boolean isSleeping()
	{
		return getFlags().getSleeping().get();
	}

	public boolean isStunned()
	{
		return getFlags().getStunned().get();
	}

	public boolean isMeditated()
	{
		return _meditated;
	}

	public boolean isWeaponEquipBlocked()
	{
		return getStat().has(BooleanStat.DISARMED);
	}

	public boolean isParalyzed()
	{
		return getFlags().hasBlockActions() && getAbnormalList().contains(AbnormalType.PARALYZE);
	}

	public boolean isControlBlocked()
	{
		return getFlags().isControlBlocked();
	}

	public boolean isImmobilized()
	{
		return getFlags().getImmobilized().get() || getStat().has(BooleanStat.BLOCK_MOVE) || getRunSpeed() < 1;
	}

	public boolean isHealBlocked()
	{
		if(isInvulnerable())	// TODO: Check this.
			return true;
		return isAlikeDead() || getFlags().getHealBlocked().get();
	}

	public boolean isDamageBlocked(Creature attacker)
	{
		if(attacker == this)
			return false;

		if(isInvulnerable())
			return true;

		Creature exception = _damageBlockedException.get();
		if(exception != null && exception == attacker)
			return false;

		// invul handling
		if (isHpBlocked() /*&& !isDOT*/) {
			return true;
		}

		if(getFlags().getDamageBlocked().get())
		{
			double blockRadius = getStat().getValue(DoubleStat.DAMAGE_BLOCK_RADIUS);
			if(blockRadius == -1)
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
		return getFlags().getDistortedSpace().get();
	}

	public boolean isCastingNow()
	{
		return getSkillCast(SkillCastingType.NORMAL).isCastingNow() || getSkillCast(SkillCastingType.NORMAL_SECOND).isCastingNow();
	}

	public boolean isCastingNow(Predicate<CreatureSkillCast> filter)
	{
		return Arrays.stream(_skillCasts)
				.filter(Objects::nonNull)
				.anyMatch(filter);
	}

	public boolean isChanneling()
	{
		return Arrays.stream(_skillCasts)
				.filter(Objects::nonNull)
				.anyMatch(CreatureSkillCast::isChanneling);
	}
	
	public boolean isLockedTarget()
	{
		return _lockedTarget;
	}

	public boolean isMovementDisabled()
	{
		return isBlocked() || isImmobilized() || isAlikeDead() || isStunned() || isSleeping() || isDecontrolled()
				|| isAttackingNow() || isCastingNow() || isControlBlocked() && !isAfraid() || getFlags().hasBlockActions();
	}

	public final boolean isActionsDisabled()
	{
		return isActionsDisabled(true);
	}

	public boolean isActionsDisabled(boolean withCast)
	{
		return isBlocked() || isAlikeDead() || isStunned() || isSleeping() || isDecontrolled() || isAttackingNow() || withCast && (isCastingNow() || getFlags().isAllSkillsDisabled()) || isControlBlocked() || getFlags().hasBlockActions();
	}

	public boolean isUseItemDisabled()
	{
		return isAlikeDead() || isStunned() || isSleeping() || isParalyzed() || isControlBlocked();
	}

	public final boolean isDecontrolled()
	{
		return isParalyzed() || isKnockDowned() || isKnockBacked() || isFlyUp();
	}

	public final boolean isAttackingDisabled()
	{
		return _attackReuseEndTime > System.nanoTime()/* remove ? || getStat().has(BooleanStat.ATTACK_MUTED)*/;
	}

	public boolean isOutOfControl()
	{
		return isBlocked() || isConfused() || isAfraid();
	}

	public void checkAndRemoveInvisible()
	{
		getAbnormalList().stop(AbnormalType.HIDE);
	}

	public void teleToLocation(ILocation loc)
	{
		teleToLocation(loc.getX(), loc.getY(), loc.getZ(), getReflection());
	}

	public void teleToLocation(ILocation loc, Reflection r)
	{
		teleToLocation(loc.getX(), loc.getY(), loc.getZ(), r);
	}

	public void teleToLocation(ILocation location, int min, int max)
	{
		teleToLocation(Location.findAroundPosition(location, min, max, 0), getReflection());
	}

	public void teleToLocation(int x, int y, int z)
	{
		teleToLocation(x, y, z, getReflection());
	}

	public void teleToLocation(Location location, int min, int max)
	{
		teleToLocation(Location.findAroundPosition(location, min, max, 0), getReflection());
	}

	public void teleToLocation(int x, int y, int z, Reflection r)
	{
		if(!isTeleporting.compareAndSet(false, true))
			return;

		if(isFakeDeath())
			breakFakeDeath();

		abortCast(true, false);
		if(!isLockedTarget())
			setTarget(null);

		getMovement().stopMove();

		if(!isBoat() && !isFlying() && !World.isWater(new Location(x, y, z), r))
			z = GeoEngine.getLowerHeight(x, y, z, r.getGeoIndex());

		final Location loc = Location.findPointToStay(x, y, z, 0, 50, r.getGeoIndex());

		//TODO: [Bonux] Check ExTeleportToLocationActivate!
		if(isPlayer())
		{
			Player player = (Player) this;

			if(!player.isInObserverMode())
				sendPacket(new TeleportToLocationPacket(this, loc.x, loc.y, loc.z));

			player.getListeners().onTeleport(loc.x, loc.y, loc.z, r);

			decayMe();

			setLoc(loc);

			setReflection(r);

			if(!player.isInObserverMode())
				sendPacket(new ExTeleportToLocationActivate(this, loc.x, loc.y, loc.z));

			if(player.isInObserverMode() || isFakePlayer())
				onTeleported();
		}
		else
		{
			broadcastPacket(new TeleportToLocationPacket(this, loc.x, loc.y, loc.z));

			World.forgetObject(this);

			setLoc(loc);

			setReflection(r);

			sendPacket(new ExTeleportToLocationActivate(this, loc.x, loc.y, loc.z));

			onTeleported();
		}
	}

	public boolean onTeleported()
	{
		if(isTeleporting.compareAndSet(true, false))
		{
			updateZones();
			return true;
		}
		return false;
	}

	public void sendMessage(CustomMessage message)
	{

	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "[" + getObjectId() + "]";
	}

	@Override
	public double getCollisionRadius()
	{
		return getBaseStats().getCollisionRadius();
	}

	@Override
	public double getCollisionHeight()
	{
		return getBaseStats().getCollisionHeight();
	}

	public AbnormalList getAbnormalList()
	{
		if(_effectList == null)
		{
			synchronized (this)
			{
				if(_effectList == null)
					_effectList = new AbnormalList(this);
			}
		}

		return _effectList;
	}

	public boolean paralizeOnAttack(Creature attacker)
	{
		int max_attacker_level = 0xFFFF;

		if(isNpc())
		{
			NpcInstance npc = (NpcInstance) this;

			NpcInstance leader = npc.getLeader();
			if(leader != null)
				return leader.paralizeOnAttack(attacker);

			if(isRaid() && !isArenaRaid())
				max_attacker_level = getLevel() + npc.getParameter("ParalizeOnAttack", Config.RAID_MAX_LEVEL_DIFF);
			else
			{
				int max_level_diff = npc.getParameter("ParalizeOnAttack", -1000);
				if(max_level_diff != -1000)
					max_attacker_level = getLevel() + max_level_diff;
			}
		}

		if(attacker.getLevel() > max_attacker_level)
			return true;

		return false;
	}

	@Override
	protected void onDelete()
	{
		CharacterAI ai = getAI();
		if(ai != null)
		{
			ai.stopAllTaskAndTimers();
			ai.notifyEvent(CtrlEvent.EVT_DELETE);
		}

		stopDeleteTask();

		GameObjectsStorage.remove(this);

		getAbnormalList().stopAll();

		super.onDelete();
	}

	// ---------------------------- Not Implemented -------------------------------

	public void addExpAndSp(long exp, long sp)
	{}

	public void broadcastCharInfo()
	{}

	public void broadcastCharInfoImpl(IUpdateTypeComponent... components)
	{}

	public void sendElementalInfo()
	{}

	public void sendSlotsInfo()
	{}

	public void sendCurrentHpMpCpExpSp()
	{}

	public void checkHpMessages(double currentHp, double newHp)
	{}

	public boolean checkPvP(Creature target, SkillEntry skillEntry)
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
		return 0;
	}

	public void setChargedSoulshotPower(double val)
	{
		//
	}

	public double getChargedSpiritshotPower()
	{
		return 0;
	}

	public double getChargedSpiritshotHealBonus()
	{
		return 0;
	}

	public void setChargedSpiritshotPower(double power, int unk, double healBonus)
	{
		//
	}

	public boolean isChargedShot(ShotType type)
	{
		return false;
	}

	public int getCharges()
	{
		return 0;
	}

	public void setCharges(int i)
	{}

	public boolean decreaseCharges(int count)
	{
		return true;
	}

	public int getAgathionEnergy()
	{
		return 0;
	}

	public void setAgathionEnergy(int val)
	{
		//
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

	public int getServitorsCount()
	{
		return 0;
	}

	public boolean hasServitor()
	{
		return false;
	}

	public final List<Servitor> getServitors()
	{
		return getServitors(servitor -> true);
	}

	public List<Servitor> getServitors(Predicate<Servitor> predicate)
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

	public void startPvPFlag(Creature target)
	{}

	public boolean unChargeShots(boolean spirit)
	{
		return false;
	}

	private Future<?> _updateAbnormalIconsTask;

	private class UpdateAbnormalIcons implements Runnable
	{
		@Override
		public void run()
		{
			updateAbnormalIconsImpl();
			_updateAbnormalIconsTask = null;
		}
	}

	public void updateAbnormalIcons()
	{
		if(Config.USER_INFO_INTERVAL == 0)
		{
			if(_updateAbnormalIconsTask != null)
			{
				_updateAbnormalIconsTask.cancel(false);
				_updateAbnormalIconsTask = null;
			}
			updateAbnormalIconsImpl();
			return;
		}

		if(_updateAbnormalIconsTask != null)
			return;

		_updateAbnormalIconsTask = ThreadPoolManager.getInstance().schedule(new UpdateAbnormalIcons(), Config.USER_INFO_INTERVAL);
	}

	public void updateAbnormalIconsImpl()
	{
		broadcastAbnormalStatus(getAbnormalStatusUpdate());
	}

	public ExAbnormalStatusUpdateFromTargetPacket getAbnormalStatusUpdate()
	{
		Abnormal[] effects = getAbnormalList().toArray();
		Arrays.sort(effects, AbnormalsComparator.getInstance());

		ExAbnormalStatusUpdateFromTargetPacket abnormalStatus = new ExAbnormalStatusUpdateFromTargetPacket(getObjectId());
		for(Abnormal effect : effects)
		{
			if(effect != null && !effect.checkAbnormalType(AbnormalType.HP_RECOVER) && (isPlayable() ? effect.getSkill().isShowPlayerAbnormal() : effect.getSkill().isShowNpcAbnormal()))
				effect.addIcon(abnormalStatus);
		}
		return abnormalStatus;
	}

	public void broadcastAbnormalStatus(ExAbnormalStatusUpdateFromTargetPacket packet)
	{
		if(getTarget() == this)
			sendPacket(packet);

		if(!isVisible())
			return;

		List<Player> players = World.getAroundObservers(this);
		Player target;
		for(int i = 0; i < players.size(); i++)
		{
			target = players.get(i);
			if(target.getTarget() == this)
				target.sendPacket(packet);
		}
	}

	/*public void broadcastModifiedStats(Set<DoubleStat> changed)
	{
		if ((changed == null) || changed.isEmpty())
		{
			return;
		}

		// Don't broadcast modified stats on login.
		if (isPlayer() && !getPlayer().isOnline())
		{
			return;
		}

		// If this creature was previously moving, but now due to stat change can no longer move, broadcast StopMove packet.
		if (isMoving() && (getMoveSpeed() <= 0))
		{
			stopMove(null);
		}

		if (isSummon())
		{
			Servitor summon = (Servitor) this;
			if (summon.getOwner() != null)
			{
				summon.updateAndBroadcastStatus(1);
			}
		}
		else
		{
			boolean broadcastFull = true;
			StatusUpdate su = new StatusUpdate(this);
			UIPacket info = null;
			if (isPlayer())
			{
				info = new UserInfo(getActingPlayer(), false);
				info.addComponentType(UserInfoType.SLOTS, UserInfoType.ENCHANTLEVEL);
			}
			for (DoubleStat stat : changed)
			{
				if (info != null)
				{
					switch (stat)
					{
						case MOVE_SPEED:
						case RUN_SPEED:
						case WALK_SPEED:
						case SWIM_RUN_SPEED:
						case SWIM_WALK_SPEED:
						case FLY_RUN_SPEED:
						case FLY_WALK_SPEED:
						{
							info.addComponentType(UserInfoType.MULTIPLIER);
							break;
						}
						case PHYSICAL_ATTACK_SPEED:
						{
							info.addComponentType(UserInfoType.MULTIPLIER, UserInfoType.STATS);
							break;
						}
						case PHYSICAL_ATTACK:
						case PHYSICAL_DEFENCE:
						case EVASION_RATE:
						case ACCURACY_COMBAT:
						case CRITICAL_RATE:
						case MAGIC_CRITICAL_RATE:
						case MAGIC_EVASION_RATE:
						case ACCURACY_MAGIC:
						case MAGICAL_ATTACK:
						case MAGICAL_ATTACK_SPEED:
						case MAGICAL_DEFENCE:
						{
							info.addComponentType(UserInfoType.STATS);
							break;
						}
						case MAX_CP:
						{
							if (isPlayer())
							{
								info.addComponentType(UserInfoType.MAX_HPCPMP);
							}
							else
							{
								su.addUpdate(StatusUpdateType.MAX_CP, getMaxCp());
							}
							break;
						}
						case MAX_HP:
						{
							if (isPlayer())
							{
								info.addComponentType(UserInfoType.MAX_HPCPMP);
							}
							else
							{
								su.addUpdate(StatusUpdateType.MAX_HP, getMaxHp());
							}
							break;
						}
						case MAX_MP:
						{
							if (isPlayer())
							{
								info.addComponentType(UserInfoType.MAX_HPCPMP);
							}
							else
							{
								su.addUpdate(StatusUpdateType.MAX_CP, getMaxMp());
							}
							break;
						}
						case STAT_STR:
						case STAT_CON:
						case STAT_DEX:
						case STAT_INT:
						case STAT_WIT:
						case STAT_MEN:
						{
							info.addComponentType(UserInfoType.BASE_STATS);
							break;
						}
						case DEFENCE_FIRE:
						case DEFENCE_WATER:
						case DEFENCE_WIND:
						case DEFENCE_EARTH:
						case DEFENCE_HOLY:
						case DEFENCE_UNHOLY:
						{
							info.addComponentType(UserInfoType.ELEMENTALS);
							break;
						}
						case ATTACK_FIRE:
						case ATTACK_WATER:
						case ATTACK_WIND:
						case ATTACK_EARTH:
						case ATTACK_HOLY:
						case ATTACK_UNHOLY:
						{
							info.addComponentType(UserInfoType.ATK_ELEMENTAL);
							break;
						}
					}
				}
			}

			if (isPlayer())
			{
				final PlayerInstance player = getActingPlayer();
				player.refreshOverloaded(true);
				player.refreshExpertisePenalty();
				sendPacket(info);

				if (broadcastFull)
				{
					player.broadcastCharInfo();
				}
				else
				{
					if (su.hasUpdates())
					{
						broadcastPacket(su);
					}
				}
				if (hasServitors() && hasAbnormalType(AbnormalType.ABILITY_CHANGE))
				{
					getServitors().values().forEach(Summon::broadcastStatusUpdate);
				}
			}
			else if (isNpc())
			{
				if (broadcastFull)
				{
					World.getInstance().forEachVisibleObject(this, PlayerInstance.class, player ->
					{
						if (!isVisibleFor(player))
						{
							return;
						}

						if (getRunSpeed() == 0)
						{
							player.sendPacket(new ServerObjectInfo((Npc) this, player));
						}
						else
						{
							player.sendPacket(new NpcInfo((Npc) this));
						}
					});
				}
				else if (su.hasUpdates())
				{
					broadcastPacket(su);
				}
			}
			else if (su.hasUpdates())
			{
				broadcastPacket(su);
			}
		}
	}*/

	public void updateStats()
	{
		sendChanges();
	}

	public void setOverhitAttacker(Creature attacker)
	{}

	public void setOverhitDamage(double damage)
	{}

	public boolean isHero()
	{
		return false;
	}

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
	public boolean setLoc(ILocation loc)
	{
		return setXYZ(loc.getX(), loc.getY(), loc.getZ());
	}

	public boolean setLoc(ILocation loc, boolean stopMove)
	{
		return setXYZ(loc.getX(), loc.getY(), loc.getZ(), stopMove);
	}

	@Override
	public boolean setXYZ(int x, int y, int z)
	{
		return setXYZ(x, y, z, false);
	}

	public boolean setXYZ(int x, int y, int z, boolean stopMove)
	{
		if(!stopMove)
			getMovement().stopMove();

		getMovement().getMoveLock().lock();
		try
		{
			if(!super.setXYZ(x, y, z))
				return false;
		}
		finally
		{
			getMovement().getMoveLock().unlock();
		}

		updateZones();
		return true;
	}

	@Override
	protected void onSpawn()
	{
		super.onSpawn();

        startRegeneration();
		updateZones();
	}

	@Override
	public void spawnMe(Location loc)
	{
		if(loc.h >= 0)
			setHeading(loc.h);
		super.spawnMe(loc);
	}

	@Override
	protected void onDespawn()
	{
		if(!isLockedTarget())
			setTarget(null);
		getMovement().stopMove();
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

	// Функция для дизактивации умений персонажа (если умение не активно, то он не дает статтов и имеет серую иконку).
	private TIntSet _unActiveSkills = new TIntHashSet();

	public void addUnActiveSkill(Skill skill)
	{
		if(skill == null || isUnActiveSkill(skill.getId()))
			return;

		if(skill.isToggle())
			getAbnormalList().stop(skill, false);

		removeTriggers(skill);

        getStat().recalculateStats(false);

		_unActiveSkills.add(skill.getId());
	}

	public void removeUnActiveSkill(Skill skill)
	{
		if(skill == null || !isUnActiveSkill(skill.getId()))
			return;

        getStat().recalculateStats(false);

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

	@NotNull
	public Optional<Double> getWeaponStat(@NotNull DoubleStat stat) {
		WeaponTemplate weaponTemplate = getActiveWeaponTemplate();
		return weaponTemplate != null ? weaponTemplate.getStat(stat) : Optional.empty();
	}

	public WeaponTemplate.WeaponType getAttackType() {
		if (getActiveWeaponTemplate() != null) {
			return getActiveWeaponTemplate().getItemType();
		}

		TransformTemplate transform = getTransform();
		if (transform != null && transform.getBaseAttackType() != WeaponTemplate.WeaponType.NONE) {
			return transform.getBaseAttackType();
		}

		return getTemplate().getBaseAttackType();
	}

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

	public <T extends Listener<Creature>> boolean addListener(T listener)
	{
		return getListeners().add(listener);
	}

	public <T extends Listener<Creature>> boolean removeListener(T listener)
	{
		return getListeners().remove(listener);
	}

	public CharStatsChangeRecorder<? extends Creature> getStatsRecorder()
	{
		if(_statsRecorder == null)
			synchronized (this)
			{
				if(_statsRecorder == null)
					_statsRecorder = new CharStatsChangeRecorder<Creature>(this);
			}

		return _statsRecorder;
	}

	@Override
	public boolean isCreature()
	{
		return true;
	}

	@Override
	public Creature asCreature()
	{
		return this;
	}

	public void displayGiveDamageMessage(Creature target, Skill skill, int damage, Servitor servitorTransferedDamage, int transferedDamage, boolean crit, boolean miss, boolean shld, boolean blocked, int elementalDamage, boolean elementalCrit)
	{
		if(miss)
		{
			if(target.isPlayer())
				target.sendPacket(new SystemMessage(SystemMessage.C1_HAS_EVADED_C2S_ATTACK).addName(target).addName(this));
			return;
		}

		if(blocked)
		{
			//
		}
		else if(shld)
		{
			if(target.isPlayer())
			{
				if(damage == Config.EXCELLENT_SHIELD_BLOCK_RECEIVED_DAMAGE)
					target.sendPacket(SystemMsg.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
				else if(damage > 0)
					target.sendPacket(SystemMsg.YOUR_SHIELD_DEFENSE_HAS_SUCCEEDED);
			}
		}
	}

	public void displayReceiveDamageMessage(Creature attacker, int damage, Servitor servitorTransferedDamage, int transferedDamage, int elementalDamage)
	{
		//
	}

	public Collection<TimeStamp> getSkillReuses()
	{
		return _skillReuses.valueCollection();
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
		if(isTransformed())
			return getTransform().getType() == TransformType.FLYING;
		return false;
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

		Location destLoc = getLoc().correctGeoZ(getGeoIndex()).changeZ((_visualTransform == null ? 0 : _visualTransform.getSpawnHeight()) + (int) getCurrentCollisionHeight());
		sendPacket(new FlyToLocation(this, destLoc, FlyType.DUMMY, 0, 0, 0));
		setLoc(destLoc);

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
	{
		//
	}

	public void setTransform(TransformTemplate template)
	{
		//
	}

	public boolean isDeathImmune()
	{
		return getFlags().getDeathImmunity().get() || isPeaceNpc() || getStat().has(BooleanStat.IGNORE_DEATH);
	}

	public final double getMovementSpeedMultiplier()
	{
		DoubleStat stat;
		if (isInWater()) {
			stat = isRunning() ? DoubleStat.SWIM_RUN_SPEED : DoubleStat.SWIM_WALK_SPEED;
		} else {
			stat = isRunning() ? DoubleStat.RUN_SPEED : DoubleStat.WALK_SPEED;
		}

		double baseSpeed = getTemplate().getBaseValue(stat).orElse(0.0);
		return getMoveSpeed() * (1.0 / baseSpeed);
	}

	@Override
	public double getMoveSpeed()
	{
		if(isRunning())
			return getRunSpeed();

		return getWalkSpeed();
	}

	public double getRunSpeed()
	{
		/* what is this value ?
		if(isMounted())
			return getRideRunSpeed();*/

		if(isFlying())
			return getFlyRunSpeed();

		if(isInWater())
			return getSwimRunSpeed();

		return getStat().getValue(DoubleStat.RUN_SPEED);
	}

	public double getWalkSpeed()
	{
		/* what is this value ?
		if(isMounted())
			return getRideWalkSpeed();*/

		if(isFlying())
			return getFlyWalkSpeed();

		if(isInWater())
			return getSwimWalkSpeed();

		return getStat().getValue(DoubleStat.WALK_SPEED);
	}

	public final double getSwimRunSpeed()
	{
		return getStat().getValue(DoubleStat.SWIM_RUN_SPEED);
	}

	public final double getSwimWalkSpeed()
	{
		return getStat().getValue(DoubleStat.SWIM_WALK_SPEED);
	}

	public final double getFlyRunSpeed()
	{
		return getStat().getValue(DoubleStat.FLY_RUN_SPEED);
	}

	public final double getFlyWalkSpeed()
	{
		return getStat().getValue(DoubleStat.FLY_WALK_SPEED);
	}

	public double getHpRegen()
	{
		return getStat().getValue(DoubleStat.HP_REGEN);
	}

	public double getMpRegen()
	{
		return getStat().getValue(DoubleStat.MP_REGEN);
	}

	public double getCpRegen()
	{
		return getStat().getValue(DoubleStat.CP_REGEN);
	}

	public int getEnchantEffect()
	{
		return 0;
	}

	public final boolean isKnockDowned()
	{
		return getFlags().getKnockDowned().get();
	}

	public final boolean isKnockBacked()
	{
		return getFlags().getKnockBacked().get();
	}

	public final boolean isFlyUp()
	{
		return getFlags().getFlyUp().get();
	}

	public void setRndCharges(int value)
	{
		_rndCharges = value;
	}

	public int getRndCharges()
	{
		return _rndCharges;
	}

	public void onEvtTimer(int timerId, Object arg1, Object arg2)
	{
		//
	}

	public void onEvtScriptEvent(String event, Object arg1, Object arg2)
	{
		//
	}

	public boolean isPeaceNpc()
	{
		return false;
	}

	// Получаем дистанцию для взаимодействия(атака, диалог и т.д.) с целью.
	public int getInteractionDistance(GameObject target)
	{
		int range = (int) Math.max(10, getMinDistance(target));
		if(target.isNpc())
		{
			range += INTERACTION_DISTANCE / 2;
			if(!target.isInRangeZ(this, range) && !GeoEngine.canMoveToCoord(getX(), getY(), getZ(), target.getX(), target.getY(), target.getZ(), getGeoIndex()))
			{
				List<Location> _moveList = GeoEngine.MoveList(getX(), getY(), getZ(), target.getX(), target.getY(), getGeoIndex(), false);
				if(_moveList != null)
				{
					Location moveLoc = _moveList.get(_moveList.size() - 1).geo2world();
					if(!target.isInRangeZ(moveLoc, range) && target.isInRangeZ(moveLoc, range + (INTERACTION_DISTANCE / 2)))
						range = target.getDistance3D(moveLoc) + 16;
				}
			}
		}
		else
			range += INTERACTION_DISTANCE;
		return range;
	}

	public boolean checkInteractionDistance(GameObject target)
	{
		return isInRangeZ(target, getInteractionDistance(target) + 32);
	}

	public void setDualCastEnable(boolean val)
	{
		_isDualCastEnable = val;
	}

	public boolean isDualCastEnable()
	{
		return _isDualCastEnable;
	}

	public boolean isTargetable(Creature creature)
	{
		if(creature != null)
		{
			if(creature == this)
				return true;

			if(creature.getFlags().getUntargetableList().get(this))
				return false;

			Player player = creature.getPlayer();
			if(player != null)
			{
				if(player.isGM())
					return true;
			}
		}

		return _isTargetable && !getStat().has(BooleanStat.UNTARGETABLE);
	}

	public boolean isTargetable()
	{
		return isTargetable(null);
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
		if(attacked.isPlayable() || !Config.DISABLE_VAMPIRIC_VS_MOB_ON_PVP)
			return true;
		return attacker.getPvpFlag() == 0;		
	}

	@Deprecated
	public CreatureBaseStats getBaseStats()
	{
		if(_baseStats == null)
			_baseStats = new CreatureBaseStats(this);
		return _baseStats;
	}

	public CreatureStat getStat()
	{
		if(_stat == null)
			_stat = new CreatureStat(this);
		return _stat;
	}

	public CreatureFlags getFlags()
	{
		if(_statuses == null)
			_statuses = new CreatureFlags(this);
		return _statuses;
	}

	public boolean isSpecialAbnormal(Skill skill)
	{
		return false;
	}

	// Аналог isInvul, но оно не блокирует атаку, а просто не отнимает ХП.
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
		for(Abnormal effect : getAbnormalList())
		{
			if(effect.isOffensive())
			{
				return true;
			}
		}
		return false;
	}

	public boolean isSitting()
	{
		return false;
	}
	
	public void sendChannelingEffect(Creature target, int state)
	{
		broadcastPacket(new ExShowChannelingEffect(this, target, state));
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

	public boolean isDeleteTaskScheduled() {
		return _deleteTask != null;
	}

	public void deleteCubics()
	{
		//
	}

	public void onZoneEnter(Zone zone)
	{
		//
	}

	public void onZoneLeave(Zone zone)
	{
		//
	}

	public boolean hasBasicPropertyResist()
	{
		return true;
	}

	public BasicPropertyResist getBasicPropertyResist(BasicProperty basicProperty)
	{
		if(_basicPropertyResists == null)
		{
			synchronized(this)
			{
				if(_basicPropertyResists == null)
					_basicPropertyResists = new ConcurrentHashMap<>();
			}
		}
		return _basicPropertyResists.computeIfAbsent(basicProperty, k -> new BasicPropertyResist());
	}

	public boolean isMounted()
	{
		return false;
	}

	@Override
	protected Shape makeGeoShape()
	{
		int x = getX();
		int y = getY();
		int z = getZ();
		Circle circle = new Circle(x, y, (int) getCollisionRadius());
		circle.setZmin(z - Config.MAX_Z_DIFF);
		circle.setZmax(z + (int) getCollisionHeight());
		return circle;
	}

	public CreatureMovement getMovement()
	{
		return _movement;
	}

	public CreatureSkillCast getSkillCast(SkillCastingType castingType)
	{
		CreatureSkillCast skillCast = _skillCasts[castingType.ordinal()];
		if(skillCast == null)
		{
			skillCast = new CreatureSkillCast(this, castingType);
			_skillCasts[castingType.ordinal()] = skillCast;
			
		}
		return skillCast;
	}

	@SafeVarargs
	public final CreatureSkillCast getSkillCast(Predicate<CreatureSkillCast> filter, Predicate<CreatureSkillCast>... filters) {
		for (Predicate<CreatureSkillCast> additionalFilter : filters) {
			filter = filter.and(additionalFilter);
		}

		return Arrays.stream(_skillCasts)
				.filter(Objects::nonNull)
				.filter(filter)
				.findAny()
				.orElse(null);
	}

	public ElementalElement getActiveElement()
	{
		return ElementalElement.NONE;
	}

	public boolean isCursedWeaponEquipped()
	{
		return false;
	}

	public MoveType getMoveType()
	{
		if (getMovement().isMoving() && isRunning())
		{
			return MoveType.RUN;
		}
		else if (getMovement().isMoving() && !isRunning())
		{
			return MoveType.WALK;
		}
		return MoveType.STAND;
	}

	public boolean isDebug()
	{
		return false;
	}
}