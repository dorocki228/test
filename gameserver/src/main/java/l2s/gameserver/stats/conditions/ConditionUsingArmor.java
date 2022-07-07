package l2s.gameserver.stats.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.templates.item.ArmorTemplate;

public class ConditionUsingArmor extends Condition
{
	private final ArmorTemplate.ArmorType _armor;

	public ConditionUsingArmor(ArmorTemplate.ArmorType armor)
	{
		_armor = armor;
	}

	@Override
	protected boolean testImpl(Creature creature, Creature target, Skill skill, ItemInstance item, double value)
	{
		return creature.isPlayer() && ((Player) creature).getWearingArmorType() == _armor;
	}
}
