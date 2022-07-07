package l2s.gameserver.templates.skill;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.actor.instances.creature.AbnormalList;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.skills.AbnormalType;
import l2s.gameserver.skills.EffectType;
import l2s.gameserver.skills.EffectUseType;
import l2s.gameserver.skills.effects.i_abstract_effect;
import l2s.gameserver.stats.StatTemplate;
import l2s.gameserver.stats.conditions.Condition;
import l2s.gameserver.templates.StatsSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public final class EffectTemplate extends StatTemplate
{
	private static final Logger _log;
	public static final EffectTemplate[] EMPTY_ARRAY;
	private final int _index;
	private Condition _attachCond;
	private final double _value;
	public final EffectType _effectType;
	private final AbnormalType _abnormalType;
	private final int _abnormalLvl;
	private final AbnormalEffect[] _abnormalEffects;
	private final StatsSet _paramSet;
	private final int _chance;
	private final int _interval;
	private final EffectUseType _useType;

	public EffectTemplate(StatsSet set, Skill skill, EffectUseType useType)
	{
		_effectType = set.getEnum("name", EffectType.class, EffectType.Buff);
		boolean instant = set.getBool("instant", _effectType.getEffectClass() == i_abstract_effect.class || i_abstract_effect.class.isAssignableFrom(_effectType.getEffectClass()));
		if(instant)
			switch(useType)
			{
				case START:
				{
					useType = EffectUseType.START_INSTANT;
					break;
				}
				case TICK:
				{
					useType = EffectUseType.TICK_INSTANT;
					break;
				}
				case SELF:
				{
					useType = EffectUseType.SELF_INSTANT;
					break;
				}
				case NORMAL:
				{
					useType = EffectUseType.NORMAL_INSTANT;
					break;
				}
			}
		_useType = useType;
		_index = (_useType.ordinal() + 1) * 100 + skill.getEffectsCount(_useType) + 1;
		_value = set.getDouble("value", 0.0);
		if(_useType == EffectUseType.NORMAL)
		{
			_abnormalType = skill.getAbnormalType();
			_abnormalLvl = skill.getAbnormalLvl();
			_abnormalEffects = skill.getAbnormalEffects();
		}
		else
		{
			_abnormalType = AbnormalType.none;
			_abnormalLvl = 0;
			_abnormalEffects = new AbnormalEffect[0];
		}
		_interval = set.getInteger("interval", Integer.MAX_VALUE);
		_chance = set.getInteger("chance", -1);
		_paramSet = set;
	}

	public int getIndex()
	{
		return _index;
	}

	public Abnormal getEffect(Creature character, Creature target, Skill skill) {
		return getEffect(character, target, skill, false);
	}

	public Abnormal getEffect(Creature character, Creature target, Skill skill, boolean reflected)
	{
		try
		{
			return _effectType.makeEffect(character, target, skill, reflected, this);
		}
		catch(Exception e)
		{
			_log.error("", e);
			return null;
		}
	}

	public void attachCond(Condition c)
	{
		_attachCond = c;
	}

	public boolean checkCondition(Abnormal effect)
	{
		return _attachCond == null || _attachCond.test(effect.getEffector(), effect.getEffected(), effect.getSkill(),
				null, 0);
	}

	public Condition getCondition()
	{
		return _attachCond;
	}

	public EffectType getEffectType()
	{
		return _effectType;
	}

	public Abnormal getSameByAbnormalType(Collection<Abnormal> list)
	{
		for(Abnormal ef : list)
			if(ef != null && AbnormalList.checkAbnormalType(ef.getTemplate(), this))
				return ef;
		return null;
	}

	public Abnormal getSameByAbnormalType(AbnormalList list)
	{
		return getSameByAbnormalType(list.getEffects());
	}

	public Abnormal getSameByAbnormalType(Creature actor)
	{
		return getSameByAbnormalType(actor.getAbnormalList().getEffects());
	}

	public StatsSet getParam()
	{
		return _paramSet;
	}

	public int getChance()
	{
		return _chance;
	}

	public AbnormalType getAbnormalType()
	{
		return _abnormalType;
	}

	public int getAbnormalLvl()
	{
		return _abnormalLvl;
	}

	public AbnormalEffect[] getAbnormalEffects()
	{
		return _abnormalEffects;
	}

	public int getInterval()
	{
		return _interval;
	}

	public EffectUseType getUseType()
	{
		return _useType;
	}

	public boolean isInstant()
	{
		return _useType.isInstant();
	}

	public final double getValue()
	{
		return _value;
	}

	static
	{
		_log = LoggerFactory.getLogger(EffectTemplate.class);
		EMPTY_ARRAY = new EffectTemplate[0];
	}
}
