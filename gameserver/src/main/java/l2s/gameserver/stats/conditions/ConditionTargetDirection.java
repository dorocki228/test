package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.utils.PositionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConditionTargetDirection extends Condition
{
	private final PositionUtils.Position _dir;

	public ConditionTargetDirection(PositionUtils.Position direction)
	{
		_dir = direction;
	}

	@Override
	protected boolean testImpl(@NotNull Creature actor, @Nullable Creature target, @Nullable Skill skill, @Nullable ItemInstance item, double value)
	{
		return PositionUtils.getDirectionTo(target, actor) == _dir;
	}
}
