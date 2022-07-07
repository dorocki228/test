package l2s.gameserver.stats.conditions;

import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;

public class ConditionPlayerMaxSP extends Condition
{
	private final int _spToAdd;

	public ConditionPlayerMaxSP(int spToAdd)
	{
		_spToAdd = spToAdd;
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		if(!creature.isPlayer())
			return false;
		long sp = creature.getPlayer().getSp() + _spToAdd;
		return sp <= Config.SP_LIMIT;
	}
}
