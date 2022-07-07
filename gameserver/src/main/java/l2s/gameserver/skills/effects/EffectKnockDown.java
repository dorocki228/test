package l2s.gameserver.skills.effects;

import l2s.gameserver.Config;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.FlyToLocationPacket;
import l2s.gameserver.templates.skill.EffectTemplate;
import l2s.gameserver.utils.Location;

public class EffectKnockDown extends EffectFlyAbstract
{
	private int _x;
	private int _y;
	private int _z;

	@Override
	public boolean checkCondition()
	{
		if(getEffected().isThrowAndKnockImmune())
		{
			getEffected().sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
			return false;
		}
		return super.checkCondition();
	}

	public EffectKnockDown(Creature creature, Creature target, Skill skill, boolean reflected, EffectTemplate template)
	{
		super(creature, target, skill, reflected, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		int curX = getEffected().getX();
		int curY = getEffected().getY();
		int curZ = getEffected().getZ();
		double dx = getEffector().getX() - curX;
		double dy = getEffector().getY() - curY;
		double dz = getEffector().getZ() - curZ;
		double distance = Math.sqrt(dx * dx + dy * dy);
		if(distance > 2000.0)
			return;
		int offset = Math.min((int) distance + getFlyRadius(), 1400);
		offset += (int) Math.abs(dz);
		if(offset < 5)
			offset = 5;
		if(distance < 1.0)
			return;
        double cos = dx / distance;
		_x = getEffector().getX() - (int) (offset * cos);
        double sin = dy / distance;
        _y = getEffector().getY() - (int) (offset * sin);
		_z = getEffected().getZ();
		if(Config.ALLOW_GEODATA)
		{
			Location destiny = GeoEngine.moveCheck(getEffected().getX(), getEffected().getY(), getEffected().getZ(), _x, _y, getEffected().getGeoIndex());
			_x = destiny.getX();
			_y = destiny.getY();
		}
		getEffected().startKnockDown();
		getEffected().abortAttack(true, true);
		getEffected().abortCast(true, true);
		getEffected().stopMove();
		getEffected().getAI().notifyEvent(CtrlEvent.EVT_KNOCK_DOWN, getEffected());
		getEffected().broadcastPacket(new FlyToLocationPacket(getEffected(), new Location(_x, _y, _z), FlyToLocationPacket.FlyType.PUSH_DOWN_HORIZONTAL, getFlySpeed(), getFlyDelay(), getFlyAnimationSpeed()));
		getEffected().setXYZ(_x, _y, _z);
		getEffected().validateLocation(1);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		if(getEffected().isKnockDowned())
		{
			getEffected().stopKnockDown();
			if(!getEffected().isPlayer())
				getEffected().getAI().notifyEvent(CtrlEvent.EVT_THINK);
		}
	}
}
