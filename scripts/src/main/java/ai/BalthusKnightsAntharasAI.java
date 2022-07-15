package ai;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.listener.actor.OnChangeCurrentHpListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.World;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.templates.npc.WalkerRoute;
import l2s.gameserver.templates.npc.WalkerRoutePoint;
import l2s.gameserver.templates.npc.WalkerRouteType;
import l2s.gameserver.utils.NpcUtils;

import instances.BalthusKnightAntharas;

/**
 * @author Bonux
**/
public final class BalthusKnightsAntharasAI extends AbstractAntharasAI
{
	private class ChangeCurrentHpListenert implements OnChangeCurrentHpListener
	{
		@Override
		public void onChangeCurrentHp(Creature actor, double oldHp, double newHp)
		{
			NpcInstance npc = getActor();
			if(actor != npc)
				return;

			if(npc.getCurrentHpPercents() <= 25)
			{
				if(!_isQuail.compareAndSet(false, true))
					return;

				npc.removeListener(this);

				for(NpcInstance minion : _minions)
					minion.deleteMe();

				for(Player player : getPlayersInsideLair())
					player.sendPacket(new ExShowScreenMessage(NpcString.ANTHARAS_IS_TRYING_TO_ESCAPE, 5000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true, true));

				Reflection reflection = npc.getReflection();
				reflection.setReenterTime(System.currentTimeMillis(), false);
				reflection.startCollapseTimer(5, true);

				clearTasks();
				setIntention(CtrlIntention.AI_INTENTION_ACTIVE);

				npc.abortCast(true, false);
				npc.abortAttack(true, false);
				npc.getMovement().stopMove();

				setWalkerRoute(QUAIL_WALKER_ROUTE);
			}
		}
	}

	private static final WalkerRoute QUAIL_WALKER_ROUTE = new WalkerRoute(IdFactory.getInstance().getNextId(), WalkerRouteType.FINISH);
	static
	{
		QUAIL_WALKER_ROUTE.addPoint(new WalkerRoutePoint(new Location(180232, 114968, -7712), new NpcString[0], -1, 0, true, false));
	}

	private static final int RASH_NPC_ID = 31716;	// Лаш - Адъютант Поддержки

	private final ChangeCurrentHpListenert _changeCurrentHpListenert = new ChangeCurrentHpListenert();
	private final AtomicBoolean _isQuail = new AtomicBoolean(false);

	public BalthusKnightsAntharasAI(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtFinishWalkerRoute(int routeId)
	{
		super.onEvtFinishWalkerRoute(routeId);

		if(routeId == QUAIL_WALKER_ROUTE.getId())
		{
			NpcInstance actor = getActor();
			NpcUtils.spawnSingle(RASH_NPC_ID, actor.getLoc(), actor.getReflection());
			actor.deleteMe();
		}
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();

		NpcInstance actor = getActor();
		Reflection reflection = actor.getReflection();
		if(reflection instanceof BalthusKnightAntharas)
		{
			actor.addListener(_changeCurrentHpListenert);
			actor.setAggroRange(0);
			actor.getFlags().getInvulnerable().start(this);

			addTimer(PREPARE_MOVIE_TIMER_ID, ((BalthusKnightAntharas) reflection).getAntharasStartDelay() * 1000);
		}
	}

	@Override
	protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
	{
		if(_isQuail.get())
			return;

		super.onEvtAttacked(attacker, skill, damage);
	}

	@Override
	protected void onEvtAggression(Creature target, int aggro)
	{
		if(_isQuail.get())
			return;

		super.onEvtAggression(target, aggro);
	}

	@Override
	protected boolean createNewTask()
	{
		if(_isQuail.get())
			return false;

		return super.createNewTask();
	}

	@Override
	protected Collection<Player> getPlayersInsideLair()
	{
		NpcInstance actor = getActor();
		Reflection reflection = actor.getReflection();
		if(!reflection.isMain())
			return reflection.getPlayers();

		return World.getAroundPlayers(actor);
	}

	@Override
	protected NpcInstance spawnMinion()
	{
		NpcInstance actor = getActor();
		return NpcUtils.spawnSingle(Rnd.chance(50) ? 29190 : 29103, Location.findPointToStay(actor.getLoc(), 400, 700, actor.getGeoIndex()), actor.getReflection());
	}
}