package ai;

import java.util.Collection;

import l2s.commons.util.Rnd;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.*;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.utils.NpcUtils;

import bosses.AntharasManager;

/**
 * @author Bonux
 */
public final class AntharasAI extends AbstractAntharasAI
{
	private static final int MOVIE_7_TIMER_ID = 5007;
	private static final int MOVIE_8_TIMER_ID = 5008;

	public AntharasAI(NpcInstance actor)
	{
		super(actor);
	}

	public void startPresentation(long delay)
	{
		if(AntharasManager.getAntharasNpc() == getActor())
		{
			NpcInstance actor = getActor();
			actor.setAggroRange(0);
			actor.getFlags().getInvulnerable().start(this);

			addTimer(PREPARE_MOVIE_TIMER_ID, delay);
		}
	}

	@Override
	protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
	{
		if(AntharasManager.getAntharasNpc() == getActor())
			AntharasManager.setLastAttackTime();

		super.onEvtAttacked(attacker, skill, damage);
	}

	@Override
	protected void onEvtDead(Creature killer, LostItems lostItems)
	{
		for(NpcInstance minion : _minions)
			minion.deleteMe();

		super.onEvtDead(killer, lostItems);
		addTimer(MOVIE_7_TIMER_ID, 10);

		if(AntharasManager.getAntharasNpc() == getActor())
			AntharasManager.onAntharasDie();
	}

	@Override
	protected void onEvtTimer(int timerId, Object arg1, Object arg2)
	{
		NpcInstance actor = getActor();
		if(timerId == MOVIE_1_TIMER_ID)
		{
			if(AntharasManager.getAntharasNpc() == getActor())
				AntharasManager.closeEntry();
		}
		else if(timerId == MOVIE_6_TIMER_ID)
		{
			if(AntharasManager.getAntharasNpc() == getActor())
				AntharasManager.startSleepCheckTask();
		}
		else if(timerId == MOVIE_7_TIMER_ID)
		{
			for(Player player : getPlayersInsideLair())
			{
				if(player.getDistance(actor) <= MOVIE_MAX_DISTANCE)
				{
					player.enterMovieMode();
					player.specialCamera(actor, 1200, 20, -10, 0, 13000, 0, 0, 0, 0);
				}
				else
					player.leaveMovieMode();
			}
			addTimer(MOVIE_8_TIMER_ID, 13000);
		}
		else if(timerId == MOVIE_8_TIMER_ID)
		{
			for(Player player : getPlayersInsideLair())
			{
				player.leaveMovieMode();
				player.altOnMagicUse(player, SkillEntry.makeSkillEntry(SkillEntryType.NONE,23312, 1));
				player.sendPacket(new ExShowScreenMessage(NpcString.ANTHARAS_THE_EVIL_LAND_DRAGON_ANTHARAS_DEFEATED, 10000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false));
			}
		}
		super.onEvtTimer(timerId, arg1, arg2);
	}

	@Override
	protected Collection<Player> getPlayersInsideLair()
	{
		NpcInstance actor = getActor();
		if(AntharasManager.getAntharasNpc() == actor)
			return AntharasManager.getPlayersInside();

		return World.getAroundPlayers(actor);
	}

	@Override
	protected NpcInstance spawnMinion()
	{
		NpcInstance actor = getActor();
		return NpcUtils.spawnSingle(Rnd.chance(50) ? 29104 : 29069, Location.findPointToStay(actor.getLoc(), 400, 700, actor.getGeoIndex()));
	}
}