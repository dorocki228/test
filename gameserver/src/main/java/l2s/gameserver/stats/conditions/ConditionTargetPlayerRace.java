package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.items.ItemInstance;

public class ConditionTargetPlayerRace extends Condition
{
	private final Race _race;

	public ConditionTargetPlayerRace(String race)
	{
		_race = Race.valueOf(race.toUpperCase());
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		return target != null && target.isPlayer() && _race == ((Player) target).getRace();
	}
}