package events;

import com.google.common.collect.ImmutableList;
import l2s.commons.collections.MultiValueSet;
import l2s.commons.geometry.Polygon;
import l2s.gameserver.Announcements;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.listener.actor.OnDeathListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Territory;
import l2s.gameserver.model.base.RestartType;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.entity.events.objects.EventPlayerObject;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Location;
import org.napile.pair.primitive.IntObjectPair;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

/**
 * @author VISTALL
 * @date 15:49/22.08.2011
 */
public class TeamVsTeamEvent extends CustomInstantTeamEvent
{
	private static final Territory[] _teleportLocs = new Territory[]
	{
		new Territory().add(new Polygon.PolygonBuilder()
                .add(189528, 24024)
				.add(188744, 24360)
                .add(189048, 25032)
                .add(189816, 24632)
                .setZmin(-3800).setZmax(-3400)
				.createPolygon()),
		new Territory().add(new Polygon.PolygonBuilder()
                .add(189800, 17864)
                .add(189736, 17352)
                .add(190488, 17272)
                .add(190728, 17992)
                .setZmin(-3800).setZmax(-3400)
				.createPolygon())
	};

	private OnDeathListener _deathListener = new OnDeathListenerImpl();
	private IntObjectMap<ScheduledFuture<?>> _deadList = new CHashIntObjectMap<ScheduledFuture<?>>();

	public TeamVsTeamEvent(MultiValueSet<String> set)
	{
		super(set);
	}

	@Override
	public void stopEvent(boolean force)
	{
		for(IntObjectPair<ScheduledFuture<?>> pair : _deadList.entrySet())
			pair.getValue().cancel(true);

		_deadList.clear();

		checkForWinner();

		List<EventPlayerObject> objects = getObjects(TeamType.BLUE);
		long bluePointCount = objects.stream()
				.mapToLong(playerObject -> playerObject.getPoints("EVENT_POINTS"))
				.sum();
		objects = getObjects(TeamType.RED);
		long redPointCount = objects.stream()
				.mapToLong(playerObject -> playerObject.getPoints("EVENT_POINTS"))
				.sum();

		super.stopEvent(force);

		Announcements.announceToAll(getName() + " event is over. " + _winner + " team won. Final score: Blue - "
				+ bluePointCount + " Red - " + redPointCount + ".");
	}

	@Override
	protected int getInstantId()
	{
		return 500;
	}

	@Override
	protected Location getTeleportLoc(TeamType team)
	{
		return _teleportLocs[team.ordinalWithoutNone()].getRandomLoc(_reflection.getGeoIndex());
	}

	@Override
	public void onAddEvent(GameObject o)
	{
		super.onAddEvent(o);
		if(o.isPlayer())
			o.getPlayer().addListener(_deathListener);
	}

	@Override
	public void onRemoveEvent(GameObject o)
	{
		super.onRemoveEvent(o);
		if(o.isPlayer())
			o.getPlayer().removeListener(_deathListener);
	}

	@Override
	public void onDie(Player player, Creature killer)
	{
	}

	@Override
	public synchronized void checkForWinner()
	{
		if(_state == State.NONE)
			return;

		TeamType winnerTeam = TeamType.NONE;
		long winnerKillCount = 0;
		for(TeamType team : TeamType.VALUES)
		{
			List<EventPlayerObject> objects = getObjects(team);

			long teamKillCount = objects.stream()
					.mapToLong(playerObject -> playerObject.getPoints("EVENT_POINTS"))
					.sum();
			if(teamKillCount > winnerKillCount)
			{
				winnerTeam = team;
                winnerKillCount = teamKillCount;
			}
		}

		if(winnerTeam != null)
		{
			_winner = winnerTeam;
		}
	}

	@Override
	protected void reward(List<EventPlayerObject> winners, List<EventPlayerObject> losers)
	{
		winners = winners.stream()
				.filter(object -> object.getPoints("EVENT_POINTS") >= 1)
				.collect(Collectors.toList());
		long teamKillCount = losers.stream()
				.mapToLong(playerObject -> playerObject.getPoints("EVENT_POINTS"))
				.sum();
		losers = teamKillCount < minKillCountForLoserTeamReward
				 ? Collections.emptyList()
				 : losers.stream()
						 .filter(object -> object.getPoints("EVENT_POINTS") >= 1)
						 .collect(Collectors.toList());

		super.reward(winners, losers);

		ImmutableList<EventPlayerObject> list = ImmutableList.<EventPlayerObject>builder()
				.addAll(getObjects(TeamType.BLUE))
				.addAll(getObjects(TeamType.RED))
				.build();
		Optional<Player> optionalPlayer = list.stream()
                .filter(eventPlayerObject -> eventPlayerObject.getPoints("EVENT_POINTS") > 0)
				.max(Comparator.comparingLong(playerObject -> playerObject.getPoints("EVENT_POINTS")))
				.map(EventPlayerObject::getPlayer);
		optionalPlayer.ifPresent(player ->
		{
			Arrays.stream(topKillerRewards)
					.forEach(reward -> ItemFunctions.addItem(player, reward.getId(), reward.getCount()));
		});
	}

	@Override
	protected boolean canWalkInWaitTime()
	{
		return false;
	}

	@Override
	protected void onRevive(Player player)
	{
		player.dispelDebuffs();

		player.setCurrentMp(player.getMaxMp());
		player.setCurrentCp(player.getMaxCp());
		player.setCurrentHp(player.getMaxHp(), false);

		resetSkills(player);
	}

	@Override
	protected void onTeleportOutsideOrExit(List<EventPlayerObject> objects, EventPlayerObject eventPlayerObject, boolean exit)
	{
		objects.remove(eventPlayerObject);

		if(exit)
			eventPlayerObject.clear();
	}

	@Override
	public void checkRestartLocs(Player player, Map<RestartType, Boolean> r)
	{
		r.clear();
	}

	@Override
	protected void actionUpdate(boolean start, Player player)
	{
	}

	private class RessurectTask implements Runnable
	{
		private Player _player;
		private int _seconds = 11;

		public RessurectTask(Player player)
		{
			_player = player;
		}

		@Override
		public void run()
		{
			_seconds -= 1;
			if(_seconds == 0)
			{
				_deadList.remove(_player.getObjectId());

				if (_player.getTeam() == TeamType.NONE) // Он уже не на эвенте.
					return;

				_player.teleToLocation(getTeleportLoc(_player.getTeam()));
				_player.doRevive();

				onRevive(_player);
			}
			else
			{
				_player.sendPacket(new SystemMessagePacket(SystemMsg.RESURRECTION_WILL_TAKE_PLACE_IN_THE_WAITING_ROOM_AFTER_S1_SECONDS).addNumber(_seconds));
				ScheduledFuture<?> f = ThreadPoolManager.getInstance().schedule(this, 1000L);

				_deadList.put(_player.getObjectId(), f);
			}
		}
	}

	private class OnDeathListenerImpl implements OnDeathListener
	{
		@Override
		public void onDeath(Creature victim, Creature killer)
		{
			_deadList.put(victim.getObjectId(), ThreadPoolManager.getInstance().schedule(new RessurectTask(victim.getPlayer()), 1000L));

			if(killer.isPlayable())
			{
				List<EventPlayerObject> objects = getObjects(killer.getTeam());
				Optional<EventPlayerObject> optionalPlayerObject = objects.stream()
						.filter(object -> Objects.equals(object.getPlayer(), killer.getPlayer()))
						.findAny();
				optionalPlayerObject.ifPresent(playerObject ->
						playerObject.increasePoints("EVENT_POINTS", 1));
			}
		}
	}
}
