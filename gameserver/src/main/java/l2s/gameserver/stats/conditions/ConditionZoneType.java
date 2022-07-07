package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.items.ItemInstance;

public class ConditionZoneType extends Condition
{
	private final Zone.ZoneType _zoneType;

	public ConditionZoneType(String zoneType)
	{
		_zoneType = Zone.ZoneType.valueOf(zoneType);
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		return creature.isPlayer() && creature.isInZone(_zoneType);
	}
}
