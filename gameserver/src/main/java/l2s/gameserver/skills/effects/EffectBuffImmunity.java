/*
 * Decompiled with CFR 0_122.
 */
package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class EffectBuffImmunity extends Abnormal
{
	public EffectBuffImmunity(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
        getEffected().startBuffImmunity();
	}

	@Override
	public void onExit()
	{
		super.onExit();
        getEffected().stopBuffImmunity();
	}

	@Override
	public boolean onActionTime()
	{
		if(_effected.isDead())
			return getSkill().isToggle();

		if(!getSkill().isToggle())
			return false;
		return true;
	}
}
