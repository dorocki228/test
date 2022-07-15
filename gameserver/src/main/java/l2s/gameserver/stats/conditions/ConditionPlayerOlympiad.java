package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConditionPlayerOlympiad extends Condition
{
	private final boolean _value;

	public ConditionPlayerOlympiad(boolean v)
	{
		_value = v;
	}

	@Override
	protected boolean testImpl(@NotNull Creature actor, @Nullable Creature target, @Nullable Skill skill, @Nullable ItemInstance item, double value)
	{
		final Player player = actor.getPlayer();
		if(player != null)
			return player.isInOlympiadMode() == _value;
		return !_value;
	}
}