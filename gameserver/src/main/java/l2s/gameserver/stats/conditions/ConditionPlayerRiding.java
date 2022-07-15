package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConditionPlayerRiding extends Condition
{
	public enum CheckPlayerRiding
	{
		NONE,
		STRIDER,
		WYVERN
	}

	private final CheckPlayerRiding _riding;

	public ConditionPlayerRiding(CheckPlayerRiding riding)
	{
		_riding = riding;
	}

	@Override
	protected boolean testImpl(@NotNull Creature actor, @Nullable Creature target, @Nullable Skill skill, @Nullable ItemInstance item, double value)
	{
		if(!actor.isPlayer())
			return false;

		Player player = (Player) actor;
		switch(_riding)
		{
			case STRIDER:
				if(player.isMounted() && !player.isFlying())
					return true;
				break;
			case WYVERN:
				if(player.isMounted() && player.isFlying())
					return true;
				break;
			case NONE:
				if(!player.isMounted())
					return true;
				break;
		}
		return false;
	}
}
