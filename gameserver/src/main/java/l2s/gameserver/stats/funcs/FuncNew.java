package l2s.gameserver.stats.funcs;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.stats.DoubleStat;
import l2s.gameserver.stats.StatModifierType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Bonux
 * Хрень временная, чтобы плавно внедрять и переписывать новую систему калькуляции статтов.
**/
public class FuncNew extends Func
{
	private final StatModifierType _modifierType;

	public FuncNew(DoubleStat stat, int order, Object owner, double value, StatModifierType modifierType)
	{
		super(stat, order, owner, value);
		_modifierType = modifierType;
	}

	@Override
	public double calc(@NotNull Creature actor, @Nullable Creature target, @Nullable Skill skill, double value)
	{
		switch(getModifierType())
		{
			case DIFF:
				return value + getValue();
			case PER:
				//env.value = env.value * (1 + value / 100);
				return value + getValue();
		}

		return value;
	}

	@Override
	public StatModifierType getModifierType()
	{
		return _modifierType;
	}
}
