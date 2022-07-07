package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.pledge.Clan;

public class ConditionPlayerClanLeaderOnline extends Condition
{
	private final boolean _value;

	public ConditionPlayerClanLeaderOnline(boolean value)
	{
		_value = value;
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		if(!creature.isPlayer())
			return !_value;
		Clan clan = creature.getPlayer().getClan();
		if(clan == null)
			return !_value;
		return clan.getLeader().isOnline() == _value;
	}
}
