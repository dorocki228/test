package l2s.gameserver.skills.effects;

import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class i_delete_hate_of_me extends i_abstract_effect
{
	public i_delete_hate_of_me(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public boolean checkCondition()
	{
		return getEffected().isMonster() && super.checkCondition();
	}

	@Override
	public void instantUse()
	{
		MonsterInstance monster = (MonsterInstance) getEffected();
		monster.getAggroList().remove(getEffector(), true);
		monster.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
	}
}
