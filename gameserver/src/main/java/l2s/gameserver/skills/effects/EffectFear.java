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
import l2s.gameserver.utils.PositionUtils;

public final class EffectFear extends Abnormal
{
	public static final double FEAR_RANGE = 900.0;

	public EffectFear(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
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
		double angle = Math.toRadians(PositionUtils.calculateAngleFrom(getEffector(), getEffected()));
		int oldX = getEffected().getX();
		int oldY = getEffected().getY();
		int x = oldX + (int) (900.0 * Math.cos(angle));
		int y = oldY + (int) (900.0 * Math.sin(angle));
		getEffected().setRunning();
		getEffected().moveToLocation(GeoEngine.moveCheck(oldX, oldY, getEffected().getZ(), x, y, getEffected().getGeoIndex()), 0, false);
		return true;
	}

	@Override
	public int getInterval()
	{
		return 3;
	}
}
