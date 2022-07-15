package l2s.gameserver.model.actor.instances.creature;

import l2s.gameserver.Config;
import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.handler.effects.impl.instant.retail.i_cp;
import l2s.gameserver.handler.effects.impl.instant.retail.i_hp;
import l2s.gameserver.handler.effects.impl.instant.retail.i_mp;
import l2s.gameserver.listener.actor.OnAttackListener;
import l2s.gameserver.listener.actor.OnMagicUseListener;
import l2s.gameserver.model.AbnormalTypeList;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.*;
import l2s.gameserver.skills.*;
import l2s.gameserver.skills.targets.AffectScope;
import l2s.gameserver.skills.targets.TargetType;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.stats.triggers.TriggerType;
import l2s.gameserver.taskmanager.EffectTaskManager;
import l2s.gameserver.templates.OptionDataTemplate;
import l2s.gameserver.templates.skill.EffectTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @reworked by Bonux
**/
// TODO: Сделать чтобы статты и триггеры хранились в абнормале, а не в эффектах.
public final class Abnormal implements Runnable, Comparable<Abnormal>
{
	private static final Logger _log = LoggerFactory.getLogger(Abnormal.class);

	//Состояние, при котором работает задача запланированного эффекта
	private static final int SUSPENDED = -1;
	private static final int STARTING = 0;
	private static final int ACTING = 1;
	private static final int FINISHED = 2;

	private final Creature _effector;
	private final Creature _effected;
	private final Skill _skill;
	private final OptionDataTemplate option;
	private final EffectUseType _useType;

	private final Collection<EffectHandler> _effects = new ConcurrentLinkedQueue<EffectHandler>();

	// the current state
	private final AtomicInteger _state;

	private final boolean reflected;
	private final boolean _saveable;

	private Future<?> _effectTask;
	private Map<EffectHandler, Future<?>> effectTickTasks;

	// period, milliseconds
	private long _startTimeMillis = Long.MAX_VALUE;

	private int _duration;
	private int _timeLeft;

	public Abnormal(Creature effector,
					Creature effected,
					Skill skill,
					EffectUseType useType,
					boolean reflected,
					boolean saveable)
	{
		_effector = effector;
		_effected = effected;
		_skill = skill;
		option = null;
		_useType = useType;
		this.reflected = reflected;

		int abnormalTime = Formulas.INSTANCE.calcEffectAbnormalTime(skill);
		_duration = Math.min(Integer.MAX_VALUE, Math.max(0, abnormalTime < 0 ? Integer.MAX_VALUE : abnormalTime));
		_timeLeft = _duration;

		_state = new AtomicInteger(STARTING);
		_saveable = saveable;

		for(EffectTemplate template : getSkill().getEffectTemplates(getUseType()))
		{
			if(template.isInstant() || !isOfUseType(template.getUseType())) // На всякий случай
				continue;

			if(!template.getTargetType().checkTarget(effected))
				continue;

			_effects.add(template.getHandler().getImpl());
		}
	}

	public Abnormal(Creature effector,
					Creature effected,
					OptionDataTemplate option,
					EffectUseType useType,
					boolean reflected,
					boolean saveable)
	{
		_effector = effector;
		_effected = effected;
		_skill = null;
		this.option = option;
		_useType = useType;
		this.reflected = reflected;

		int abnormalTime = Formulas.INSTANCE.calcEffectAbnormalTime(null);
		_duration = Math.min(Integer.MAX_VALUE, Math.max(0, abnormalTime < 0 ? Integer.MAX_VALUE : abnormalTime));
		_timeLeft = _duration;

		_state = new AtomicInteger(STARTING);
		_saveable = saveable;
	}

	public Abnormal(Creature effector, Creature effected, Abnormal abnormal, boolean reflected)
	{
		this(effector, effected, abnormal.getSkill(), abnormal.getUseType(), reflected, true);
	}

	public Skill getSkill()
	{
		return _skill;
	}

	public OptionDataTemplate getOption() {
		return option;
	}

	public AbnormalTypeList getAbnormalTypeList()
	{
		return getSkill().getAbnormalTypeList();
	}

	public int getAbnormalLvl()
	{
		return getSkill().getAbnormalLvl();
	}

	public Creature getEffector()
	{
		return _effector;
	}

	public Creature getEffected()
	{
		return _effected;
	}

	public boolean isReflected()
	{
		return reflected;
	}

	/**
	 * Возвращает время старта эффекта, если время не установлено, возвращается текущее
	 */
	public long getStartTime()
	{
		return _startTimeMillis;
	}

	/** Возвращает оставшееся время в секундах. */
	public int getTimeLeft()
	{
		return _timeLeft;
	}

	public void setTimeLeft(int value)
	{
		_timeLeft = Math.max(0, Math.min(value, _duration));
	}

	/** Возвращает true, если осталось время для действия эффекта */
	public boolean isTimeLeft()
	{
		return getTimeLeft() > 0;
	}

	public Collection<EffectHandler> getEffects()
	{
		return _effects;
	}

	/**
	 * Adds an effect to this buff info.
	 * @param effect the effect to add
	 */
	public void addEffect(EffectHandler effect)
	{
		_effects.add(effect);
	}

	public boolean isActive()
	{
		return getState() == ACTING;
	}

	public boolean isSuspended()
	{
		return getState() == SUSPENDED;
	}

	public boolean checkAbnormalType(AbnormalType abnormal)
	{
		AbnormalTypeList abnormalTypeList = getAbnormalTypeList();
		if(abnormalTypeList.isNone())
			return false;

		return abnormalTypeList.contains(abnormal);
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

	private ActionDispelListener _listener;

	private class ActionDispelListener implements OnAttackListener, OnMagicUseListener
	{
		@Override
		public void onMagicUse(Creature actor, SkillEntry skillEntry, Creature target, boolean alt)
		{
			// Vicious Stance and handlers should dispel invisibility
			Skill skill = skillEntry.getTemplate();
			if (getSkill().getId() == 922 && skillEntry.getId() != 312) {
				if (skill.getTargetTypeNew() == TargetType.SELF && skill.getAffectScope() == AffectScope.SINGLE) {
					if (skillEntry.getEntryType() != SkillEntryType.CUNSUMABLE_ITEM || !skill.hasEffect(EffectUseType.NORMAL_INSTANT, i_cp.class, i_hp.class, i_mp.class)) {
						return;
					}
				}
			}

			exit();
		}

		@Override
		public void onAttack(Creature actor, Creature target)
		{
			exit();
		}
	}

	private boolean checkCondition()
	{
		for(EffectHandler effect : getEffects())
		{
			if(!effect.checkPumpConditionImpl(this, getEffector(), getEffected()))
				return false;
		}
		return true;
	}

	private boolean checkActingCondition()
	{
		for(EffectHandler effect : getEffects())
		{
			if(!effect.checkActingConditionImpl(this, getEffector(), getEffected()))
				return false;
		}
		return true;
	}

	/** Notify started */
	private void onStart()
	{
		if(getSkill().isAbnormalCancelOnAction()) {
			_listener = new ActionDispelListener();
			getEffected().addListener(_listener);
		}
		if(getEffected().isPlayer() && !getSkill().canUseTeleport())
			getEffected().getPlayer().getPlayerAccess().UseTeleport = false;

		for(AbnormalVisualEffect abnormal : getSkill().getAbnormalEffects())
			getEffected().startAbnormalEffect(abnormal);

		for(EffectHandler effect : getEffects())
		{
			if (effect.checkPumpConditionImpl(this, getEffector(), getEffected())) {
				effect.pumpStart(this, getEffector(), getEffected());
			}

			getEffected().getStat().addFuncs(effect.getStatFuncs());
			getEffected().addTriggers(effect.getTemplate());

			//tigger on start
			getEffected().useTriggers(getEffected(), TriggerType.ON_START_EFFECT, null, getSkill(), effect.getTemplate(), 0);
		}
	}

	/**
	 * Cancel the effect in the the abnormal effect map of the effected L2Character.<BR><BR>
	 */
	private void onExit(boolean lastEffect)
	{
		if(getSkill().isAbnormalCancelOnAction())
			getEffected().removeListener(_listener);
		if(getEffected().isPlayer())
		{
			if(checkAbnormalType(AbnormalType.HP_RECOVER))
				getEffected().sendPacket(new ShortBuffStatusUpdatePacket());
			if(!getSkill().canUseTeleport() && !getEffected().getPlayer().getPlayerAccess().UseTeleport)
				getEffected().getPlayer().getPlayerAccess().UseTeleport = true;
		}

		for(AbnormalVisualEffect abnormal : getSkill().getAbnormalEffects())
		{
			if(abnormal != AbnormalVisualEffect.NONE)
				getEffected().stopAbnormalEffect(abnormal);
		}

		for(EffectHandler effect : getEffects())
		{
			if (effect.checkPumpConditionImpl(this, getEffector(), getEffected())) {
				effect.pumpEnd(this, getEffector(), getEffected());
			}

			getEffected().getStat().removeFuncsByOwner(effect);
			getEffected().removeTriggers(effect.getTemplate());

			//trigger on exit
			getEffected().useTriggers(getEffected(), TriggerType.ON_EXIT_EFFECT, null, getSkill(), effect.getTemplate(), 0);
		}

		if(lastEffect)
			getSkill().onSkillEnd(getEffector(), getEffected());
	}

	private void stopEffectTask()
	{
		if(_effectTask != null)
		{
			_effectTask.cancel(false);
			_effectTask = null;
		}

		if (effectTickTasks != null) {
			effectTickTasks.forEach((effect, task) -> task.cancel(false));
			effectTickTasks.clear();
		}
	}

	private void startEffectTask()
	{
		if(_effectTask == null)
		{
			_startTimeMillis = System.currentTimeMillis();
			_effectTask = EffectTaskManager.getInstance().scheduleAtFixedRate(this, 1000L, 1000L);
			if (!_effects.isEmpty()) {
				effectTickTasks = new ConcurrentHashMap<>(8);
				for (EffectHandler effect : _effects) {
					// Call on start.
					// TODO need ? effect.pumpStart(getEffector(), getEffected(), getSkill());

					if (effect.getTicks() == 0) {
						continue;
					}

					// If it's a continuous effect, if has ticks schedule a task with period, otherwise schedule a simple task to end it.
					final long delay = effect.getTicks() * 666L;
					Future<?> effectTickTask = EffectTaskManager.getInstance()
							.scheduleAtFixedRate(() -> onTick(effect), delay, delay);
					effectTickTasks.put(effect, effectTickTask);
				}
			}
		}
	}

	public void restart()
	{
		_timeLeft = getDuration();

		stopEffectTask();
		startEffectTask();
	}

	public boolean apply(Creature aimingTarget) {
		return apply(aimingTarget, true);
	}

	public boolean apply(Creature aimingTarget, boolean update)
	{
		if(_effects.isEmpty())
			return false;

		if(getEffected().isDead() && !getSkill().isPreservedOnDeath()) //why alike dead?
			return false;

		// need ?
		// if(!checkCondition())
		//	return false;

		if(getEffector() != getEffected() && isOfUseType(EffectUseType.NORMAL))
		{
			if(getEffected().isEffectImmune(getEffector()))
				return false;

			if(getEffected().isBuffImmune() && !isOffensive() || getEffected().isDebuffImmune() && isOffensive())
			{
				if(!isHidden() && !getSkill().isHideStartMessage())
				{
					if(getEffected() == aimingTarget)
					{
						getEffector().sendPacket(new SystemMessagePacket(SystemMsg.C1_HAS_RESISTED_YOUR_S2).addName(getEffected()).addSkillName(getSkill().getDisplayId(), getSkill().getDisplayLevel()));
						getEffector().sendPacket(new ExMagicAttackInfo(getEffector().getObjectId(), getEffected().getObjectId(), ExMagicAttackInfo.RESISTED));
					}
				}
				return false;
			}
		}

		if(!getEffected().getAbnormalList().add(this, update))
			return false;

		if(!isHidden() && !getSkill().isHideStartMessage())
			getEffected().sendPacket(new SystemMessagePacket(SystemMsg.S1S_EFFECT_CAN_BE_FELT).addSkillName(getDisplayId(), getDisplayLevel()));

		return true;
	}

	/**
	 * Переводит эффект в "фоновый" режим, эффект может быть запущен методом schedule
	 */
	public void suspend()
	{
		// Эффект создан, запускаем задачу в фоне
		if(setState(STARTING, SUSPENDED))
			startEffectTask();
		else if(setState(ACTING, SUSPENDED))
		{
			synchronized (this)
			{
				onExit(false);
			}
		}
	}

	/**
	 * Запускает задачу эффекта, в случае если эффект успешно добавлен в список
	 */
	public void start()
	{
		if(setState(SUSPENDED, ACTING))
		{
			synchronized (this)
			{
				onStart();
			}
		}
		else if(setState(STARTING, ACTING))
		{
			synchronized (this)
			{
				onStart();
				startEffectTask();
			}
		}
	}

	private void onTick(EffectHandler effect) {
		if (getState() != ACTING) {
			return;
		}

		effect.tick(this, getEffector(), getEffected());

		if (getSkill().isToggle() && !effect.consume(this, getEffected())) {
			exit();
		}
	}

	@Override
	public void run()
	{
		_timeLeft--;

		if(getState() == SUSPENDED)
		{
			if(isTimeLeft())
				return;

			exit();
			return;
		}

		if(getState() == ACTING)
		{
			if(isTimeLeft())
			{
				return;
			}
		}

		if(getDuration() == Integer.MAX_VALUE) // Если вдруг закончится время у безконечного эффекта.
		{
			_timeLeft = getDuration();
			return;
		}

		if(setState(ACTING, FINISHED))
		{
			boolean lastEffect = getEffected().getAbnormalList().getCount(getSkill()) == 1;

			synchronized(this)
			{
				stopEffectTask();
				onExit(lastEffect);
			}

			boolean msg = !isHidden() && lastEffect;

			getEffected().getAbnormalList().remove(this);

			// Отображать сообщение только для последнего оставшегося эффекта скилла
			if(msg)
				getEffected().sendPacket(new SystemMessage(SystemMessage.S1_HAS_WORN_OFF).addSkillName(getDisplayId(), getDisplayLevel()));

			if(lastEffect)
				getSkill().onAbnormalTimeEnd(getEffector(), getEffected());

			// trigger on finish
			for(EffectHandler effect : getEffects())
				getEffected().useTriggers(getEffected(), TriggerType.ON_FINISH_EFFECT, null, getSkill(), effect.getTemplate(), 0);
		}
	}

	/**
	 * Завершает эффект и все связанные, удаляет эффект из списка эффектов
	 */
	public void exit() {
		exit(true);
	}

	/**
	 * Завершает эффект и все связанные, удаляет эффект из списка эффектов
	 */
	public void exit(boolean update)
	{
		//Эффект запланирован на запуск, удаляем
		if(setState(STARTING, FINISHED))
			getEffected().getAbnormalList().remove(this, update);
		//Эффект работает в "фоне", останавливаем задачу в планировщике
		else if(setState(SUSPENDED, FINISHED))
			stopEffectTask();
		else if(setState(ACTING, FINISHED))
		{
			boolean lastEffect = getEffected().getAbnormalList().getCount(getSkill()) == 1;

			synchronized (this)
			{
				stopEffectTask();
				onExit(lastEffect);
			}
			getEffected().getAbnormalList().remove(this, update);
		}
	}

	public void addIcon(AbnormalStatusUpdatePacket abnormalStatus)
	{
		if(!isActive() || isHidden())
			return;
		int duration = isHideTime() ? AbnormalStatusUpdatePacket.INFINITIVE_EFFECT : getTimeLeft();
		getAbnormalTypeList().forEachClientId(clientId -> {
			abnormalStatus.addEffect(getDisplayId(), getDisplayLevel(), clientId, duration);
			return null;
		});
	}

	public void addIcon(ExAbnormalStatusUpdateFromTargetPacket abnormalStatus)
	{
		if(!isActive() || isHidden())
			return;
		int duration = isHideTime() ? AbnormalStatusUpdatePacket.INFINITIVE_EFFECT : getTimeLeft();
		getAbnormalTypeList().forEachClientId(clientId -> {
			abnormalStatus.addEffect(getEffector().getObjectId(), getDisplayId(), getDisplayLevel(), clientId, duration);
			return null;
		});
	}

	public void addPartySpelledIcon(PartySpelledPacket ps)
	{
		if(!isActive() || isHidden())
			return;
		int duration = isHideTime() ? AbnormalStatusUpdatePacket.INFINITIVE_EFFECT : getTimeLeft();
		getAbnormalTypeList().forEachClientId(clientId -> {
			ps.addPartySpelledEffect(getDisplayId(), getDisplayLevel(), clientId, duration);
			return null;
		});
	}

	public void addOlympiadSpelledIcon(Player player, ExOlympiadSpelledInfoPacket os)
	{
		if(!isActive() || isHidden())
			return;
		int duration = isHideTime() ? AbnormalStatusUpdatePacket.INFINITIVE_EFFECT : getTimeLeft();
		os.addSpellRecivedPlayer(player);
		getAbnormalTypeList().forEachClientId(clientId -> {
			os.addEffect(getDisplayId(), getDisplayLevel(), clientId, duration);
			return null;
		});
	}

	@Override
	public int compareTo(Abnormal obj)
	{
		if(obj.equals(this))
			return 0;
		return 1;
	}

	public boolean isCancelable()
	{
		return getSkill().isCancelable() && !isHidden();
	}

	public boolean isSelfDispellable()
	{
		return getSkill().isSelfDispellable() && !isHidden();
	}

	public int getId()
	{
		return getSkill().getId();
	}

	public int getLevel()
	{
		return getSkill().getLevel();
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
		return "Skill: " + getSkill() + ", state: " + getState() + ", active : " + isActive();
	}

	// TODO Переделать
	public boolean checkBlockedAbnormalType(AbnormalTypeList abnormalTypeList)
	{
		for(EffectHandler effect : getEffects())
		{
			if(effect.checkBlockedAbnormalType(this, getEffector(), getEffected(), abnormalTypeList))
				return true;
		}
		return false;
	}

	// TODO Переделать
	public boolean isHidden()
	{
		if(getDisplayId() < 0)
			return true;
		for(EffectHandler effect : getEffects())
		{
			if(effect.isHidden())
				return true;
		}
		return false;
	}

	// TODO Переделать
	public boolean isSaveable()
	{
		if(!_saveable || !getSkill().isSaveable() || getTimeLeft() < Config.ALT_SAVE_EFFECTS_REMAINING_TIME || isHidden())
			return false;

		for(EffectHandler effect : getEffects())
		{
			if(!effect.isSaveable())
				return false;
		}
		return true;
	}

	public EffectUseType getUseType()
	{
		return _useType;
	}

	public boolean isOfUseType(EffectUseType useType)
	{
		return _useType == useType;
	}

	public boolean isOffensive()
	{
		/* remove
		if(isOfUseType(EffectUseType.SELF))
			return getSkill().isSelfDebuff();
		else*/
			return getSkill().isDebuff() || getSkill().isBad();
	}

	public int getDuration()
	{
		return _duration;
	}

	public void setDuration(int value)
	{
		_duration = Math.min(Integer.MAX_VALUE, Math.max(0, value));
		_timeLeft = _duration;
	}

	public boolean isHideTime()
	{
		return getSkill().isAbnormalHideTime() || getDuration() == Integer.MAX_VALUE;
	}
}