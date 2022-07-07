package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.player.Mount;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.model.items.ItemInstance;

public class ConditionTargetPetFeed extends Condition
{
	private final int _itemId;

	public ConditionTargetPetFeed(int itemId)
	{
		_itemId = itemId;
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		if(target == null)
			return false;
		if(target.isPet())
		{
			PetInstance pet = (PetInstance) target;
			return pet.isMyFeed(_itemId);
		}
		if(target.isPlayer() && target.getPlayer().isMounted())
		{
			Mount mount = target.getPlayer().getMount();
			return mount.isMyFeed(_itemId);
		}
		return false;
	}
}
