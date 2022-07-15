package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.pledge.Clan;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Bonux
 */
public class ConditionPlayerMinClanLevel extends Condition
{
	private final int _value;

	public ConditionPlayerMinClanLevel(int value)
	{
		_value = value;
	}

	@Override
	protected boolean testImpl(@NotNull Creature actor, @Nullable Creature target, @Nullable Skill skill, @Nullable ItemInstance item, double value)
	{
		if(!actor.isPlayer())
			return _value <= 0;

		Clan clan = actor.getPlayer().getClan();
		if(clan == null)
			return _value <= 0;

		return _value <= clan.getLevel();
	}
}
