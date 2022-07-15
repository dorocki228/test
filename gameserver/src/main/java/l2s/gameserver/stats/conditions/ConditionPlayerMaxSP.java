package l2s.gameserver.stats.conditions;

import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConditionPlayerMaxSP extends Condition
{
	private final int _spToAdd;

	public ConditionPlayerMaxSP(int spToAdd)
	{
		_spToAdd = spToAdd;
	}

	@Override
	protected boolean testImpl(@NotNull Creature actor, @Nullable Creature target, @Nullable Skill skill, @Nullable ItemInstance item, double value)
	{
		if(!actor.isPlayer())
			return false;

		long sp = actor.getPlayer().getSp() + _spToAdd;
		return sp <= Config.SP_LIMIT;
	}
}