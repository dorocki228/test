package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Bonux
**/
public class ConditionPlayerCanUntransform extends Condition
{
	private final boolean _val;

	public ConditionPlayerCanUntransform(boolean val)
	{
		_val = val;
	}

	@Override
	protected boolean testImpl(@NotNull Creature actor, @Nullable Creature target, @Nullable Skill skill, @Nullable ItemInstance item, double value)
	{
		if(!actor.isPlayer())
			return !_val;

		Player player = actor.getPlayer();
		if(!player.isTransformed())
			return !_val;

		// Нельзя отменять летающую трансформу слишком высоко над землей
		if(player.isInFlyingTransform() && Math.abs(player.getZ() - player.getLoc().correctGeoZ(player.getGeoIndex()).z) > 333)
			return !_val;

		return _val;
	}
}