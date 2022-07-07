package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.templates.skill.EffectTemplate;

public class EffectArmorBreaker extends Abnormal
{
	private ItemInstance item;

	public EffectArmorBreaker(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public boolean checkCondition()
	{
		return _effected.isPlayer() && _effected.getPlayer().getInventory().getPaperdollItem(10) != null && super.checkCondition();
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		item = _effected.getPlayer().getInventory().getPaperdollItem(10);
		_effected.getPlayer().getInventory().unEquipItem(item);
	}

	@Override
	protected void onExit()
	{
		super.onExit();
		_effected.getPlayer().getInventory().equipItem(item);
	}
}
