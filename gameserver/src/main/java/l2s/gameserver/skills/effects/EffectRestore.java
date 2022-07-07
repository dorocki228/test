package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

public abstract class EffectRestore extends Abnormal
{
	protected final boolean _ignoreBonuses;
	protected final boolean _percent;
	protected final boolean _staticPower;

	public EffectRestore(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
		_ignoreBonuses = template.getParam().getBool("ignore_bonuses", false);
		_percent = template.getParam().getBool("percent", false);
		_staticPower = template.getParam().getBool("static_power", skill.isHandler() || _percent);// || !template.isInstant());
	}
}
