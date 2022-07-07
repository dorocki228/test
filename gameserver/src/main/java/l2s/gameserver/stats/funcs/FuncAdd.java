package l2s.gameserver.stats.funcs;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.stats.Stats;

public class FuncAdd extends Func
{
	public FuncAdd(Stats stat, int order, Object owner, double value)
	{
		super(stat, order, owner, value);
	}

	@Override
	public double calc(Creature creature, Creature target, Skill skill, double value)
	{
		return value + this.value;
	}
}
