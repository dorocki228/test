package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;

public class ConditionPlayerClassId extends Condition
{
	private final int[] _classIds;

	public ConditionPlayerClassId(String[] ids)
	{
		_classIds = new int[ids.length];
		for(int i = 0; i < ids.length; ++i)
			_classIds[i] = Integer.parseInt(ids[i]);
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		if(!creature.isPlayer())
			return false;
		int playerClassId = creature.getPlayer().getActiveClassId();
		for(int id : _classIds)
			if(playerClassId == id)
				return true;
		return false;
	}
}
