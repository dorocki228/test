package l2s.gameserver.skills.skillclasses;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.FlyToLocationPacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.PositionUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author IOException
 */
public class InstantJump extends Skill
{
	/**
	 * @param set парамерты скилла
	 */
	public InstantJump(StatsSet set) {
		super(set);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first) {
		if(activeChar != null && activeChar.isPlayer() && activeChar.getPlayer().isMounted())
			return false;
		return super.checkCondition(activeChar, target, forceUse, dontMove, first);
	}

	@Override
	public void useSkill(Creature activeChar, Creature target, boolean reflected)
	{
		if (target == null)
			return;

		if (Rnd.chance(target.calcStat(Stats.P_SKILL_EVASION, 0, activeChar, this))) {
			if (activeChar.isPlayer())
				activeChar.sendPacket(new SystemMessagePacket(SystemMsg.C1_DODGED_THE_ATTACK).addName(target));
			if (target.isPlayer())
				target.sendPacket(new SystemMessagePacket(SystemMsg.C1_HAS_EVADED_C2S_ATTACK).addName(target).addName(activeChar));
			return;
		}
		int x, y, z;

		int px = target.getX();
		int py = target.getY();
		double ph = PositionUtils.convertHeadingToDegree(target.getHeading());

		ph += 180;

		if (ph > 360)
			ph -= 360;

		ph = Math.PI * ph / 180;

		x = (int) (px + 25 * StrictMath.cos(ph));
		y = (int) (py + 25 * StrictMath.sin(ph));
		z = target.getZ();

		Location loc = new Location(x, y, z);

		if (Config.ALLOW_GEODATA)
			loc = GeoEngine.moveCheck(activeChar.getX(), activeChar.getY(), activeChar.getZ(), x, y, activeChar.getReflection().getGeoIndex());

		// По тикету #300 Shadowstep
		// Если сзади персонажа, на которого используют шедоу степ, недостаточно места, телепортирует перед(к лицу) персонажа.
		if (loc.distance(target.getLoc()) < 10)
		{
			x = (int) (px - 25 * StrictMath.cos(ph));
			y = (int) (py - 25 * StrictMath.sin(ph));
			z = target.getZ();

			loc = new Location(x, y, z);
			if (Config.ALLOW_GEODATA)
				loc = GeoEngine.moveCheck(activeChar.getX(), activeChar.getY(), activeChar.getZ(), x, y, activeChar.getReflection().getGeoIndex());
		}

		if (target.isMonster())
		{
			MonsterInstance monster = (MonsterInstance) target;
			monster.abortAttack(true, true);
			monster.abortCast(true, true);
			monster.getAI().notifyEvent(CtrlEvent.EVT_THINK);
		}
		else if(target.isPlayable() && target.getTarget() != null)
		{
			target.setTarget(null);
			target.abortAttack(true, true);
			target.abortCast(true, true);
		}

		target.abortAttack(true, true);
		target.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, activeChar);

		activeChar.broadcastPacket(new FlyToLocationPacket(activeChar, loc, FlyToLocationPacket.FlyType.DUMMY,
                getFlySpeed(), getFlyDelay(), getFlyAnimationSpeed()));

		if (activeChar.isPlayer()) {
			Player player = activeChar.getPlayer();
			player.setIgnoreValidatePosition(true);
			activeChar.setXYZ(loc.x, loc.y, loc.z);
			activeChar.validateLocation(1);
			activeChar.setTarget(target);
			activeChar.setHeading(PositionUtils.calculateHeadingFrom(activeChar ,target));
			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			if(!player.isPhantom())
			{
				ThreadPoolManager.getInstance().schedule(() -> 
				{
					player.setIgnoreValidatePosition(false);
				}, Math.max(player.getNetConnection().getPing(), 1000), TimeUnit.MILLISECONDS);
			}else
				player.setIgnoreValidatePosition(false);
		} else {
			activeChar.setXYZ(loc.x, loc.y, loc.z);
			activeChar.validateLocation(1);
			activeChar.setTarget(target);
			activeChar.setHeading(PositionUtils.calculateHeadingFrom(activeChar ,target));
			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
	}
}
