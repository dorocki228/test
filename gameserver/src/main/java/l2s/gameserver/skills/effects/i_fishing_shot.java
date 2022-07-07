package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class i_fishing_shot extends i_abstract_effect
{
	private final double _power;

	public i_fishing_shot(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
		_power = template.getParam().getDouble("power", 100.0);
	}

	@Override
	public void instantUse()
	{
		getEffected().getPlayer().setChargedFishshotPower(_power);
	}
}
