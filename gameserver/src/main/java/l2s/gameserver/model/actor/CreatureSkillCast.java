package l2s.gameserver.model.actor;

import com.google.common.flogger.FluentLogger;
import l2s.commons.lang.reference.HardReference;
import l2s.commons.lang.reference.HardReferences;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.*;
import l2s.gameserver.model.Skill.SkillTargetType;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.*;
import l2s.gameserver.network.l2.s2c.FlyToLocation.FlyType;
import l2s.gameserver.skills.SkillCastingType;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillOperateType;
import l2s.gameserver.skills.targets.TargetType;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.stats.triggers.TriggerType;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.PositionUtils;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @auhor Bonux
**/
public class CreatureSkillCast
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	private class MagicLaunchedTask implements Runnable
	{
		@Override
		public void run()
		{
			final SkillEntry skillEntry = getSkillEntry();
			if(skillEntry == null)
				return;

			final Creature target = getTarget();
			if(target == null)
				return;

			final List<Creature> targets = skillEntry.getTemplate().getTargets(_actor, target, skillEntry, _forceUse, true, false);
			if (targets == null) {
				return;
			}

			_targets = targets;

			// Finish flying by setting the target location after picking targets. Packet is sent before MagicSkillLaunched.
			if (skillEntry.getTemplate().isFlyType()) {
				handleSkillFly(_actor, target, skillEntry);
			}

			if(!skillEntry.getTemplate().isNotBroadcastable())
				_actor.broadcastPacket(new MagicSkillLaunchedPacket(_actor.getObjectId(), skillEntry.getDisplayId(), skillEntry.getDisplayLevel(), targets, _castingType));

			if(_castLeftTime > 0)
				_skillTask = ThreadPoolManager.getInstance().schedule(() -> onMagicUseTimer(), _castLeftTime);
			else
				onMagicUseTimer();
		}
	}

	private final Creature _actor;
	private final SkillCastingType _castingType;

	private final AtomicBoolean _isCastingNow = new AtomicBoolean(false);

	private HardReference<? extends Creature> _target = HardReferences.emptyRef();

	private List<Creature> _targets = null;

	private boolean _forceUse = false;

	private SkillEntry _skillEntry = null;

	private long _animationEndTime;
	private int _castLeftTime;

	private boolean _isCriticalBlow = false;

	private Future<?> _skillTask = null;
	private Future<?> _skillTickTask = null;

	private Location _flyLoc = null;

	public CreatureSkillCast(Creature actor, SkillCastingType castingType)
	{
		_actor = actor;
		_castingType = castingType;
	}

	public Creature getActor()
	{
		return _actor;
	}

	public SkillCastingType getCastingType()
	{
		return _castingType;
	}

	public boolean isDual()
	{
		return _castingType == SkillCastingType.NORMAL_SECOND;
	}

	public boolean isAnyNormalType() {
		return _castingType == SkillCastingType.NORMAL || _castingType == SkillCastingType.NORMAL_SECOND;
	}

	public boolean isCastingNow()
	{
		return _isCastingNow.get();
	}

	public boolean isChanneling()
	{
		return _skillTickTask != null;
	}

	public Creature getTarget()
	{
		return _target == null ? null : _target.get();
	}

	public List<Creature> getTargets()
	{
		return _targets;
	}

	public SkillEntry getSkillEntry()
	{
		return _skillEntry;
	}

	public long getAnimationEndTime()
	{
		return _animationEndTime;
	}

	public int getCastLeftTime()
	{
		return _castLeftTime;
	}

	public boolean isCriticalBlow()
	{
		return _isCriticalBlow;
	}

	public boolean doCast(SkillEntry skillEntry, Creature target, boolean forceUse)
	{
		if(!_isCastingNow.compareAndSet(false, true))
			return false;

		if(!doCast0(skillEntry, target, forceUse))
		{
			clearVars();
			return false;
		}
		return true;
	}

	private boolean doCast0(SkillEntry skillEntry, Creature target, boolean forceUse)
	{
		if(skillEntry == null)
			return false;

		final Skill skill = skillEntry.getTemplate();

		if(isDual())
		{
			if(!_actor.isDualCastEnable() || !skill.isDouble())
				return false;
		}

		final Creature aimingTarget;
		if(target != null)
			aimingTarget = target;
		else
			aimingTarget = skill.getAimingTarget(_actor, getTarget(), skill, forceUse, true, false);

		if(aimingTarget == null)
			return false;

		_skillEntry = skillEntry;
		_target = aimingTarget.getRef();
		_forceUse = forceUse;

		if(skill.getReferenceItemId() > 0 && !_actor.consumeItemMp(skill.getReferenceItemId(), skill.getReferenceItemMpConsume()))
			return false;

		final double mpConsume1 = skill.getMpConsume1();
		if(mpConsume1 > 0)
		{
			if(_actor.getCurrentMp() < mpConsume1)
			{
				_actor.sendPacket(SystemMsg.NOT_ENOUGH_MP);
				return false;
			}
		}

		if(!skill.isHandler() && _actor.isPlayable())
		{
			if(skill.getItemConsumeId() > 0 && skill.getItemConsume() > 0)
			{
				if(skill.isItemConsumeFromMaster())
				{
					Player master = _actor.getPlayer();
					if(master == null)
						return false;

					if(ItemFunctions.getItemCount(master, skill.getItemConsumeId()) < skill.getItemConsume())
					{
						master.sendPacket(SystemMsg.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
						return false;
					}
				}
				else if(ItemFunctions.getItemCount((Playable) _actor, skill.getItemConsumeId()) < skill.getItemConsume())
				{
					_actor.sendPacket(SystemMsg.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
					return false;
				}
			}
		}

		_actor.getListeners().onMagicUse(skillEntry, aimingTarget, false);

		Location groundLoc = null;
		if(skill.getTargetTypeNew() == TargetType.GROUND)
		{
			if(_actor.isPlayer())
			{
				groundLoc = _actor.getPlayer().getGroundSkillLoc();
				if(groundLoc != null)
					_actor.setHeading(PositionUtils.calculateHeadingFrom(_actor.getX(), _actor.getY(), groundLoc.getX(), groundLoc.getY()), true);
			}
		}
		else if(skill.getTargetType() == SkillTargetType.TARGET_GROUND)
		{
			if(_actor.isPlayer())
			{
				groundLoc = _actor.getPlayer().getGroundSkillLoc();
				if(groundLoc != null)
					_actor.setHeading(PositionUtils.calculateHeadingFrom(_actor.getX(), _actor.getY(), groundLoc.getX(), groundLoc.getY()), true);
			}
		}
		else if(_actor != aimingTarget)
		{
			_actor.setHeading(PositionUtils.calculateHeadingFrom(_actor, aimingTarget), true);
			_actor.sendPacket(new MoveToPawnPacket(_actor, aimingTarget, _actor.getDistance(aimingTarget)));
		}

		int hitTime = skill.isSkillTimePermanent() ? skill.getHitTime() : Formulas.INSTANCE.calcSkillCastSpd(_actor, skill, skill.getHitTime());
		int hitCancelTime = skill.isSkillTimePermanent() ? skill.getHitCancelTime() : Formulas.INSTANCE.calcSkillCastSpd(_actor, skill, skill.getHitCancelTime());

		if(skill.isMagic() && !skill.isSkillTimePermanent() && _actor.getChargedSpiritshotPower() > 0)
		{
			hitTime = (int) (0.70 * hitTime);
			hitCancelTime = (int) (0.70 * hitCancelTime);
		}

		if(!skill.isSkillTimePermanent())
		{
			if(skill.isMagic())
			{
				int minCastTimeMagical = Math.min(Config.SKILLS_CAST_TIME_MIN_MAGICAL, skill.getHitTime());
				if(hitTime < minCastTimeMagical)
				{
					hitTime = minCastTimeMagical;
					hitCancelTime = 0;
				}
			}
			else
			{
				int minCastTimePhysical = Math.min(Config.SKILLS_CAST_TIME_MIN_PHYSICAL, skill.getHitTime());
				if(hitTime < minCastTimePhysical)
				{
					hitTime = minCastTimePhysical;
					hitCancelTime = 0;
				}
			}
		}

		_animationEndTime = System.currentTimeMillis() + hitTime;

		// TODO fix it
		boolean criticalBlow = skill.calcCriticalBlow(_actor, aimingTarget);

		long reuseDelay = Math.max(0, _actor.getStat().getReuseTime(skill));
		if(reuseDelay > 10)
			_actor.disableSkill(skill, reuseDelay);

		if(mpConsume1 > 0)
			_actor.reduceCurrentMp(mpConsume1, null);

		if (Rnd.nextDouble() < _actor.getStat().getCastChanceValue(skill.getMagicType())) {
			_actor.abortCast(true, true);
		}

		if(!skill.isNotBroadcastable())
		{
			MagicSkillUse msu = new MagicSkillUse(_actor, aimingTarget, skill.getDisplayId(), skill.getDisplayLevel(), hitTime, reuseDelay, _castingType);
			msu.setReuseSkillId(skill.getReuseSkillId());
			msu.setGroundLoc(groundLoc);
			msu.setCriticalBlow(criticalBlow);
			if(_actor.isServitor()) // TODO: [Bonux] Переделать.
			{
				Servitor.UsedSkill servitorUsedSkill = ((Servitor) _actor).getUsedSkill();
				if(servitorUsedSkill != null && servitorUsedSkill.getSkill() == skill)
				{
					msu.setServitorSkillInfo(servitorUsedSkill.getActionId());
					((Servitor) _actor).setUsedSkill(null);
				}
			}
			_actor.broadcastPacket(msu);
		}

		if(skill.getTargetTypeNew() == TargetType.HOLYTHING)
			aimingTarget.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, _actor, 1);
		else if(skill.getTargetType() == SkillTargetType.TARGET_HOLY)
			aimingTarget.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, _actor, 1);

		if(_actor.isPlayer())
		{
			if(skill.hasEffect("i_summon_pet"))
				_actor.sendPacket(SystemMsg.SUMMONING_YOUR_PET);
			else
				_actor.sendPacket(new SystemMessagePacket(SystemMsg.YOU_USE_S1).addSkillName(skill));
		}

		if(!skill.isHandler() && _actor.isPlayable())
		{
			if(skill.getItemConsumeId() > 0 && skill.getItemConsume() > 0)
			{
				if(skill.isItemConsumeFromMaster())
				{
					Player master = _actor.getPlayer();
					if(master != null)
						master.consumeItem(skill.getItemConsumeId(), skill.getItemConsume(), true);
				}
				else
					_actor.consumeItem(skill.getItemConsumeId(), skill.getItemConsume(), true);
			}
		}

		/*Location flyLoc = null;
		switch(skill.getFlyType())
		{
			case CHARGE:
				flyLoc = _actor.getFlyLocation(aimingTarget, skill);
				if(flyLoc != null)
					_actor.broadcastPacket(new FlyToLocation(_actor, flyLoc, skill.getFlyType(), skill.getFlySpeed(), skill.getFlyDelay(), skill.getFlyAnimationSpeed()));
				break;
			case WARP_BACK:
			case WARP_FORWARD:
				flyLoc = _actor.getFlyLocation(_actor, skill);
				if(flyLoc != null)
					_actor.broadcastPacket(new FlyToLocation(_actor, flyLoc, skill.getFlyType(), (skill.getFlyRadius() / hitTime) * 1000, skill.getFlyDelay(), skill.getFlyAnimationSpeed()));
			break;
		}*/

		if(criticalBlow)
			_isCriticalBlow = true;

		/*if(flyLoc != null)
			_flyLoc = flyLoc;*/

		_castLeftTime = hitTime - hitCancelTime;

		_skillTask = ThreadPoolManager.getInstance().schedule(new MagicLaunchedTask(), hitCancelTime);

		if(skill.isChanneling())
			_skillTickTask = ThreadPoolManager.getInstance().schedule(() -> onMagicTickTimer(), skill.getChannelingStart());

		skill.onStartCast(skillEntry, _actor, aimingTarget);
		_actor.useTriggers(aimingTarget, TriggerType.ON_START_CAST, null, skill, 0);
		return true;
	}

	private void onMagicTickTimer()
	{
		final SkillEntry skillEntry = getSkillEntry();
		if(skillEntry == null)
			return;

		final Creature aimingTarget = getTarget();
		final Skill skill = skillEntry.getTemplate();
		final List<Creature> targets = skill.getTargets(_actor, aimingTarget, skillEntry, _forceUse, true, false);
		if (targets == null) {
			return;
		}

		_targets = targets;

		if(!skill.isNotBroadcastable())
			_actor.broadcastPacket(new MagicSkillLaunchedPacket(_actor.getObjectId(), skillEntry.getDisplayId(), skillEntry.getDisplayLevel(), targets, _castingType));

		double mpConsumeTick = skill.getMpConsumeTick();
		if(mpConsumeTick > 0)
		{
			/* should work like this ?
			if(skill.isMusic())
			{
				double inc = mpConsumeTick / 2;
				double add = 0;
				for(Abnormal e : _actor.getAbnormalList())
				{
					if(e.getSkill().getId() != skillEntry.getId() && e.getSkill().isMusic() && e.getTimeLeft() > 30)
						add += inc;
				}
				mpConsumeTick += add;
				mpConsumeTick = _actor.getStat().getValue(DoubleStat.MP_DANCE_SKILL_CONSUME, mpConsumeTick, aimingTarget, skill);
			}
			else if(skill.isMagic())
				mpConsumeTick = _actor.getStat().getValue(DoubleStat.MP_MAGIC_SKILL_CONSUME, mpConsumeTick, aimingTarget, skill);
			else
				mpConsumeTick = _actor.getStat().getValue(DoubleStat.MP_PHYSICAL_SKILL_CONSUME, mpConsumeTick, aimingTarget, skill);*/

			if(_actor.getCurrentMp() < mpConsumeTick && _actor.isPlayable())
			{
				_actor.sendPacket(SystemMsg.YOUR_SKILL_WAS_DEACTIVATED_DUE_TO_LACK_OF_MP);
				_actor.broadcastPacket(new MagicSkillCanceled(_actor.getObjectId()));
				_actor.sendPacket(ActionFailPacket.get(getCastingType())); // send an "action failed" packet to the caster
				onCastEndTime(false);
				return;
			}
			_actor.reduceCurrentMp(mpConsumeTick, null);
		}

		skill.onTickCast(skillEntry, _actor, targets);
		_actor.useTriggers(aimingTarget, TriggerType.ON_TICK_CAST, null, skill, 0);

		if(skill.getTickInterval() > 0)
			_skillTickTask = ThreadPoolManager.getInstance().schedule(() -> onMagicTickTimer(), skill.getTickInterval());
	}

	private void onMagicUseTimer()
	{
		final SkillEntry skillEntry = getSkillEntry();
		if(skillEntry == null)
		{
			_actor.broadcastPacket(new MagicSkillCanceled(_actor.getObjectId()));
			_actor.sendPacket(ActionFailPacket.get(getCastingType())); // send an "action failed" packet to the caster
			clearVars();
			return;
		}

		Skill skill = skillEntry.getTemplate();
		/*switch(skill.getFlyType())
		{
			case CHARGE:
			case WARP_BACK:
			case WARP_FORWARD:
				if(_flyLoc != null)
					_actor.setLoc(_flyLoc);
				break;
		}*/

		if(!skill.isBad() && _actor.getAggressionTarget() != null)
			_forceUse = true;

		List<Creature> targets = getTargets();

		skill.checkTargetsEffectiveRange(_actor, targets); // Чистим цели, которые вышли за радиус 'effective_range'

		if(skill.oneTarget() && targets.isEmpty())
		{
			_actor.sendPacket(SystemMsg.THE_DISTANCE_IS_TOO_FAR_AND_SO_THE_CASTING_HAS_BEEN_STOPPED);
			_actor.broadcastPacket(new MagicSkillCanceled(_actor.getObjectId()));
			_actor.sendPacket(ActionFailPacket.get(getCastingType())); // send an "action failed" packet to the caster
			onCastEndTime(false);
			return;
		}

		final Creature aimingTarget = getTarget();

		if(!skillEntry.checkCondition(_actor, aimingTarget, _forceUse, false, false))
		{
			if(skill.hasEffect("i_summon_pet") && _actor.isPlayer())
				_actor.getPlayer().setPetControlItem(null);
			_actor.broadcastPacket(new MagicSkillCanceled(_actor.getObjectId()));
			_actor.sendPacket(ActionFailPacket.get(getCastingType())); // send an "action failed" packet to the caster
			onCastEndTime(false);
			return;
		}

		if(skill.getCastRange() != -2 && !skill.hasEffect("i_holything_possess") && !GeoEngine.canSeeTarget(_actor, aimingTarget))
		{
			_actor.sendPacket(SystemMsg.CANNOT_SEE_TARGET);
			_actor.broadcastPacket(new MagicSkillCanceled(_actor.getObjectId()));
			_actor.sendPacket(ActionFailPacket.get(getCastingType())); // send an "action failed" packet to the caster
			onCastEndTime(false);
			return;
		}

		//must be player for usage with a clan.
		int clanRepConsume = skill.getClanRepConsume();
		if(clanRepConsume > 0)
			_actor.getPlayer().getClan().incReputation(-clanRepConsume, false, "clan skills");

		int fameConsume = skill.getFameConsume();
		if(fameConsume > 0)
			_actor.getPlayer().setFame(_actor.getPlayer().getFame() - fameConsume, "clan skills", true);

		int hpConsume = skill.getHpConsume();
		if(hpConsume > 0)
			_actor.setCurrentHp(Math.max(0, _actor.getCurrentHp() - hpConsume), false);

		double mpConsume2 = _actor.getStat().getMpConsume(skill);
		if(mpConsume2 > 0)
		{
			if(_actor.getCurrentMp() < mpConsume2 && _actor.isPlayable())
			{
				_actor.sendPacket(SystemMsg.NOT_ENOUGH_MP);
				_actor.broadcastPacket(new MagicSkillCanceled(_actor.getObjectId()));
				_actor.sendPacket(ActionFailPacket.get(getCastingType())); // send an "action failed" packet to the caster
				onCastEndTime(false);
				return;
			}
			_actor.reduceCurrentMp(mpConsume2, null);
		}

		_actor.callSkill(aimingTarget, skillEntry, targets, true, false);

		if (skill.getChargeConsume() > 0) {
			int chargesCount = Math.min(skill.getChargeConsume(), _actor.getCharges());
			_actor.decreaseCharges(chargesCount);
		}

		if (skill.getChargeConsume() > 0) {
			int chargesCount = Math.min(skill.getChargeConsume(), _actor.getCharges());
			_actor.decreaseCharges(chargesCount);
		}

		/*if(skill.getSoulConsume() > 0)
		{
			int currentCount = _actor.getSouls(skill.getSoulConsumeType());
			int decreaseCount = Math.min(skill.getSoulConsume(), currentCount);
			_actor.setSouls(currentCount - decreaseCount, skill.getSoulConsumeType());
		}*/

		if(skill.getCondCharges() > 0 && _actor.getCharges() > 0)
		{
			int decreasedForce = skill.getCondCharges();
			if(decreasedForce > 15)
				decreasedForce = 5;
			_actor.setCharges(_actor.getCharges() - decreasedForce);
		}

		/*switch(skill.getFlyType())
		{
			// @Rivelia. Targets fly types.
			case THROW_UP:
			case THROW_HORIZONTAL:
			case PUSH_HORIZONTAL:
			case PUSH_DOWN_HORIZONTAL:
				for(Creature target : targets)
				{
					Location flyLoc = target.getFlyLocation(_actor, skill);
					if(flyLoc == null)
						_log.atWarning().log( "%s have null flyLoc.", skill.getFlyType() );
					else
					{
						target.broadcastPacket(new FlyToLocation(target, flyLoc, skill.getFlyType(), skill.getFlySpeed(), skill.getFlyDelay(), skill.getFlyAnimationSpeed()));
						target.setLoc(flyLoc);
					}
				}
				break;
			// @Rivelia. Caster fly types.
			case DUMMY:
				Creature dummyTarget = aimingTarget;
				if(skill.getTargetTypeNew() == TargetType.NONE)
					dummyTarget = _actor;
				else if(skill.getTargetType() == SkillTargetType.TARGET_AURA)
					dummyTarget = _actor;

				Location flyLoc = _actor.getFlyLocation(dummyTarget, skill);
				if(flyLoc != null)
				{
					_actor.broadcastPacket(new FlyToLocation(_actor, flyLoc, skill.getFlyType(), skill.getFlySpeed(), skill.getFlyDelay(), skill.getFlyAnimationSpeed()));
					_actor.setLoc(flyLoc);
				}
				*//*else
					sendPacket(SystemMsg.CANNOT_SEE_TARGET);*//*
				break;
		}*/

		// @Rivelia.
		int skillCoolTime = 0;
		int chargeAddition = 0;

		// @Rivelia. Add the fly speed in the skill cooltime to make the travelling end before the creature can take action again.
		// TODO
		//if(skill.getFlyType() == FlyType.CHARGE && skill.getFlySpeed() > 0)
		//	chargeAddition = (_actor.getDistance(aimingTarget) / skill.getFlySpeed()) * 1000;
		
		if(!skill.isSkillTimePermanent())
			skillCoolTime = Formulas.INSTANCE.calcSkillCastSpd(_actor, skill, skill.getCoolTime() + chargeAddition);
		else
			skillCoolTime = skill.getCoolTime() + chargeAddition;

		if(skillCoolTime > 0)
			ThreadPoolManager.getInstance().schedule(() -> onCastEndTime(true), skillCoolTime);
		else
			onCastEndTime(true);
	}

	private void onCastEndTime(boolean success)
	{
		final SkillEntry skillEntry = getSkillEntry();
		final Creature target = getTarget();
		final List<Creature> targets = getTargets();

		if (!isCastingNow()) {
			return;
		}

		clearVars();

		_actor.onCastEndTime(skillEntry, target, targets, success);
	}

	/**
	 * @return {@code true} if casting can be aborted through regular means such as cast break while being attacked or while cancelling target, {@code false} otherwise.
	 */
	public boolean canAbortCast()
	{
		return _targets == null;
	}

	public boolean abortCast(boolean force)
	{
		if(isCastingNow() && (force || canAbortCast()))
		{
			final SkillEntry skillEntry = getSkillEntry();
			if(skillEntry != null)
			{
				clearVars();
				return true;
			}
		}
		return false;
	}

	public boolean abortCast()
	{
		if(isCastingNow())
		{
			final SkillEntry skillEntry = getSkillEntry();
			if(skillEntry != null)
			{
				clearVars();
				return true;
			}
		}
		return false;
	}

	private void clearVars()
	{
		_isCastingNow.set(false);
		_target = HardReferences.emptyRef();
		_targets = null;
		_forceUse = false;
		_castLeftTime = 0;
		_animationEndTime = 0;
		_skillEntry = null;
		_isCriticalBlow = false;
		if(_skillTask != null)
		{
			_skillTask.cancel(false);
			_skillTask = null;
		}
		if(_skillTickTask != null)
		{
			_skillTickTask.cancel(false);
			_skillTickTask = null;
		}
		_flyLoc = null;
	}

	private void handleSkillFly(Creature creature, GameObject target, SkillEntry skillEntry)
	{
		Skill skill = skillEntry.getTemplate();

		double x = 0;
		double y = 0;
		double z = 0;
		FlyType flyType = FlyType.CHARGE;

		switch (skill.getOperateType())
		{
			case DA4:
			case DA5:
			{
				final double course = skill.getOperateType() == SkillOperateType.DA4 ? Math.toRadians(270) : Math.toRadians(90);
				final double radian = Math.toRadians(PositionUtils.convertHeadingToDegree(target.getHeading()));
				double nRadius = creature.getCollisionRadius();
				if (target.isCreature())
				{
					nRadius += target.getCollisionRadius();
				}
				x = target.getX() + (Math.cos(Math.PI + radian + course) * nRadius);
				y = target.getY() + (Math.sin(Math.PI + radian + course) * nRadius);
				z = target.getZ();
				break;
			}
			case DA3:
			{
				flyType = FlyType.WARP_BACK;
				final double radian = Math.toRadians(PositionUtils.convertHeadingToDegree(creature.getHeading()));
				x = creature.getX() + (Math.cos(Math.PI + radian) * skill.getCastRange());
				y = creature.getY() + (Math.sin(Math.PI + radian) * skill.getCastRange());
				z = creature.getZ();
				break;
			}
			case DA2:
			case DA1:
			{
				if (creature == target)
				{
					final double course = Math.toRadians(180);
					final double radian = Math.toRadians(PositionUtils.convertHeadingToDegree(creature.getHeading()));
					x = creature.getX() + (int) (Math.cos(Math.PI + radian + course) * skill.getCastRange());
					y = creature.getY() + (int) (Math.sin(Math.PI + radian + course) * skill.getCastRange());
					z = creature.getZ();
				}
				else
				{
					final double dx = target.getX() - creature.getX();
					final double dy = target.getY() - creature.getY();
					final double distance = Math.sqrt((dx * dx) + (dy * dy));
					double nRadius = creature.getCollisionRadius();
					if (target.isCreature())
					{
						nRadius += target.getCollisionRadius();
					}
					x = (int) (target.getX() - (nRadius * (dx / distance)));
					y = (int) (target.getY() - (nRadius * (dy / distance)));
					z = target.getZ();
				}
				break;
			}
		}

		creature.setUsingFlyingSkill(true);

		//final Location destination = GeoEngine.moveCheck(creature.getX(), creature.getY(), creature.getZ(), (int) x, (int) y, (int) z, creature.getGeoIndex());
		Location destination = GeoEngine.moveCheck(creature.getX(), creature.getY(), (int) z, (int) x, (int) y, creature.getGeoIndex());
		if (destination == null) {
			destination = creature.getLoc();
		}

		creature.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		creature.broadcastPacket(new FlyToLocation(creature, destination, flyType, 0, 0, 333));
		creature.setLoc(destination);
		//creature.broadcastPacket(new ValidateLocationPacket(creature));

		ThreadPoolManager.getInstance().schedule(() -> {
				creature.setUsingFlyingSkill(false);
		}, 60, TimeUnit.SECONDS);
	}
}