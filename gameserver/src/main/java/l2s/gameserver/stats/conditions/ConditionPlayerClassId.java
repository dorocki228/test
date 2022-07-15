package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConditionPlayerClassId extends Condition
{
	private final int[] _classIds;

	public ConditionPlayerClassId(String[] ids)
	{
		_classIds = new int[ids.length];
		for(int i = 0; i < ids.length; i++)
			_classIds[i] = Integer.parseInt(ids[i]);
	}

	@Override
	protected boolean testImpl(@NotNull Creature actor, @Nullable Creature target, @Nullable Skill skill, @Nullable ItemInstance item, double value)
	{
		if(!actor.isPlayer())
			return false;

		int playerClassId = actor.getPlayer().getActiveClassId();
		for(int id : _classIds)
			if(playerClassId == id)
				return true;

		return false;
	}
}