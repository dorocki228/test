package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.pledge.Clan;

public class ConditionPlayerMinClanLevel extends Condition
{
	private final int _value;

	public ConditionPlayerMinClanLevel(int value)
	{
		_value = value;
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		if(!creature.isPlayer())
			return _value <= 0;
		Clan clan = creature.getPlayer().getClan();
		if(clan == null)
			return _value <= 0;
		return _value <= clan.getLevel();
	}
}
