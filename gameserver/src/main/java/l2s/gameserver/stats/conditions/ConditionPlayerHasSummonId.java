package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.SummonInstance;
import l2s.gameserver.model.items.ItemInstance;

public class ConditionPlayerHasSummonId extends Condition
{
	private final int _id;

	public ConditionPlayerHasSummonId(int id)
	{
		_id = id;
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		if(target == null || !target.isPlayer())
			return false;

		Player player = target.getPlayer();
		for(SummonInstance summon : player.getSummons())
			if(summon.getNpcId() == _id)
				return true;
		return false;
	}
}
