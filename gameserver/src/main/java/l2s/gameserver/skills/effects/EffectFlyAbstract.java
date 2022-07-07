package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.s2c.FlyToLocationPacket;
import l2s.gameserver.templates.skill.EffectTemplate;

public abstract class EffectFlyAbstract extends Abnormal
{
	private final FlyToLocationPacket.FlyType _flyType;
	private final double _flyCourse;
	private final int _flySpeed;
	private final int _flyDelay;
	private final int _flyAnimationSpeed;
	private final int _flyRadius;

	public EffectFlyAbstract(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
		_flyType = (FlyToLocationPacket.FlyType) template.getParam().getEnum("fly_type", (Class) FlyToLocationPacket.FlyType.class, (Enum) getSkill().getFlyType());
		_flyCourse = template.getParam().getDouble("fly_course", 0.0);
		_flySpeed = template.getParam().getInteger("fly_speed", getSkill().getFlySpeed());
		_flyDelay = template.getParam().getInteger("fly_delay", getSkill().getFlyDelay());
		_flyAnimationSpeed = template.getParam().getInteger("fly_animation_speed", getSkill().getFlyAnimationSpeed());
		_flyRadius = template.getParam().getInteger("fly_radius", getSkill().getFlyRadius());
	}

	public FlyToLocationPacket.FlyType getFlyType()
	{
		return _flyType;
	}

	public double getFlyCourse()
	{
		return _flyCourse;
	}

	public int getFlySpeed()
	{
		return _flySpeed;
	}

	public int getFlyDelay()
	{
		return _flyDelay;
	}

	public int getFlyAnimationSpeed()
	{
		return _flyAnimationSpeed;
	}

	public int getFlyRadius()
	{
		return _flyRadius;
	}
}
