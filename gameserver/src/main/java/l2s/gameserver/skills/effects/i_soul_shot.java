package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class i_soul_shot extends i_abstract_effect
{
	private final double _power;

	public i_soul_shot(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
		_power = template.getParam().getDouble("power", 100.0);
	}

	@Override
	public void instantUse()
	{
		getEffected().sendPacket(SystemMsg.YOUR_SOULSHOTS_ARE_ENABLED);
		getEffected().setChargedSoulshotPower(_power);
	}
}
