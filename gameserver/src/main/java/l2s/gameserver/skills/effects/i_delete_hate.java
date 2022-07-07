package l2s.gameserver.skills.effects;

import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class i_delete_hate extends i_abstract_effect
{
	public i_delete_hate(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
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
		monster.getAggroList().clear(true);
		if(monster.getAI() instanceof DefaultAI)
			((DefaultAI) monster.getAI()).setGlobalAggro(System.currentTimeMillis() + monster.getParameter("globalAggro", 10000L));
		monster.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
	}
}
