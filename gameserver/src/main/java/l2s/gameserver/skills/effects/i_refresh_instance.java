package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class i_refresh_instance extends i_abstract_effect
{
	public i_refresh_instance(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public boolean checkCondition()
	{
		return getEffected().isPlayer() && super.checkCondition();
	}

	@Override
	public void instantUse()
	{
		Player player = getEffected().getPlayer();
		if(player != null)
		{
			int instanceId = (int) calc();
			if(instanceId == -1) {
				player.removeAllInstanceReuses();
				player.loadInstanceReuses();
			} else {
				player.removeInstanceReuse(instanceId);
				player.loadInstanceReuses();
			}
		}
	}
}
