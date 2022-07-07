package l2s.gameserver.stats.conditions;

import gnu.trove.set.hash.TIntHashSet;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;

public class ConditionTargetForbiddenClassId extends Condition
{
	private final TIntHashSet _classIds;

	public ConditionTargetForbiddenClassId(String[] ids)
	{
		_classIds = new TIntHashSet();
		for(String id : ids)
			_classIds.add(Integer.parseInt(id));
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		return !target.isPlayable() || !target.isPlayer() || !_classIds.contains(target.getPlayer().getActiveClassId());
	}
}
