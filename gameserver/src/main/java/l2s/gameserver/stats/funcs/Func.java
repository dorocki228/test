package l2s.gameserver.stats.funcs;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.stats.conditions.Condition;

public abstract class Func implements Comparable<Func>
{
	public static final Func[] EMPTY_FUNC_ARRAY;
	public final Stats stat;
	public final int order;
	public final Object owner;
	public final double value;
	protected Condition cond;

	public Func(Stats stat, int order, Object owner)
	{
		this(stat, order, owner, 0.0);
	}

	public Func(Stats stat, int order, Object owner, double value)
	{
		this.stat = stat;
		this.order = order;
		this.owner = owner;
		this.value = value;
	}

	public void setCondition(Condition cond)
	{
		this.cond = cond;
	}

	public Condition getCondition()
	{
		return cond;
	}

	public abstract double calc(Creature creature, Creature target, Skill skill, double value);

	@Override
	public int compareTo(Func f) throws NullPointerException
	{
		return order - f.order;
	}

	static
	{
		EMPTY_FUNC_ARRAY = new Func[0];
	}
}
