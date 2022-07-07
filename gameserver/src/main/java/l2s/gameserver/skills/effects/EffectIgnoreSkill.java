package l2s.gameserver.skills.effects;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

import java.util.Arrays;

public class EffectIgnoreSkill extends Abnormal
{
	private final TIntSet _ignoredSkill;

	public EffectIgnoreSkill(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
		_ignoredSkill = new TIntHashSet();
		String[] split = template.getParam().getString("skillId", "").split(";");
		Arrays.stream(split).mapToInt(Integer::parseInt).forEach(_ignoredSkill::add);
	}

	@Override
	public boolean isIgnoredSkill(Skill skill)
	{
		return !_ignoredSkill.isEmpty() && _ignoredSkill.contains(skill.getId());
	}
}
