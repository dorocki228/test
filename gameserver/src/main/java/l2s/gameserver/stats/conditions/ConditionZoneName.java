package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;

public class ConditionZoneName extends Condition
{
	private final String _zoneName;

	public ConditionZoneName(String zoneName)
	{
		_zoneName = zoneName;
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		return creature.isPlayer() && creature.isInZone(_zoneName);
	}
}
