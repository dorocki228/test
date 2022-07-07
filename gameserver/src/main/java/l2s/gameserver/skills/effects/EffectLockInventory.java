package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.items.LockType;
import l2s.gameserver.templates.skill.EffectTemplate;

public class EffectLockInventory extends Abnormal
{
	private final LockType _lockType;
	private final int[] _lockItems;

	public EffectLockInventory(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
		_lockType = (LockType) template.getParam().getEnum("lockType", (Class) LockType.class);
		_lockItems = template.getParam().getIntegerArray("lockItems");
	}

	@Override
	public void onStart()
	{
		super.onStart();
		Player player = _effector.getPlayer();
		player.getInventory().lockItems(_lockType, _lockItems);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		Player player = _effector.getPlayer();
		player.getInventory().unlock();
	}
}
