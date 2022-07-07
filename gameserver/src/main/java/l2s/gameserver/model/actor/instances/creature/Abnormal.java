package l2s.gameserver.model.actor.instances.creature;

import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.listener.actor.OnAttackListener;
import l2s.gameserver.listener.actor.OnMagicUseListener;
import l2s.gameserver.listener.actor.player.impl.ApplySkillOnReviveListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.s2c.AbnormalStatusUpdatePacket;
import l2s.gameserver.network.l2.s2c.ExAbnormalStatusUpdateFromTargetPacket;
import l2s.gameserver.network.l2.s2c.ExOlympiadSpelledInfoPacket;
import l2s.gameserver.network.l2.s2c.PartySpelledPacket;
import l2s.gameserver.network.l2.s2c.ShortBuffStatusUpdatePacket;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.skills.AbnormalType;
import l2s.gameserver.skills.EffectType;
import l2s.gameserver.skills.EffectUseType;
import l2s.gameserver.stats.funcs.Func;
import l2s.gameserver.stats.funcs.FuncOwner;
import l2s.gameserver.stats.triggers.TriggerType;
import l2s.gameserver.taskmanager.EffectTaskManager;
import l2s.gameserver.templates.skill.EffectTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Abnormal implements Runnable, Comparable<Abnormal>, FuncOwner
{
	protected static final Logger _log = LoggerFactory.getLogger(Abnormal.class);

	public static final Abnormal[] EMPTY_L2EFFECT_ARRAY = new Abnormal[0];

	public static final int SUSPENDED = -1;
	public static final int STARTING = 0;
	public static final int ACTING = 1;
	public static final int FINISHED = 2;

	protected final Creature _effector;
	protected final Creature _effected;
	protected final Skill _skill;
	private final AtomicInteger _state;
	private long _startTimeMillis;
	private int _duration;
	private int _timeLeft;
	private final int _interval;
	private Abnormal _next;
	private boolean _active;

	private ScheduledFuture<?> reApplyTask;

	protected final EffectTemplate _template;
	private Future<?> _effectTask;
	private final boolean _isOffensive;
	private final boolean _reflected;
	private ActionDispelListener _listener;

	protected Abnormal(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		_startTimeMillis = Long.MAX_VALUE;
		_next = null;
		_active = false;
		_skill = skill;
		_effector = creature;
		_effected = target;
		_reflected = reflected;
		_template = template;
		_duration = Math.min(Integer.MAX_VALUE, Math.max(0, _skill.getAbnormalTime() < 0 ? Integer.MAX_VALUE : _skill.getAbnormalTime()));
		_timeLeft = _duration;
		_interval = Math.max(1, template.getInterval());
		_state = new AtomicInteger(STARTING);
		boolean isSelf = template.getUseType() == EffectUseType.SELF;
		_isOffensive = template.getParam().getBool("offensive", isSelf && _skill.isSelfOffensive() || !isSelf && _skill.isOffensive());
	}

	public final long getStartTime()
	{
		return _startTimeMillis;
	}

	public final int getTimeLeft()
	{
		return _timeLeft;
	}

	public final void setTimeLeft(int value)
	{
		_timeLeft = Math.max(0, Math.min(value, _duration));
	}

	public final boolean isTimeLeft()
	{
		return getTimeLeft() > 0;
	}

	public final boolean isActive()
	{
		return _active;
	}

	public void setActive(boolean set)
	{
		_active = set;
	}

	public EffectTemplate getTemplate()
	{
		return _template;
	}

	public AbnormalType getAbnormalType()
	{
		return getTemplate().getAbnormalType();
	}

	public int getAbnormalLvl()
	{
		return getTemplate().getAbnormalLvl();
	}

	public boolean checkAbnormalType(AbnormalType abnormal)
	{
		AbnormalType abnormalType = getAbnormalType();
		return abnormalType != AbnormalType.none && abnormal == abnormalType;
	}

	public boolean checkAbnormalType(Abnormal effect)
	{
		return checkAbnormalType(effect.getAbnormalType());
	}

	public Skill getSkill()
	{
		return _skill;
	}

	public Creature getEffector()
	{
		return _effector;
	}

	public Creature getEffected()
	{
		return _effected;
	}

	public double calc()
	{
		return getTemplate().getValue();
	}

	public boolean isFinished()
	{
		return getState() == FINISHED;
	}

	private int getState()
	{
		return _state.get();
	}

	private boolean setState(int oldState, int newState)
	{
		return _state.compareAndSet(oldState, newState);
	}

	public boolean checkCondition()
	{
		return getTemplate().checkCondition(this);
	}

	protected boolean checkActingCondition()
	{
		if(isOfUseType(EffectUseType.START) || isOfUseType(EffectUseType.TICK))
			return getEffector().getCastingSkill() == getSkill();
		return getTemplate().checkCondition(this);
	}

	public void instantUse()
	{
		onStart();
		onActionTime();
		onExit();
	}

	protected void onStart()
	{
		if(!isInstant())
		{
			getEffected().addStatFuncs(getStatFuncs());
			getEffected().addTriggers(getTemplate());
			AbnormalEffect[] abnormalEffects;
			AbnormalEffect[] abnormals = abnormalEffects = _template.getAbnormalEffects();
			for(AbnormalEffect abnormal : abnormalEffects)
				if(abnormal != AbnormalEffect.NONE)
					getEffected().startAbnormalEffect(abnormal);
			if(getSkill().isAbnormalCancelOnAction() && getEffected().isPlayable())
				getEffected().addListener((_listener = new ActionDispelListener()));
			if(getEffected().isPlayer() && !getSkill().canUseTeleport())
				getEffected().getPlayer().getPlayerAccess().UseTeleport = false;
		}
		getEffected().useTriggers(getEffected(), TriggerType.ON_START_EFFECT, null, _skill, getTemplate(), 0.0);
	}

	protected boolean onActionTime()
	{
		return true;
	}

	protected void onExit()
	{
		if(!isInstant())
		{
			if (getEffected().isPhantom())
			getEffected().getPlayer().getListeners().stopEffect(this.getSkill().getId());
		
			getEffected().removeStatsOwner(this);
			getEffected().removeTriggers(getTemplate());
			AbnormalEffect[] abnormalEffects = _template.getAbnormalEffects();
			for(AbnormalEffect abnormal : abnormalEffects)
				if(abnormal != AbnormalEffect.NONE)
					getEffected().stopAbnormalEffect(abnormal);
			if(getSkill().isAbnormalCancelOnAction())
				getEffected().removeListener(_listener);
			if(getEffected().isPlayer() && checkAbnormalType(AbnormalType.hp_recover))
				getEffected().sendPacket(new ShortBuffStatusUpdatePacket());
			if(getEffected().isPlayer() && !getSkill().canUseTeleport() && !getEffected().getPlayer().getPlayerAccess().UseTeleport)
				getEffected().getPlayer().getPlayerAccess().UseTeleport = true;
			if((isOfUseType(EffectUseType.START) || isOfUseType(EffectUseType.TICK)) && getEffected() == getEffector().getCastingTarget() && getSkill() == getEffector().getCastingSkill())
				getEffector().abortCast(true, false);
		}
		getEffected().useTriggers(getEffected(), TriggerType.ON_EXIT_EFFECT, null, _skill, getTemplate(), 0.0);
	}

	private void stopEffectTask()
	{
		if(_effectTask != null)
		{
			_effectTask.cancel(false);
			_effectTask = null;
		}
	}

	private void startEffectTask()
	{
		if(_effectTask == null)
		{
			_startTimeMillis = System.currentTimeMillis();
			_effectTask = EffectTaskManager.getInstance().scheduleAtFixedRate(this, 1000L, 1000L);
		}
	}

	public void restart()
	{
		_timeLeft = getDuration();
		stopEffectTask();
		startEffectTask();
	}

	public final boolean schedule()
	{
		Creature effected = getEffected();
		return effected != null && checkCondition() && getEffected().getAbnormalList().addEffect(this);
	}

	public final void start()
	{
		if(setState(STARTING, ACTING))
			synchronized (this)
			{
				setActive(true);
				onStart();
				startEffectTask();
			}
		else if(setState(FINISHED, ACTING))
			synchronized (this)
			{
				setActive(true);
				onStart();
				startEffectTask();
			}
	}

	public final void stop()
	{
		if(setState(ACTING, STARTING))
			synchronized (this)
			{
				setActive(false);
				onExit();
				stopEffectTask();
			}
	}

	@Override
	public final void run()
	{
		--_timeLeft;
		if(getState() != SUSPENDED)
		{
			boolean successActing = true;
			if(getState() == ACTING && isTimeLeft() && checkActingCondition())
			{
				if(!isActive())
					return;
				if(getTimeLeft() % getInterval() != 0)
					return;
				successActing = onActionTime();
				if(successActing)
					return;
			}
			if(getDuration() == Integer.MAX_VALUE && checkActingCondition() && getDuration() % getInterval() == 0)
			{
				successActing = onActionTime();
				if(successActing)
				{
					_timeLeft = getDuration();
					return;
				}
			}
			if(setState(ACTING, FINISHED))
			{
				if(checkActingCondition() && getDuration() % getInterval() == 0)
					onActionTime();
				synchronized (this)
				{
					setActive(false);
					stopEffectTask();
					onExit();
				}
				boolean lastEffect = getEffected().getAbnormalList().getEffectsCount(getSkill()) == 1;
				boolean msg = successActing && !isHidden() && lastEffect;
				getEffected().getAbnormalList().removeEffect(this);
				if(msg)
					getEffected().sendPacket(new SystemMessage(92).addSkillName(getDisplayId(), getDisplayLevel()));
				if(lastEffect)
					getSkill().onAbnormalTimeEnd(getEffector(), getEffected());
				getEffected().useTriggers(getEffected(), TriggerType.ON_FINISH_EFFECT, null, _skill, getTemplate(), 0.0);
				Abnormal next = getNext();
				if(next != null && next.setState(SUSPENDED, STARTING))
					next.schedule();
			}
			return;
		}
		if(isTimeLeft())
			return;
		exit();
	}

	public final void exit()
	{
		Abnormal next = getNext();
		if(next != null)
			next.exit();
		removeNext();
		if(setState(STARTING, FINISHED))
			getEffected().getAbnormalList().removeEffect(this);
		else if(setState(SUSPENDED, FINISHED))
			stopEffectTask();
		else if(setState(ACTING, FINISHED))
		{
			synchronized (this)
			{
				setActive(false);
				stopEffectTask();
				onExit();
			}
			getEffected().getAbnormalList().removeEffect(this);
		}
	}

	private boolean scheduleNext(Abnormal e)
	{
		if(e == null || e.isFinished())
			return false;
		Abnormal next = getNext();
		if(next != null && !next.maybeScheduleNext(e))
			return false;

		next.exit();

		_next = e;

		return true;
	}

	public Abnormal getNext()
	{
		return _next;
	}

	private void removeNext()
	{
		_next = null;
	}

	public boolean maybeScheduleNext(Abnormal newEffect)
	{
		if(newEffect.getAbnormalLvl() == getAbnormalLvl())
		{
			if(newEffect.getSkill().getTargetType() != Skill.SkillTargetType.TARGET_SELF && newEffect.getTimeLeft() < getTimeLeft())
				return false;
		}
		else if(newEffect.getAbnormalLvl() < getAbnormalLvl())
			return false;

		return true;
	}

	public Func[] getStatFuncs()
	{
		return getTemplate().getStatFuncs(this);
	}

	public void addIcon(AbnormalStatusUpdatePacket abnormalStatus)
	{
		if(!isActive() || isHidden())
			return;
		int duration = isHideTime() ? -1 : getTimeLeft();
		abnormalStatus.addEffect(getDisplayId(), getDisplayLevel(), duration);
	}

	public void addIcon(ExAbnormalStatusUpdateFromTargetPacket abnormalStatus)
	{
		if(!isActive() || isHidden())
			return;
		int duration = isHideTime() ? -1 : getTimeLeft();
		abnormalStatus.addEffect(_effector.getObjectId(), getDisplayId(), getDisplayLevel(), duration, 0);
	}

	public void addPartySpelledIcon(PartySpelledPacket ps)
	{
		if(!isActive() || isHidden())
			return;
		int duration = isHideTime() ? -1 : getTimeLeft();
		ps.addPartySpelledEffect(getDisplayId(), getDisplayLevel(), duration);
	}

	protected int getLevel()
	{
		return _skill.getLevel();
	}

	public EffectType getEffectType()
	{
		return getTemplate()._effectType;
	}

	public boolean isHidden()
	{
		return getDisplayId() < 0 || isOfUseType(EffectUseType.START) || isOfUseType(EffectUseType.TICK);
	}

	@Override
	public int compareTo(Abnormal obj)
	{
		return Objects.equals(obj, this) ? 0 : 1;
	}

	public boolean isSaveable()
	{
		return getSkill().isSaveable() && getTimeLeft() >= Config.ALT_SAVE_EFFECTS_REMAINING_TIME && !isHidden();
	}

	public boolean isCancelable()
	{
		return getSkill().isCancelable() && !isHidden();
	}

	public boolean isSelfDispellable()
	{
		return getSkill().isSelfDispellable() && !isHidden();
	}

	public int getDisplayId()
	{
		return getSkill().getDisplayId();
	}

	public int getDisplayLevel()
	{
		return getSkill().getDisplayLevel();
	}

	@Override
	public String toString()
	{
		return "Skill: " + _skill + ", state: " + getState() + ", active : " + _active;
	}

	@Override
	public boolean isFuncEnabled()
	{
		return true;
	}

	@Override
	public boolean overrideLimits()
	{
		return false;
	}

	public int getIndex()
	{
		return _template.getIndex();
	}

	public boolean checkBlockedAbnormalType(AbnormalType abnormal)
	{
		return false;
	}

	public boolean checkDebuffImmunity()
	{
		return false;
	}

	public boolean isIgnoredSkill(Skill skill)
	{
		return false;
	}

	public final boolean isOfUseType(EffectUseType useType)
	{
		return getTemplate().getUseType() == useType;
	}

	public final boolean isOffensive()
	{
		return _isOffensive;
	}

	public int getDuration()
	{
		return _duration;
	}

	public final void setDuration(int value)
	{
		_duration = Math.min(Integer.MAX_VALUE, Math.max(0, value));
		_timeLeft = _duration;
	}

	public int getInterval()
	{
		return _interval;
	}

	public boolean isHideTime()
	{
		return getSkill().isAbnormalHideTime() || getDuration() == Integer.MAX_VALUE;
	}

	public boolean isReflected()
	{
		return _reflected;
	}

	public boolean isInstant()
	{
		return getTemplate().isInstant();
	}

	public void scheduleForReApply(long reApplyTime)
	{
		if(reApplyTime == 0)
			return;

		Creature effected = getEffected();

		if(!effected.isPlayer() || effected.getPlayer().isInOlympiadMode())
			return;

		cancelReApply();
		reApplyTask = ThreadPoolManager.getInstance().schedule(() ->
		{
			if (effected.isDead()) {
				effected.addListener(new ApplySkillOnReviveListener(getSkill(), getTimeLeft() * 1000));
			} else {
				getSkill().getEffects(effected, effected, getTimeLeft() * 1000, 1);
			}
		}, reApplyTime);

		effected.addReApplyTask(reApplyTask);
	}

	public void cancelReApply() {
		if (reApplyTask != null) {
			reApplyTask.cancel(false);
		}
	}

	private class ActionDispelListener implements OnAttackListener, OnMagicUseListener
	{
		@Override
		public void onMagicUse(Creature actor, Skill skill, Creature target, boolean alt)
		{
			if(getSkill().isDoNotDispelOnSelfBuff() && target == actor && !skill.isOffensive())
				return;
			exit();
		}

		@Override
		public void onAttack(Creature actor, Creature target)
		{
			exit();
		}
	}

	public void addOlympiadSpelledIcon(Player player, ExOlympiadSpelledInfoPacket os)
	{
		if(!isActive() || isHidden())
			return;

		int duration = isHideTime() ? -1 : getTimeLeft();
		os.addSpellRecivedPlayer(player);
		os.addEffect(getDisplayId(), getDisplayLevel(), duration);
	}
}
