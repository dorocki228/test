package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConditionPlayerFlagged extends Condition
{
	private final boolean _flagged;

	public ConditionPlayerFlagged(boolean flagged)
	{
		_flagged = flagged;
	}

	@Override
	protected boolean testImpl(@NotNull Creature actor, @Nullable Creature target, @Nullable Skill skill, @Nullable ItemInstance item, double value)
	{
		if(_flagged)
			return actor.getPvpFlag() > 0;
		return actor.getPvpFlag() <= 0;
	}
}