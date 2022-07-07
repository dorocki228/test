package l2s.gameserver.skills.effects;

import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.World;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.skill.EffectTemplate;

import java.util.List;

public class EffectTargetableDisable extends Abnormal
{
	public EffectTargetableDisable(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public boolean checkCondition()
	{
		return getEffected().isTargetable(getEffector());
	}

	@Override
	public void onStart()
	{
		Creature effected = getEffected();
		effected.setTargetable(false);
		effected.abortAttack(true, true);
		effected.abortCast(true, true);
		List<Creature> characters = World.getAroundCharacters(effected);
		for(Creature character : characters)
		{
			if(character.getTarget() != effected && character.getAI().getAttackTarget() != effected && character.getAI().getCastTarget() != effected)
				continue;
			if(character.isNpc())
				((NpcInstance) character).getAggroList().remove(effected, true);
			if(character.getTarget() == effected)
				character.setTarget(null);
			if(character.getAI().getAttackTarget() == effected)
				character.abortAttack(true, true);
			if(character.getAI().getCastTarget() == effected)
				character.abortCast(true, true);
			character.sendActionFailed();
			character.stopMove();
			character.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
		super.onStart();
	}

	@Override
	public void onExit()
	{
		getEffected().setTargetable(true);
		super.onExit();
	}
}
