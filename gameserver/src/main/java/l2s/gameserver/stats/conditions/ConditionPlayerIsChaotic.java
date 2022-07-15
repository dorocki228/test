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
public class ConditionPlayerIsChaotic extends Condition
{
	private final boolean _chaotic;

	public ConditionPlayerIsChaotic(boolean chaotic)
	{
		_chaotic = chaotic;
	}

	@Override
	protected boolean testImpl(@NotNull Creature actor, @Nullable Creature target, @Nullable Skill skill, @Nullable ItemInstance item, double value)
	{
		Player player = actor.getPlayer();
		if(player == null)
			return !_chaotic;
		if(player.isPK())
			return _chaotic;
		return !_chaotic;
	}
}