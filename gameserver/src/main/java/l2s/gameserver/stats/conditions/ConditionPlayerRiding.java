package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;

public class ConditionPlayerRiding extends Condition
{
	private final CheckPlayerRiding _riding;

	public ConditionPlayerRiding(CheckPlayerRiding riding)
	{
		_riding = riding;
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		if(!creature.isPlayer())
			return false;
		Player player = creature.getPlayer();
		switch(_riding)
		{
			case STRIDER:
			{
				if(player.isMounted() && !player.isFlying())
					return true;
				break;
			}
			case WYVERN:
			{
				if(player.isMounted() && player.isFlying())
					return true;
				break;
			}
			case NONE:
			{
				if(!player.isMounted())
					return true;
				break;
			}
		}
		return false;
	}

	public enum CheckPlayerRiding
	{
		NONE,
		STRIDER,
		WYVERN
    }
}
