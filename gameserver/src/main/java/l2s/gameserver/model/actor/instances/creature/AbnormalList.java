package l2s.gameserver.model.actor.instances.creature;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.ArrayUtils;

import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.skills.AbnormalType;
import l2s.gameserver.skills.EffectType;
import l2s.gameserver.skills.EffectUseType;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.skillclasses.Transformation;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.stats.funcs.FuncTemplate;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class AbnormalList implements Iterable<Abnormal>
{
    public static final int NONE_SLOT_TYPE = -1;
    public static final int BUFF_SLOT_TYPE = 0;
    public static final int MUSIC_SLOT_TYPE = 1;
    public static final int TRIGGER_SLOT_TYPE = 2;
    public static final int DEBUFF_SLOT_TYPE = 3;

    private final Collection<Abnormal> _effects;
    private final Creature _owner;

    private static final List<Integer> NEXT_ABNORMALS_SKILLS_SAVE_LIST = List.of(1363, 1414, 1355, 1356, 1357, 1045);
    private Multimap<Integer, Abnormal> nextAbnormals = Multimaps.newListMultimap(
            new ConcurrentHashMap<>(1), CopyOnWriteArrayList::new);

    private final Map<Abnormal, Long> debuffs = new HashMap<>();

    public AbnormalList(Creature owner)
    {
        _effects = new ConcurrentLinkedQueue<>();
        _owner = owner;
    }

    public static int getSlotType(Abnormal e)
    {
        if(e.getSkill().getBuffSlotType() != -2)
            return e.getSkill().getBuffSlotType();
        if(e.isHidden() || e.getSkill().isPassive() || e.getSkill().isToggle() || e.getSkill() instanceof Transformation || e.checkAbnormalType(AbnormalType.hp_recover))
            return -1;
        if(e.isOffensive())
            return 3;
        if(e.getSkill().isMusic())
            return 1;
        if(e.getSkill().isTrigger())
            return 2;
        return 0;
    }

    public static boolean checkAbnormalType(EffectTemplate ef1, EffectTemplate ef2)
    {
        AbnormalType abnormalType1 = ef1.getAbnormalType();
        if(abnormalType1 == AbnormalType.none)
            return false;
        AbnormalType abnormalType2 = ef2.getAbnormalType();
        return abnormalType2 != AbnormalType.none && abnormalType1 == abnormalType2;
    }

    public boolean containsEffects(int skillId)
    {
        if(_effects.isEmpty())
            return false;
        for(Abnormal e : _effects)
            if(e.getSkill().getId() == skillId)
                return true;
        return false;
    }

    public boolean containsEffects(Skill skill)
    {
        return skill != null && containsEffects(skill.getId());
    }

    public boolean containsEffects(EffectType et)
    {
        if(et == null)
            return false;
        for(Abnormal e : _effects)
            if(e.getEffectType() == et)
                return true;
        return false;
    }

    public boolean containsEffects(AbnormalType type)
    {
        if(type == null)
            return false;
        for(Abnormal e : _effects)
            if(e.getAbnormalType() == type)
                return true;
        return false;
    }

    public Collection<Abnormal> getEffects()
    {
        return _effects;
    }

    public int getEffectsCount(int skillId)
    {
        if(_effects.isEmpty())
            return 0;
        int result = 0;
        for(Abnormal e : _effects)
            if(e.getSkill().getId() == skillId)
                ++result;
        return result;
    }

    public int getEffectsCount(Skill skill)
    {
        if(skill == null)
            return 0;
        return getEffectsCount(skill.getId());
    }

    public int getEffectsCount(AbnormalType type)
    {
        if(_effects.isEmpty())
            return 0;
        int result = 0;
        for(Abnormal e : _effects)
            if(type == e.getAbnormalType())
                ++result;
        return result;
    }

    public int getEffectsCount(EffectType et)
    {
        if(_effects.isEmpty())
            return 0;
        int result = 0;
        for(Abnormal e : _effects)
            if(e.getEffectType() == et)
                ++result;
        return result;
    }

    public int getEffectsCount()
    {
        return _effects.size();
    }

    public boolean isEmpty()
    {
        return _effects.isEmpty();
    }

    public Abnormal[] getFirstEffects()
    {
        Abnormal[] result;
        if(!_effects.isEmpty())
        {
            TIntObjectMap<Abnormal> map = new TIntObjectHashMap<>();
            for(Abnormal e : _effects)
                if(!e.isHidden())
                    map.put(e.getSkill().getId(), e);
            result = map.values(new Abnormal[map.size()]);
        }
        else
            result = Abnormal.EMPTY_L2EFFECT_ARRAY;
        return result;
    }

    private void checkSlotLimit(Abnormal newEffect)
    {
        if(_effects.isEmpty())
            return;
        int slotType = getSlotType(newEffect);
        if(slotType == -1)
            return;
        int size = 0;
        TIntSet skillIds = new TIntHashSet();
        for(Abnormal e : _effects)
        {
            if(e.getSkill().equals(newEffect.getSkill()))
                return;
            if(skillIds.contains(e.getSkill().getId()))
                continue;
            int subType = getSlotType(e);
            if(subType != slotType)
                continue;
            ++size;
            skillIds.add(e.getSkill().getId());
        }
        int limit = 0;
        switch(slotType)
        {
            case 0:
            {
                limit = _owner.getBuffLimit();
                break;
            }
            case 1:
            {
                limit = Config.ALT_MUSIC_LIMIT;
                break;
            }
            case 3:
            {
                limit = Config.ALT_DEBUFF_LIMIT;
                break;
            }
            case 2:
            {
                limit = Config.ALT_TRIGGER_LIMIT;
                break;
            }
        }
        if(size < limit)
            return;
        for(Abnormal e2 : _effects)
            if(getSlotType(e2) == slotType)
            {
                stopEffects(e2.getSkill().getId());
                break;
            }
    }

    public synchronized boolean addEffect(Abnormal effect)
    {
        if(!effect.isTimeLeft())
            return false;
        double hp = _owner.getCurrentHp();
        double mp = _owner.getCurrentMp();
        double cp = _owner.getCurrentCp();
        _owner.getStatsRecorder().block();
        Skill effectSkill = effect.getSkill();
        if(!_effects.isEmpty())
        {
            AbnormalType abnormalType = effect.getAbnormalType();
            if(abnormalType == AbnormalType.none)
            {
                for(Abnormal e : _effects)
                    if(e.getAbnormalType() == AbnormalType.none && e.getSkill().getId() == effectSkill.getId() && e.getIndex() == effect.getIndex())
                        if(e.isOfUseType(EffectUseType.START) || e.isOfUseType(EffectUseType.TICK))
                        {
                            if(effect.getEffector() != e.getEffector())
                                continue;
                            e.exit();
                        }
                        else
                        {
                            if(effect.getTimeLeft() <= e.getTimeLeft())
                            {
                                _owner.getStatsRecorder().unblock();
                                return false;
                            }
                            e.exit();
                        }
            }
            else
                for(Abnormal e : _effects)
                {
                    if(e.checkBlockedAbnormalType(abnormalType))
                    {
                        _owner.getStatsRecorder().unblock();

                        effect.scheduleForReApply(e.getTimeLeft() * 1000 + 10);

                        return false;
                    }
                    if(effect.checkBlockedAbnormalType(e.getAbnormalType()))
                        e.exit();
                    else
                    {
                        if(e.getEffector() != effect.getEffector() && effect.getAbnormalType().isStackable())
                            continue;
                        if(!checkAbnormalType(e.getTemplate(), effect.getTemplate()))
                            continue;
                        if(e.getSkill() == effectSkill && e.getIndex() != effect.getIndex())
                            break;
                        if(e.getAbnormalLvl() == -1)
                        {
                            _owner.getStatsRecorder().unblock();
                            return false;
                        }
                        if(e.maybeScheduleNext(effect))
                        {
                            // если на игроке висит Chant of Victory(1363) или Victories of Paagrio(1414),
                            // и персонаж накладывает на себя Over the Body (536)
                            // то по истечению его времени возвращаются эти скиллы.
                            if((effectSkill.getId() == 536 || effectSkill.getId() == 121)
                                    && NEXT_ABNORMALS_SKILLS_SAVE_LIST.contains(e.getSkill().getId())
                                    && !nextAbnormals.containsKey(effectSkill.getId()))
                            {
                                e.stop();

                                nextAbnormals.put(effectSkill.getId(), e);
                                continue;
                            }
                            else
                            {
                                e.exit();
                                continue;
                            }
                        }
                        else
                        {
                            _owner.getStatsRecorder().unblock();
                            return false;
                        }
                    }
                }
            checkSlotLimit(effect);
        }
        boolean success = false;

        if(_effects.add(effect))
        {
            effect.start();
            success = true;
        }
        for(FuncTemplate ft : effect.getTemplate().getAttachedFuncs())
            if(ft._stat == Stats.MAX_HP)
                _owner.setCurrentHp(hp, false);
            else if(ft._stat == Stats.MAX_MP)
                _owner.setCurrentMp(mp);
            else if(ft._stat == Stats.MAX_CP)
                _owner.setCurrentCp(cp);
        _owner.getStatsRecorder().unblock();
        onChange();

        if(effectSkill.isDebuff())
            debuffs.put(effect, effect.getStartTime());

        return success;
    }

    public void removeEffect(Abnormal effect)
    {
        if(effect == null)
            return;
        if(_effects.remove(effect))
            onChange();

        Collection<Abnormal> abnormals = nextAbnormals.removeAll(effect.getSkill().getId());
        abnormals.forEach(this::addEffect);
    }

    public int stopAllEffects()
    {
        if(_effects.isEmpty())
            return 0;
        int removed = 0;
        for(Abnormal e : _effects)
        {
            if(_owner.isSpecialEffect(e.getSkill()))
                continue;
            e.exit();
            ++removed;
        }
        if(removed > 0)
            onChange();

        clearDebuffs();

		if (_owner.isPhantom())
			_owner.getPlayer().phantom_params.setNeedRebuff(true);
		
        return removed;
    }

    public int stopEffects(int skillId)
    {
        if(_effects.isEmpty())
            return 0;
        int removed = 0;
        for(Abnormal e : _effects)
            if(e.getSkill().getId() == skillId)
            {
                e.exit();
                ++removed;
            }
        if(removed > 0)
            onChange();
        return removed;
    }

    public int stopEffects(TIntSet skillIds)
    {
        if(_effects.isEmpty())
            return 0;
        int removed = 0;
        for(Abnormal e : _effects)
            if(skillIds.contains(e.getSkill().getId()))
            {
                e.exit();
                ++removed;
            }
        if(removed > 0)
            onChange();
        return removed;
    }

    @Deprecated
    public int stopEffects(Skill skill)
    {
        if(skill == null)
            return 0;
        return stopEffects(skill.getId());
    }

    public int stopEffects(SkillEntry skill)
    {
        if(skill == null)
            return 0;
        return stopEffects(skill.getId());
    }

    public int stopEffects(EffectType type)
    {
        return stopEffects(type, null);
    }

    public int stopEffects(EffectType type, Skill ignoreSkill)
    {
        if(_effects.isEmpty()) {
            return 0;
        }

        int removed = 0;
        for(Abnormal e : _effects) {
            if((ignoreSkill == null || e.getSkill() != ignoreSkill) && e.getEffectType() == type) {
                e.exit();
                ++removed;
            }
        }

        if(removed > 0) {
            onChange();
        }

        return removed;
    }

    public Map<Abnormal, Long> getDebuffs()
    {
        return debuffs;
    }

    public void clearDebuffs()
    {
        debuffs.clear();
    }

    private void onChange()
    {
        _owner.updateStats();
        _owner.updateEffectIcons();
    }

    @Override
    public Iterator<Abnormal> iterator()
    {
        return _effects.iterator();
    }
    
  	public Abnormal getEffectBySkillId(int skillId)
  	{
  		if (_effects == null)
  			return null;
  		for(Abnormal e : _effects)
  			if (e.getSkill().getId() == skillId)
  				return e;
  		return null;
  	}

  	public boolean containEffectFromSkills(int[] skillIds)
  	{
  		if (isEmpty())
  			return false;
  		
  		int skillId;
  		for(Abnormal e : _effects)
  		{
  			skillId = e.getSkill().getId();
  			if (ArrayUtils.contains(skillIds, skillId))
  				return true;
  		}
  		
  		return false;
  	}
}
