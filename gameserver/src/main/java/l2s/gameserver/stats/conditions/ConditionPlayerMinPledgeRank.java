package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.PledgeRank;
import l2s.gameserver.model.items.ItemInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConditionPlayerMinPledgeRank extends Condition
{
	private final PledgeRank _rank;

	public ConditionPlayerMinPledgeRank(PledgeRank rank)
	{
		_rank = rank;
	}

	@Override
	protected boolean testImpl(@NotNull Creature actor, @Nullable Creature target, @Nullable Skill skill, @Nullable ItemInstance item, double value)
	{
		if(!actor.isPlayer())
			return false;
		return actor.getPlayer().getPledgeRank().ordinal() >= _rank.ordinal();
	}
}