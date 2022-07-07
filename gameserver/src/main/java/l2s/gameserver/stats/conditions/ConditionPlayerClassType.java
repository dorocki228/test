package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.SubClassType;
import l2s.gameserver.model.items.ItemInstance;

public class ConditionPlayerClassType extends Condition
{
	private final SubClassType _type;

	public ConditionPlayerClassType(SubClassType type)
	{
		_type = type;
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		if(!creature.isPlayer())
			return false;
		Player player = creature.getPlayer();
		return player.getActiveSubClass().getType() == _type;
	}
}
