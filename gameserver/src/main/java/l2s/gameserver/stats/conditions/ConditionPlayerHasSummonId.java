package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.SummonInstance;
import l2s.gameserver.model.items.ItemInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Bonux
 */
public class ConditionPlayerHasSummonId extends Condition
{
	private int _id;

	public ConditionPlayerHasSummonId(int id)
	{
		_id = id;
	}

	@Override
	protected boolean testImpl(@NotNull Creature actor, @Nullable Creature target, @Nullable Skill skill, @Nullable ItemInstance item, double value)
	{
		if(target == null || !target.isPlayer())
			return false;

		Player player = (Player) target;
		SummonInstance summon = player.getSummon();
		return summon != null && summon.getNpcId() == _id;
	}
}
