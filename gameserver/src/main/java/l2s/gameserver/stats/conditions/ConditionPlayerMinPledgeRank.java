package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.PledgeRank;
import l2s.gameserver.model.items.ItemInstance;

public class ConditionPlayerMinPledgeRank extends Condition
{
	private final PledgeRank _rank;

	public ConditionPlayerMinPledgeRank(PledgeRank rank)
	{
		_rank = rank;
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		return creature.isPlayer() && creature.getPlayer().getPledgeRank().ordinal() >= _rank.ordinal();
	}
}
