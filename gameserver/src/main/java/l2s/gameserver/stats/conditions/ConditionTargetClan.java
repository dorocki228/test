package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConditionTargetClan extends Condition
{
	private final boolean _test;

	public ConditionTargetClan(String param)
	{
		_test = Boolean.valueOf(param);
	}

	@Override
	protected boolean testImpl(@NotNull Creature actor, @Nullable Creature target, @Nullable Skill skill, @Nullable ItemInstance item, double value)
	{
		return actor.getPlayer() != null && target.getPlayer() != null
				&& (actor.getPlayer().getClanId() != 0
				&& actor.getPlayer().getClanId() == target.getPlayer().getClanId() == _test
				|| actor.getPlayer().getParty() != null
				&& actor.getPlayer().getParty() == target.getPlayer().getParty());
	}
}