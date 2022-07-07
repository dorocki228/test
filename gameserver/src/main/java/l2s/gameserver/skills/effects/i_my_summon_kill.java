package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Skill;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class i_my_summon_kill extends i_abstract_effect
{
	public i_my_summon_kill(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public void instantUse()
	{
		for(Servitor servitor : getEffected().getServitors())
			if(servitor.isSummon())
				servitor.unSummon(false);
	}
}
