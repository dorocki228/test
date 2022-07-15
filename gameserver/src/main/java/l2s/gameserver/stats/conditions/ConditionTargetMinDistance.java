package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Bonux
**/
public class ConditionTargetMinDistance extends Condition
{
	private final int _distance;

	public ConditionTargetMinDistance(int distance)
	{
		_distance = distance;
	}

	@Override
	protected boolean testImpl(@NotNull Creature actor, @Nullable Creature target, @Nullable Skill skill, @Nullable ItemInstance item, double value)
	{
		if(target == null)
			return false;

		return !actor.isInRange(target.getLoc(), _distance);
	}
}
