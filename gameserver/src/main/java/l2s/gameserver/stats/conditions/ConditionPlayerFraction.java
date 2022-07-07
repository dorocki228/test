package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.Fraction;
import l2s.gameserver.model.items.ItemInstance;

import java.util.Arrays;

public class ConditionPlayerFraction extends Condition
{
	private final Fraction[] fractions;

	public ConditionPlayerFraction(String[] strings)
	{
		fractions = Arrays.stream(strings).map(String::toUpperCase).map(Fraction::valueOf).toArray(Fraction[]::new);
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		return Arrays.stream(fractions).anyMatch(temp -> creature.getFraction() == temp);
	}
}
