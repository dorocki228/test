package l2s.gameserver.stats.funcs;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.stats.DoubleStat;
import l2s.gameserver.stats.StatModifierType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FuncMul extends Func
{
	public FuncMul(DoubleStat stat, int order, Object owner, double value)
	{
		super(stat, order, owner, value);
	}

	@Override
	public double calc(@NotNull Creature actor, @Nullable Creature target, @Nullable Skill skill, double value)
	{
		return value + getValue() * 100.0 - 100.0;
	}

	@Override
	public StatModifierType getModifierType()
	{
		return StatModifierType.PER;
	}
}
