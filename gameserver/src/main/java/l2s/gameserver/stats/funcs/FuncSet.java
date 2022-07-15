package l2s.gameserver.stats.funcs;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.stats.DoubleStat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FuncSet extends Func
{
	public FuncSet(DoubleStat stat, int order, Object owner, double value)
	{
		super(stat, order, owner, value);
	}

	@Override
	public double calc(@NotNull Creature actor, @Nullable Creature target, @Nullable Skill skill, double value)
	{
		return getValue();
	}
}
