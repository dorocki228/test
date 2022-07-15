package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.model.items.ItemInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConditionZoneType extends Condition
{
	private final ZoneType _zoneType;

	public ConditionZoneType(String zoneType)
	{
		_zoneType = ZoneType.valueOf(zoneType);
	}

	@Override
	protected boolean testImpl(@NotNull Creature actor, @Nullable Creature target, @Nullable Skill skill, @Nullable ItemInstance item, double value)
	{
		if(!actor.isPlayer())
			return false;
		return actor.isInZone(_zoneType);
	}
}