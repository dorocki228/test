package l2s.gameserver.skills.effects;

import l2s.gameserver.data.xml.holder.TransformTemplateHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.base.TransformType;
import l2s.gameserver.templates.player.transform.TransformTemplate;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class EffectTransformation extends Abnormal
{
	public EffectTransformation(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public boolean checkCondition()
	{
		if(getEffected() != getEffector())
			return false;
		if(!getEffected().isPlayer())
			return false;
		int transformId = (int) calc();
		if(transformId > 0)
		{
			TransformTemplate template = TransformTemplateHolder.getInstance().getTemplate(getEffected().getSex(), transformId);
			if(template == null)
				return false;
			if(template.getType() == TransformType.FLYING && getEffected().getX() > -166168)
				return false;
		}
		return super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		getEffected().setTransform((int) calc());
	}

	@Override
	public void onExit()
	{
		super.onExit();
		if(calc() > 0.0)
			getEffected().setTransform(null);
	}
}
