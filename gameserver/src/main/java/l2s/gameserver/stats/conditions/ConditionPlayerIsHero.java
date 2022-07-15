package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Bonux
 */
public class ConditionPlayerIsHero extends Condition
{
	private final boolean _value;

	public ConditionPlayerIsHero(boolean value)
	{
		_value = value;
	}

	@Override
	protected boolean testImpl(@NotNull Creature actor, @Nullable Creature target, @Nullable Skill skill, @Nullable ItemInstance item, double value)
	{
		if(!actor.isPlayer())
			return !_value;

		return actor.getPlayer().isHero() == _value;
	}
}
