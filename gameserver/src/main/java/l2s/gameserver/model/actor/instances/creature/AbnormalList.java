package l2s.gameserver.model.actor.instances.creature;

import com.google.common.flogger.FluentLogger;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import l2s.gameserver.Config;
import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.AbnormalTypeList;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.SkillBuffType;
import l2s.gameserver.skills.AbnormalType;
import l2s.gameserver.skills.EffectUseType;
import l2s.gameserver.skills.SkillInfo;
import l2s.gameserver.templates.OptionDataTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

/**
 * @reworked by Bonux
**/
public final class AbnormalList implements Iterable<Abnormal>
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	public static final int NONE_SLOT_TYPE = -1;
	public static final int BUFF_SLOT_TYPE = 0;
	public static final int MUSIC_SLOT_TYPE = 1;
	public static final int TRIGGER_SLOT_TYPE = 2;
	public static final int DEBUFF_SLOT_TYPE = 3;

	private final Collection<Abnormal> _abnormals = new ConcurrentLinkedQueue<Abnormal>();

	/** Set containing all {@code AbnormalType}s that shouldn't be added to this creature effect list. */
	private volatile Set<AbnormalType> blockedAbnormalTypes = null;

	private final Creature _owner;

	private final Lock _addAbnormalLock = new ReentrantLock();

	public AbnormalList(Creature owner)
	{
		_owner = owner;
	}

	@Override
	public Iterator<Abnormal> iterator()
	{
		return _abnormals.iterator();
	}

	public boolean contains(int skillId)
	{
		if(_abnormals.isEmpty())
			return false;

		for(Abnormal abnormal : _abnormals)
		{
			if(abnormal.getSkill().getId() == skillId)
				return true;
		}
		return false;
	}

	public boolean contains(SkillInfo skillInfo)
	{
		if(skillInfo == null)
			return false;
		return contains(skillInfo.getId());
	}

	public boolean contains(AbnormalType type)
	{
		if(type == null)
			return false;

		return _abnormals.stream()
				.anyMatch(abnormal -> abnormal.getAbnormalTypeList().contains(type));
	}

	public boolean contains(AbnormalType type, Predicate<Abnormal> filter)
	{
		if(type == null)
			return false;

		return _abnormals.stream()
				.anyMatch(abnormal -> abnormal.getAbnormalTypeList().contains(type) && filter.test(abnormal));
	}

	public Collection<Abnormal> values()
	{
		return _abnormals;
	}

	public Abnormal[] toArray()
	{
		return _abnormals.toArray(new Abnormal[_abnormals.size()]);
	}

	public int getCount(int skillId)
	{
		int result = 0;

		if(_abnormals.isEmpty())
			return 0;

		for(Abnormal abnormal : _abnormals)
		{
			if(abnormal.getSkill().getId() == skillId)
				result++;
		}
		return result;
	}

	public int getCount(SkillInfo skillInfo)
	{
		if(skillInfo == null)
			return 0;
		return getCount(skillInfo.getId());
	}

	public int getCount(AbnormalType type)
	{
		if(_abnormals.isEmpty())
			return 0;

		return (int) _abnormals.stream()
				.filter(abnormal -> abnormal.getAbnormalTypeList().contains(type))
				.count();
	}

	/**
	 * Gets the buffs count without including the hidden buffs (after getting an Herb buff).<br>
	 * Prevents initialization.
	 * @return the number of buffs in this creature effect list
	 */
	public int getBuffCount()
	{
		if(_abnormals.isEmpty())
			return 0;

		// TODO decrease by hidden buffs
		return (int) _abnormals.stream()
				.filter(abnormal -> abnormal.getSkill().getBuffType() == SkillBuffType.BUFF && !abnormal.isHidden())
				.count();
	}

	/**
	 * Gets the debuff skills count.<br>
	 * Prevents initialization.
	 * @return the number of debuff effects in this creature effect list
	 */
	public int getDebuffCount()
	{
		if(_abnormals.isEmpty())
			return 0;

		return (int) _abnormals.stream()
				.filter(abnormal -> abnormal.getSkill().getBuffType() == SkillBuffType.DEBUFF && !abnormal.isHidden())
				.count();
	}

	public int getAbnormalLevel(AbnormalType type)
	{

		if(_abnormals.isEmpty())
			return -1;

		return _abnormals.stream()
				.filter(abnormal -> abnormal.getAbnormalTypeList().contains(type))
				.mapToInt(Abnormal::getAbnormalLvl)
				.filter(abnormal -> abnormal >= -1)
				.max()
				.orElse(-1);
	}

	public int getAbnormalLevel(int skillId)
	{
		if(_abnormals.isEmpty())
			return -1;

		return _abnormals.stream()
				.filter(abnormal -> abnormal.getSkill().getId() == skillId)
				.mapToInt(Abnormal::getAbnormalLvl)
				.filter(abnormal -> abnormal >= -1)
				.max()
				.orElse(-1);
	}

	public int size()
	{
		return _abnormals.size();
	}

	public boolean isEmpty()
	{
		return _abnormals.isEmpty();
	}

	private void checkSlotLimit(Abnormal newAbnormal)
	{
		if(_abnormals.isEmpty())
			return;

		int slotType = getSlotType(newAbnormal);
		if(slotType == NONE_SLOT_TYPE)
			return;

		int size = 0;
		TIntSet skillIds = new TIntHashSet();
		for(Abnormal e : _abnormals)
		{
			if(e.getSkill().equals(newAbnormal.getSkill())) // мы уже имеем эффект от этого скилла
				return;

			if(!skillIds.contains(e.getSkill().getId()))
			{
				int subType = getSlotType(e);
				if(subType == slotType)
				{
					size++;
					skillIds.add(e.getSkill().getId());
				}
			}
		}

		int limit = 0;
		switch(slotType)
		{
			case BUFF_SLOT_TYPE:
				limit = _owner.getStat().getMaxBuffCount();
				break;
			case MUSIC_SLOT_TYPE:
				limit = Config.ALT_MUSIC_LIMIT;
				break;
			case DEBUFF_SLOT_TYPE:
				limit = Config.ALT_DEBUFF_LIMIT;
				break;
			case TRIGGER_SLOT_TYPE:
				limit = Config.ALT_TRIGGER_LIMIT;
				break;
		}

		if(size < limit)
			return;

		for(Abnormal e : _abnormals)
		{
			if(getSlotType(e) == slotType)
			{
				stop(e.getSkill().getId());
				break;
			}
		}
	}

	public static int getSlotType(Abnormal e)
	{
		switch (e.getSkill().getBuffType()) {
			case BUFF:
				return BUFF_SLOT_TYPE;
			case DEBUFF:
				return DEBUFF_SLOT_TYPE;
			case DANCE:
				return MUSIC_SLOT_TYPE;
			case TRIGGER:
				return TRIGGER_SLOT_TYPE;
			case NONE:
			case TOGGLE:
			default:
				return NONE_SLOT_TYPE;
		}
	}

	public static boolean checkAbnormalType(Skill skill1, Skill skill2)
	{
		AbnormalTypeList abnormalTypeList1 = skill1.getAbnormalTypeList();
		if(abnormalTypeList1.isNone())
			return false;

		AbnormalTypeList abnormalTypeList2 = skill2.getAbnormalTypeList();
		if(abnormalTypeList2.isNone())
			return false;

		return abnormalTypeList1.containsAnyOf(abnormalTypeList2);
	}

	public boolean add(Abnormal abnormal) {
		return add(abnormal, true);
	}

	public boolean add(Abnormal abnormal, boolean update)
	{
		if(!abnormal.isTimeLeft())
			return false;

		Skill skill = abnormal.getSkill();
		if(skill == null)
			return false;

		boolean success = false;
		try
		{
			if(!_addAbnormalLock.tryLock(1000, TimeUnit.MILLISECONDS))
				return false;

			_owner.getStatsRecorder().block(); // Для того, чтобы не флудить пакетами.
			try
			{
				//TODO [G1ta0] затычка на статы повышающие HP/MP/CP
				/*double hp = _owner.getCurrentHp();
				double mp = _owner.getCurrentMp();
				double cp = _owner.getCurrentCp();*/

				boolean suspended = false;

				if(!_abnormals.isEmpty() && (abnormal.isOfUseType(EffectUseType.NORMAL) || abnormal.isOfUseType(EffectUseType.SELF)))
				{
					if(skill.isToggle())
					{
						if(contains(skill))
							return false;

						if(skill.isToggleGrouped() && skill.getToggleGroupId() > 0)
						{
							for(Abnormal a : _abnormals)
							{
								if(!a.getSkill().isToggleGrouped())
									continue;

								if(skill.getToggleGroupId() == a.getSkill().getToggleGroupId())
								{
									if(!_owner.isDualCastEnable() || a.getSkill().getToggleGroupId() != 1)
										a.exit();
								}
							}
						}
					}
					else
					{
						AbnormalTypeList abnormalTypeList = abnormal.getAbnormalTypeList();
						if(abnormalTypeList.isNone())
						{
							// Удаляем такие же эффекты
							for(Abnormal a : _abnormals)
							{
								if(a.getSkill().getId() == skill.getId())
								{
									// Если оставшаяся длительность старого эффекта больше чем длительность нового, то оставляем старый.
									/*if(abnormal.getTimeLeft() > a.getTimeLeft()) // Отключено для теста, вроде-бы так будет по оффу - отключенным.
										a.exit();
									// Если у старого эффекта уровень ниже, чем у нового, то заменяем новым.
									else */if(skill.getLevel() >= a.getSkill().getLevel())
										a.exit();
									else
										return false;
								}
							}
						}
						else
						{
							if (abnormalTypeList.containsAnyOf(getBlockedAbnormalTypes())) {
								return false;
							}

							// Проверяем, нужно ли накладывать эффект, при совпадении StackType.
							// Новый эффект накладывается только в том случае, если у него больше StackOrder и больше длительность.
							// Если условия подходят - удаляем старый.
							for(Abnormal a : _abnormals)
							{
								/* remove ?
								if(a.checkBlockedAbnormalType(abnormalTypeList))
									return false;

								if(abnormal.checkBlockedAbnormalType(a.getAbnormalTypeList()))
								{
									a.exit();
									continue;
								}*/

								if (a.getEffector() != abnormal.getEffector())
									if (abnormalTypeList.all(AbnormalType::isStackable)) {
										continue;
									}

								if(!checkAbnormalType(a.getSkill(), skill))
									continue;

								if(a.getSkill().isIrreplaceableBuff())
									return false;

								/*if(abnormal.getAbnormalLvl() == a.getAbnormalLvl()) // Отключено для теста, вроде-бы так будет по оффу - отключенным.
								{
									if(skill.getTargetType() != SkillTargetType.TARGET_SELF && abnormal.getTimeLeft() < a.getTimeLeft())
										return false;
								}
								else */if(abnormal.getAbnormalLvl() < a.getAbnormalLvl())
								{
									if(a.getSkill().isAbnormalInstant() && !skill.isAbnormalInstant())
									{
										suspended = true;
										break;
									}
									return false;
								}

								if(!a.getSkill().isAbnormalInstant() && skill.isAbnormalInstant())
									a.suspend();
								else
									a.exit();

								break;
							}
						}

						// Проверяем на лимиты бафов/дебафов
						checkSlotLimit(abnormal);
					}
				}

				success = _abnormals.add(abnormal);

				if(success)
				{
					if(!suspended)
						abnormal.start(); // Запускаем эффект
					else
						abnormal.suspend(); // Запускаем эффект в пассивном режиме
				}

				//TODO [G1ta0] затычка на статы повышающие HP/MP/CP
				/* TODO still needed ?
				for(EffectHandler effect : abnormal.getEffects())
				{
					for(FuncTemplate ft : effect.getTemplate().getAttachedFuncs())
					{
						if(ft._stat == DoubleStat.MAX_HP)
							_owner.setCurrentHp(hp, false);
						else if(ft._stat == DoubleStat.MAX_MP)
							_owner.setCurrentMp(mp);
						else if(ft._stat == DoubleStat.MAX_CP)
							_owner.setCurrentCp(cp);
					}
				}*/
			}
			finally
			{
				_owner.getStatsRecorder().unblock();
			}

			if (update) {
				_owner.getStat().recalculateStats(false);
			}
			_owner.updateAbnormalIcons();
		}
		catch(InterruptedException e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "Error while adding new abnormal: %s", e );
			return false;
		}
		finally
		{
			_addAbnormalLock.unlock();
		}
		return success;
	}

	/**
	 * Удаление эффекта из списка
	 *
	 * @param abnormal эффект для удаления
	 */
	public void remove(Abnormal abnormal) {
		remove(abnormal, true);
	}

	/**
	 * Удаление эффекта из списка
	 *
	 * @param abnormal эффект для удаления
	 */
	public void remove(Abnormal abnormal, boolean update)
	{
		if(abnormal == null)
			return;

		if(_abnormals.remove(abnormal))
		{
			Skill skill = abnormal.getSkill();
			if(skill.isAbnormalInstant())
			{
				for(Abnormal a : _abnormals)
				{
					if(a.getAbnormalTypeList().containsAnyOf(abnormal.getAbnormalTypeList()))
					{
						if(a.isSuspended())
						{
							a.start();
							break;
						}
					}
				}
			}

			if (update) {
				_owner.getStat().recalculateStats(false);
			}
			_owner.updateAbnormalIcons();

			if (skill.getAbnormalTypeList().contains(AbnormalType.ABILITY_CHANGE)) {
				for (Servitor servitor : _owner.getServitors()) {
					servitor.getStat().recalculateStats(false);
				}
			}
		}
	}

	public int stopAll()
	{
		if(_abnormals.isEmpty())
			return 0;

		int removed = 0;
		for(Abnormal a : _abnormals)
		{
			if(_owner.isSpecialAbnormal(a.getSkill()))
				continue;

			a.exit();
			removed++;
		}

		return removed;
	}

	public int stop(int skillId, int skillLvl)
	{
		if(_abnormals.isEmpty())
			return 0;

		int removed = 0;
		for(Abnormal a : _abnormals)
		{
			if(a.getSkill().getId() == skillId && a.getSkill().getLevel() == skillLvl)
			{
				a.exit();
				removed++;
			}
		}

		return removed;
	}

	public int stop(int skillId)
	{
		if(_abnormals.isEmpty())
			return 0;

		int removed = 0;
		for(Abnormal a : _abnormals)
		{
			if(a.getSkill().getId() == skillId)
			{
				a.exit();
				removed++;
			}
		}

		return removed;
	}

	public int stop(TIntSet skillIds)
	{
		if(_abnormals.isEmpty())
			return 0;

		int removed = 0;
		for(Abnormal a : _abnormals)
		{
			if(skillIds.contains(a.getSkill().getId()))
			{
				a.exit(false);
				removed++;
			}
		}

		if (removed > 0) {
			_owner.getStat().recalculateStats(true);
		}

		return removed;
	}

	public int stop(AbnormalType type)
	{
		if(_abnormals.isEmpty())
			return 0;

		int removed = 0;
		for(Abnormal a : _abnormals)
		{
			if(a.getAbnormalTypeList().contains(type))
			{
				a.exit();
				removed++;
			}
		}

		return removed;
	}

	public int stop(SkillInfo skillInfo, boolean checkLevel)
	{
		if(skillInfo == null)
			return 0;

		if(checkLevel)
			return stop(skillInfo.getId(), skillInfo.getLevel());
		return stop(skillInfo.getId());
	}

	/**
	 * Находит скиллы с указанным эффектом, и останавливает у этих скиллов все эффекты (не только указанный).
	 */
	@Deprecated
	public int stop(String name)
	{
		if(_abnormals.isEmpty())
			return 0;

		TIntSet skillIds = new TIntHashSet();
		for(Abnormal abnormal : _abnormals)
		{
			for(EffectHandler effect : abnormal.getEffects())
			{
				if(effect.getName().equalsIgnoreCase(name))
				{
					skillIds.add(effect.getSkill().getId());
					break;
				}
			}
		}

		int removed = 0;
		for(Abnormal abnormal : _abnormals)
		{
			if(skillIds.contains(abnormal.getSkill().getId()))
			{
				abnormal.exit();
				removed++;
			}
		}

		return removed;
	}

	public void stopEffectsOnDamage()
	{
		final TIntSet effectsToRemove = new TIntHashSet();
		for(Abnormal effect : this)
		{
			if(effect.getSkill().isRemovedOnDamage())
				effectsToRemove.add(effect.getSkill().getId());
		}
		stop(effectsToRemove);

		if(_owner.isMeditated())
			stop("Meditation");

		_owner.checkAndRemoveInvisible();
	}

	/**
	 * Stops all active dances/songs skills.
	 * @param update set to true to update the effect flags and icons
	 * @param broadcast {@code true} to broadcast update packets if updating, {@code false} otherwise.
	 */
	public void stopAllOptions(boolean update, boolean broadcast)
	{
		for (Abnormal abnormal : _abnormals) {
			if (abnormal.getOption() == null) {
				continue;
			}

			remove(abnormal, false);
		}

		if (update) {
			_owner.getStat().recalculateStats(broadcast);
		}
	}

	/**
	 * Adds {@code AbnormalType}s to the blocked buff slot set.
	 * @param blockedAbnormalTypes the blocked buff slot set to add
	 */
	public void addBlockedAbnormalTypes(Set<AbnormalType> blockedAbnormalTypes)
	{
		// Initialize
		if (this.blockedAbnormalTypes == null)
		{
			synchronized (this)
			{
				if (this.blockedAbnormalTypes == null)
				{
					this.blockedAbnormalTypes = EnumSet.copyOf(blockedAbnormalTypes);
				}
			}
		}

		this.blockedAbnormalTypes.addAll(blockedAbnormalTypes);
	}

	/**
	 * Removes {@code AbnormalType}s from the blocked buff slot set.
	 * @param blockedBuffSlots the blocked buff slot set to remove
	 * @return {@code true} if the blocked buff slots set has been modified, {@code false} otherwise
	 */
	public boolean removeBlockedAbnormalTypes(Set<AbnormalType> blockedBuffSlots)
	{
		return (blockedAbnormalTypes != null) && blockedAbnormalTypes.removeAll(blockedBuffSlots);
	}

	/**
	 * Gets all the blocked {@code AbnormalType}s for this creature effect list.
	 * @return the current blocked {@code AbnormalType}s set in unmodifiable view.
	 */
	public Set<AbnormalType> getBlockedAbnormalTypes()
	{
		return blockedAbnormalTypes != null ? Collections.unmodifiableSet(blockedAbnormalTypes) : Collections.emptySet();
	}
}