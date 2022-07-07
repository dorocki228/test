package l2s.gameserver.skills.effects;

import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class EffectMoveToEffector extends Abnormal
{
	public EffectMoveToEffector(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public boolean checkCondition()
	{
		if(getEffected().isFearImmune())
			return false;
		Player player = getEffected().getPlayer();
		if(player != null)
		{
			SiegeEvent<?, ?> siegeEvent = player.getEvent(SiegeEvent.class);
			if(getEffected().isSummon() && siegeEvent != null && siegeEvent.containsSiegeSummon((Servitor) getEffected()))
				return false;
		}
		return !getEffected().isInPeaceZone() && super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(!getEffected().startFear())
		{
			getEffected().abortAttack(true, true);
			getEffected().abortCast(true, true);
			getEffected().stopMove();
		}
		onActionTime();
	}

	@Override
	public void onExit()
	{
		super.onExit();
		getEffected().stopFear();
		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
	}

	@Override
	public boolean onActionTime()
	{
		getEffected().moveToLocation(GeoEngine.moveCheck(getEffected().getX(), getEffected().getY(), getEffected().getZ(), getEffector().getX(), getEffector().getY(), getEffected().getGeoIndex()), 40, true);
		return true;
	}
}
