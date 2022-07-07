package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.stats.StatModifierType;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.stats.funcs.Func;
import l2s.gameserver.stats.funcs.FuncTemplate;
import l2s.gameserver.templates.skill.EffectTemplate;

public abstract class p_abstract_stat_effect extends Abnormal
{
	private final StatModifierType _modifierType;
	private final Func _func;

	public p_abstract_stat_effect(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template, Stats stat)
	{
		super(creature, target, skill, reflected, template);
		_modifierType = (StatModifierType) template.getParam().getEnum("type", (Class) StatModifierType.class, (Enum) StatModifierType.DIFF);
		if(_modifierType == StatModifierType.DIFF)
			_func = new FuncTemplate(getTemplate().getCondition(), "Add", stat, 64, calc()).getFunc(this);
		else
			_func = new FuncTemplate(getTemplate().getCondition(), "Mul", stat, 48, calc() / 100.0 + 1.0).getFunc(this);
	}

	protected final StatModifierType getModifierType()
	{
		return _modifierType;
	}

	@Override
	protected final void onStart()
	{
		getEffected().addStatFunc(_func);
		afterApplyActions();
	}

	@Override
	protected final void onExit()
	{
		getEffected().removeStatFunc(_func);
	}

	protected void afterApplyActions()
	{}

	@Override
	public final boolean checkCondition()
	{
		return true;
	}
}
