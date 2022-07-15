package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.player.Mount;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.model.items.ItemInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConditionTargetPetFeed extends Condition
{
	private final int _itemId;

	public ConditionTargetPetFeed(int itemId)
	{
		_itemId = itemId;
	}

	@Override
	protected boolean testImpl(@NotNull Creature actor, @Nullable Creature target, @Nullable Skill skill, @Nullable ItemInstance item, double value)
	{
		if(actor.isPet())
		{
			PetInstance pet = (PetInstance) actor;
			return pet.isMyFeed(_itemId);
		}

		if(actor.isPlayer() && actor.getPlayer().isMounted())
		{
			Mount mount = actor.getPlayer().getMount();
			return mount.isMyFeed(_itemId);
		}
		return false;
	}
}
