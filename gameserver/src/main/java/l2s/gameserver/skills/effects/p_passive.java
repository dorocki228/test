package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.skill.EffectTemplate;

public class p_passive extends Abnormal
{
	public p_passive(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(getEffected().isNpc())
		{
			NpcInstance npc = (NpcInstance) getEffected();
			npc.setUnAggred(true);
		}
	}

	@Override
	public void onExit()
	{
		super.onExit();
		if(getEffected().isNpc())
			((NpcInstance) getEffected()).setUnAggred(false);
	}
}
